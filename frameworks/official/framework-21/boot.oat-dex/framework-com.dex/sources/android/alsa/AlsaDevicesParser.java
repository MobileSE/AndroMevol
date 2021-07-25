package android.alsa;

import android.content.Context;
import android.provider.Downloads;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class AlsaDevicesParser {
    private static final String TAG = "AlsaDevicesParser";
    private static final int kEndIndex_CardNum = 8;
    private static final int kEndIndex_DeviceNum = 11;
    private static final int kIndex_CardDeviceField = 5;
    private static final int kStartIndex_CardNum = 6;
    private static final int kStartIndex_DeviceNum = 9;
    private static final int kStartIndex_Type = 14;
    private static LineTokenizer mTokenizer = new LineTokenizer(" :[]-");
    private Vector<AlsaDeviceRecord> deviceRecords_ = new Vector<>();
    private boolean mHasCaptureDevices = false;
    private boolean mHasMIDIDevices = false;
    private boolean mHasPlaybackDevices = false;

    public class AlsaDeviceRecord {
        public static final int kDeviceDir_Capture = 0;
        public static final int kDeviceDir_Playback = 1;
        public static final int kDeviceDir_Unknown = -1;
        public static final int kDeviceType_Audio = 0;
        public static final int kDeviceType_Control = 1;
        public static final int kDeviceType_MIDI = 2;
        public static final int kDeviceType_Unknown = -1;
        int mCardNum = -1;
        int mDeviceDir = -1;
        int mDeviceNum = -1;
        int mDeviceType = -1;

        public AlsaDeviceRecord() {
        }

        public boolean parse(String line) {
            int delimOffset = 0;
            int tokenIndex = 0;
            while (true) {
                int tokenOffset = AlsaDevicesParser.mTokenizer.nextToken(line, delimOffset);
                if (tokenOffset == -1) {
                    return true;
                }
                delimOffset = AlsaDevicesParser.mTokenizer.nextDelimiter(line, tokenOffset);
                if (delimOffset == -1) {
                    delimOffset = line.length();
                }
                String token = line.substring(tokenOffset, delimOffset);
                switch (tokenIndex) {
                    case 1:
                        this.mCardNum = Integer.parseInt(token);
                        if (line.charAt(delimOffset) == '-') {
                            break;
                        } else {
                            tokenIndex++;
                            break;
                        }
                    case 2:
                        this.mDeviceNum = Integer.parseInt(token);
                        break;
                    case 3:
                        if (!token.equals("digital")) {
                            if (!token.equals(Downloads.Impl.COLUMN_CONTROL)) {
                                if (token.equals("raw")) {
                                }
                                break;
                            } else {
                                this.mDeviceType = 1;
                                break;
                            }
                        } else {
                            break;
                        }
                    case 4:
                        if (!token.equals(Context.AUDIO_SERVICE)) {
                            if (!token.equals("midi")) {
                                break;
                            } else {
                                this.mDeviceType = 2;
                                AlsaDevicesParser.this.mHasMIDIDevices = true;
                                break;
                            }
                        } else {
                            this.mDeviceType = 0;
                            break;
                        }
                    case 5:
                        if (!token.equals("capture")) {
                            if (!token.equals("playback")) {
                                break;
                            } else {
                                this.mDeviceDir = 1;
                                AlsaDevicesParser.this.mHasPlaybackDevices = true;
                                break;
                            }
                        } else {
                            this.mDeviceDir = 0;
                            AlsaDevicesParser.this.mHasCaptureDevices = true;
                            break;
                        }
                }
                tokenIndex++;
            }
        }

        public String textFormat() {
            StringBuilder sb = new StringBuilder();
            sb.append("[" + this.mCardNum + ":" + this.mDeviceNum + "]");
            switch (this.mDeviceType) {
                case -1:
                    sb.append(" N/A");
                    break;
                case 0:
                    sb.append(" Audio");
                    break;
                case 1:
                    sb.append(" Control");
                    break;
                case 2:
                    sb.append(" MIDI");
                    break;
            }
            switch (this.mDeviceDir) {
                case -1:
                    sb.append(" N/A");
                    break;
                case 0:
                    sb.append(" Capture");
                    break;
                case 1:
                    sb.append(" Playback");
                    break;
            }
            return sb.toString();
        }
    }

    private boolean isLineDeviceRecord(String line) {
        return line.charAt(5) == '[';
    }

    public int getNumDeviceRecords() {
        return this.deviceRecords_.size();
    }

    public AlsaDeviceRecord getDeviceRecordAt(int index) {
        return this.deviceRecords_.get(index);
    }

    public void Log() {
        int numDevRecs = getNumDeviceRecords();
        for (int index = 0; index < numDevRecs; index++) {
            Slog.w(TAG, "usb:" + getDeviceRecordAt(index).textFormat());
        }
    }

    public boolean hasPlaybackDevices() {
        return this.mHasPlaybackDevices;
    }

    public boolean hasPlaybackDevices(int card) {
        for (int index = 0; index < this.deviceRecords_.size(); index++) {
            AlsaDeviceRecord deviceRecord = this.deviceRecords_.get(index);
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCaptureDevices() {
        return this.mHasCaptureDevices;
    }

    public boolean hasCaptureDevices(int card) {
        for (int index = 0; index < this.deviceRecords_.size(); index++) {
            AlsaDeviceRecord deviceRecord = this.deviceRecords_.get(index);
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 0 && deviceRecord.mDeviceDir == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMIDIDevices() {
        return this.mHasMIDIDevices;
    }

    public boolean hasMIDIDevices(int card) {
        for (int index = 0; index < this.deviceRecords_.size(); index++) {
            AlsaDeviceRecord deviceRecord = this.deviceRecords_.get(index);
            if (deviceRecord.mCardNum == card && deviceRecord.mDeviceType == 2) {
                return true;
            }
        }
        return false;
    }

    public void scan() {
        this.deviceRecords_.clear();
        try {
            FileReader reader = new FileReader(new File("/proc/asound/devices"));
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    reader.close();
                    return;
                } else if (isLineDeviceRecord(line)) {
                    AlsaDeviceRecord deviceRecord = new AlsaDeviceRecord();
                    deviceRecord.parse(line);
                    this.deviceRecords_.add(deviceRecord);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
