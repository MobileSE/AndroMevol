package android.widget;

import android.media.MediaPlayer;

class VideoView$3 implements MediaPlayer.OnCompletionListener {
    final /* synthetic */ VideoView this$0;

    VideoView$3(VideoView videoView) {
        this.this$0 = videoView;
    }

    public void onCompletion(MediaPlayer mp) {
        VideoView.access$202(this.this$0, 5);
        VideoView.access$1202(this.this$0, 5);
        if (VideoView.access$800(this.this$0) != null) {
            VideoView.access$800(this.this$0).hide();
        }
        if (VideoView.access$1300(this.this$0) != null) {
            VideoView.access$1300(this.this$0).onCompletion(VideoView.access$700(this.this$0));
        }
    }
}
