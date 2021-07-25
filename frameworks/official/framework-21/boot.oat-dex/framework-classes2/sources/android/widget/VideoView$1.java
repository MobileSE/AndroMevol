package android.widget;

import android.media.MediaPlayer;

class VideoView$1 implements MediaPlayer.OnVideoSizeChangedListener {
    final /* synthetic */ VideoView this$0;

    VideoView$1(VideoView videoView) {
        this.this$0 = videoView;
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        VideoView.access$002(this.this$0, mp.getVideoWidth());
        VideoView.access$102(this.this$0, mp.getVideoHeight());
        if (VideoView.access$000(this.this$0) != 0 && VideoView.access$100(this.this$0) != 0) {
            this.this$0.getHolder().setFixedSize(VideoView.access$000(this.this$0), VideoView.access$100(this.this$0));
            this.this$0.requestLayout();
        }
    }
}
