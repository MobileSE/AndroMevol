package android.widget;

import android.media.SubtitleTrack;

class VideoView$8 implements SubtitleTrack.RenderingWidget.OnChangedListener {
    final /* synthetic */ VideoView this$0;

    VideoView$8(VideoView videoView) {
        this.this$0 = videoView;
    }

    public void onChanged(SubtitleTrack.RenderingWidget renderingWidget) {
        this.this$0.invalidate();
    }
}
