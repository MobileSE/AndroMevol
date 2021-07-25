package android.widget;

import android.media.MediaPlayer;

class VideoView$6 implements MediaPlayer.OnBufferingUpdateListener {
    final /* synthetic */ VideoView this$0;

    VideoView$6(VideoView videoView) {
        this.this$0 = videoView;
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        VideoView.access$1902(this.this$0, percent);
    }
}
