package android.widget;

import android.media.MediaPlayer;
import android.media.Metadata;

class VideoView$2 implements MediaPlayer.OnPreparedListener {
    final /* synthetic */ VideoView this$0;

    VideoView$2(VideoView videoView) {
        this.this$0 = videoView;
    }

    public void onPrepared(MediaPlayer mp) {
        boolean z;
        boolean z2;
        boolean z3;
        VideoView.access$202(this.this$0, 2);
        Metadata data = mp.getMetadata(false, false);
        if (data != null) {
            VideoView videoView = this.this$0;
            if (!data.has(1) || data.getBoolean(1)) {
                z = true;
            } else {
                z = false;
            }
            VideoView.access$302(videoView, z);
            VideoView videoView2 = this.this$0;
            if (!data.has(2) || data.getBoolean(2)) {
                z2 = true;
            } else {
                z2 = false;
            }
            VideoView.access$402(videoView2, z2);
            VideoView videoView3 = this.this$0;
            if (!data.has(3) || data.getBoolean(3)) {
                z3 = true;
            } else {
                z3 = false;
            }
            VideoView.access$502(videoView3, z3);
        } else {
            VideoView.access$302(this.this$0, VideoView.access$402(this.this$0, VideoView.access$502(this.this$0, true)));
        }
        if (VideoView.access$600(this.this$0) != null) {
            VideoView.access$600(this.this$0).onPrepared(VideoView.access$700(this.this$0));
        }
        if (VideoView.access$800(this.this$0) != null) {
            VideoView.access$800(this.this$0).setEnabled(true);
        }
        VideoView.access$002(this.this$0, mp.getVideoWidth());
        VideoView.access$102(this.this$0, mp.getVideoHeight());
        int seekToPosition = VideoView.access$900(this.this$0);
        if (seekToPosition != 0) {
            this.this$0.seekTo(seekToPosition);
        }
        if (VideoView.access$000(this.this$0) != 0 && VideoView.access$100(this.this$0) != 0) {
            this.this$0.getHolder().setFixedSize(VideoView.access$000(this.this$0), VideoView.access$100(this.this$0));
            if (VideoView.access$1000(this.this$0) != VideoView.access$000(this.this$0) || VideoView.access$1100(this.this$0) != VideoView.access$100(this.this$0)) {
                return;
            }
            if (VideoView.access$1200(this.this$0) == 3) {
                this.this$0.start();
                if (VideoView.access$800(this.this$0) != null) {
                    VideoView.access$800(this.this$0).show();
                }
            } else if (this.this$0.isPlaying()) {
            } else {
                if ((seekToPosition != 0 || this.this$0.getCurrentPosition() > 0) && VideoView.access$800(this.this$0) != null) {
                    VideoView.access$800(this.this$0).show(0);
                }
            }
        } else if (VideoView.access$1200(this.this$0) == 3) {
            this.this$0.start();
        }
    }
}
