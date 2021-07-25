package android.media;

import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import java.util.LinkedList;

public class AsyncPlayer {
    private static final int PLAY = 1;
    private static final int STOP = 2;
    private static final boolean mDebug = false;
    private final LinkedList<Command> mCmdQueue = new LinkedList<>();
    private MediaPlayer mPlayer;
    private int mState = 2;
    private String mTag;
    private Thread mThread;
    private PowerManager.WakeLock mWakeLock;

    /* access modifiers changed from: private */
    public static final class Command {
        int code;
        Context context;
        boolean looping;
        long requestTime;
        int stream;
        Uri uri;

        private Command() {
        }

        public String toString() {
            return "{ code=" + this.code + " looping=" + this.looping + " stream=" + this.stream + " uri=" + this.uri + " }";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSound(Command cmd) {
        try {
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(cmd.stream);
            player.setDataSource(cmd.context, cmd.uri);
            player.setLooping(cmd.looping);
            player.prepare();
            player.start();
            if (this.mPlayer != null) {
                this.mPlayer.release();
            }
            this.mPlayer = player;
            long delay = SystemClock.uptimeMillis() - cmd.requestTime;
            if (delay > 1000) {
                Log.w(this.mTag, "Notification sound delayed by " + delay + "msecs");
            }
        } catch (Exception e) {
            Log.w(this.mTag, "error loading sound for " + cmd.uri, e);
        }
    }

    /* access modifiers changed from: private */
    public final class Thread extends Thread {
        Thread() {
            super("AsyncPlayer-" + AsyncPlayer.this.mTag);
        }

        public void run() {
            Command cmd;
            while (true) {
                synchronized (AsyncPlayer.this.mCmdQueue) {
                    cmd = (Command) AsyncPlayer.this.mCmdQueue.removeFirst();
                }
                switch (cmd.code) {
                    case 1:
                        AsyncPlayer.this.startSound(cmd);
                        break;
                    case 2:
                        if (AsyncPlayer.this.mPlayer == null) {
                            Log.w(AsyncPlayer.this.mTag, "STOP command without a player");
                            break;
                        } else {
                            long delay = SystemClock.uptimeMillis() - cmd.requestTime;
                            if (delay > 1000) {
                                Log.w(AsyncPlayer.this.mTag, "Notification stop delayed by " + delay + "msecs");
                            }
                            AsyncPlayer.this.mPlayer.stop();
                            AsyncPlayer.this.mPlayer.release();
                            AsyncPlayer.this.mPlayer = null;
                            break;
                        }
                }
                synchronized (AsyncPlayer.this.mCmdQueue) {
                    if (AsyncPlayer.this.mCmdQueue.size() == 0) {
                        AsyncPlayer.this.mThread = null;
                        AsyncPlayer.this.releaseWakeLock();
                        return;
                    }
                }
            }
        }
    }

    public AsyncPlayer(String tag) {
        if (tag != null) {
            this.mTag = tag;
        } else {
            this.mTag = "AsyncPlayer";
        }
    }

    public void play(Context context, Uri uri, boolean looping, int stream) {
        Command cmd = new Command();
        cmd.requestTime = SystemClock.uptimeMillis();
        cmd.code = 1;
        cmd.context = context;
        cmd.uri = uri;
        cmd.looping = looping;
        cmd.stream = stream;
        synchronized (this.mCmdQueue) {
            enqueueLocked(cmd);
            this.mState = 1;
        }
    }

    public void stop() {
        synchronized (this.mCmdQueue) {
            if (this.mState != 2) {
                Command cmd = new Command();
                cmd.requestTime = SystemClock.uptimeMillis();
                cmd.code = 2;
                enqueueLocked(cmd);
                this.mState = 2;
            }
        }
    }

    private void enqueueLocked(Command cmd) {
        this.mCmdQueue.add(cmd);
        if (this.mThread == null) {
            acquireWakeLock();
            this.mThread = new Thread();
            this.mThread.start();
        }
    }

    public void setUsesWakeLock(Context context) {
        if (this.mWakeLock == null && this.mThread == null) {
            this.mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(1, this.mTag);
            return;
        }
        throw new RuntimeException("assertion failed mWakeLock=" + this.mWakeLock + " mThread=" + this.mThread);
    }

    private void acquireWakeLock() {
        if (this.mWakeLock != null) {
            this.mWakeLock.acquire();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseWakeLock() {
        if (this.mWakeLock != null) {
            this.mWakeLock.release();
        }
    }
}
