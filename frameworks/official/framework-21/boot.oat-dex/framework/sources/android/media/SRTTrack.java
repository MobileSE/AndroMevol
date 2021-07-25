package android.media;

import android.media.SubtitleTrack;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/* compiled from: SRTRenderer */
class SRTTrack extends WebVttTrack {
    private static final int KEY_LOCAL_SETTING = 102;
    private static final int KEY_START_TIME = 7;
    private static final int KEY_STRUCT_TEXT = 16;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final String TAG = "SRTTrack";
    private final Handler mEventHandler;

    SRTTrack(WebVttRenderingWidget renderingWidget, MediaFormat format) {
        super(renderingWidget, format);
        this.mEventHandler = null;
    }

    SRTTrack(Handler eventHandler, MediaFormat format) {
        super(null, format);
        this.mEventHandler = eventHandler;
    }

    /* access modifiers changed from: protected */
    @Override // android.media.SubtitleTrack
    public void onData(SubtitleData data) {
        try {
            TextTrackCue cue = new TextTrackCue();
            cue.mStartTimeMs = data.getStartTimeUs() / 1000;
            cue.mEndTimeMs = (data.getStartTimeUs() + data.getDurationUs()) / 1000;
            String[] lines = new String(data.getData(), "UTF-8").split("\\r?\\n");
            cue.mLines = new TextTrackCueSpan[lines.length][];
            int len$ = lines.length;
            int i$ = 0;
            int i = 0;
            while (i$ < len$) {
                cue.mLines[i] = new TextTrackCueSpan[]{new TextTrackCueSpan(lines[i$], -1)};
                i$++;
                i++;
            }
            addCue(cue);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "subtitle data is not UTF-8 encoded: " + e);
        }
    }

    @Override // android.media.WebVttTrack, android.media.SubtitleTrack
    public void onData(byte[] data, boolean eos, long runID) {
        String header;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));
            while (br.readLine() != null && (header = br.readLine()) != null) {
                TextTrackCue cue = new TextTrackCue();
                String[] startEnd = header.split("-->");
                cue.mStartTimeMs = parseMs(startEnd[0]);
                cue.mEndTimeMs = parseMs(startEnd[1]);
                List<String> paragraph = new ArrayList<>();
                while (true) {
                    String s = br.readLine();
                    if (s == null || s.trim().equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                        int i = 0;
                        cue.mLines = new TextTrackCueSpan[paragraph.size()][];
                        cue.mStrings = (String[]) paragraph.toArray(new String[0]);
                    } else {
                        paragraph.add(s);
                    }
                }
                int i2 = 0;
                cue.mLines = new TextTrackCueSpan[paragraph.size()][];
                cue.mStrings = (String[]) paragraph.toArray(new String[0]);
                for (String line : paragraph) {
                    TextTrackCueSpan[] span = {new TextTrackCueSpan(line, -1)};
                    cue.mStrings[i2] = line;
                    i2++;
                    cue.mLines[i2] = span;
                }
                addCue(cue);
            }
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "subtitle data is not UTF-8 encoded: " + e);
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage(), ioe);
        }
    }

    @Override // android.media.WebVttTrack, android.media.SubtitleTrack
    public void updateView(Vector<SubtitleTrack.Cue> activeCues) {
        if (getRenderingWidget() != null) {
            super.updateView(activeCues);
        } else if (this.mEventHandler != null) {
            Iterator<SubtitleTrack.Cue> it = activeCues.iterator();
            while (it.hasNext()) {
                SubtitleTrack.Cue cue = it.next();
                Parcel parcel = Parcel.obtain();
                parcel.writeInt(102);
                parcel.writeInt(7);
                parcel.writeInt((int) cue.mStartTimeMs);
                parcel.writeInt(16);
                StringBuilder sb = new StringBuilder();
                for (String line : ((TextTrackCue) cue).mStrings) {
                    sb.append(line).append('\n');
                }
                byte[] buf = sb.toString().getBytes();
                parcel.writeInt(buf.length);
                parcel.writeByteArray(buf);
                this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(99, 0, 0, parcel));
            }
            activeCues.clear();
        }
    }

    private static long parseMs(String in) {
        long hours = Long.parseLong(in.split(":")[0].trim());
        long minutes = Long.parseLong(in.split(":")[1].trim());
        long seconds = Long.parseLong(in.split(":")[2].split(",")[0].trim());
        return (60 * hours * 60 * 1000) + (60 * minutes * 1000) + (1000 * seconds) + Long.parseLong(in.split(":")[2].split(",")[1].trim());
    }
}
