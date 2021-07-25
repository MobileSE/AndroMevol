package android.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.util.Log;

class VideoView$5 implements MediaPlayer.OnErrorListener {
    final /* synthetic */ VideoView this$0;

    VideoView$5(VideoView videoView) {
        this.this$0 = videoView;
    }

    public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
        int messageId;
        Log.d(VideoView.access$1500(this.this$0), "Error: " + framework_err + "," + impl_err);
        VideoView.access$202(this.this$0, -1);
        VideoView.access$1202(this.this$0, -1);
        if (VideoView.access$800(this.this$0) != null) {
            VideoView.access$800(this.this$0).hide();
        }
        if ((VideoView.access$1600(this.this$0) == null || !VideoView.access$1600(this.this$0).onError(VideoView.access$700(this.this$0), framework_err, impl_err)) && this.this$0.getWindowToken() != null) {
            VideoView.access$1700(this.this$0).getResources();
            if (framework_err == 200) {
                messageId = 17039381;
            } else {
                messageId = 17039377;
            }
            new AlertDialog.Builder(VideoView.access$1800(this.this$0)).setMessage(messageId).setPositiveButton(17039376, new DialogInterface.OnClickListener() {
                /* class android.widget.VideoView$5.AnonymousClass1 */

                public void onClick(DialogInterface dialog, int whichButton) {
                    if (VideoView.access$1300(VideoView$5.this.this$0) != null) {
                        VideoView.access$1300(VideoView$5.this.this$0).onCompletion(VideoView.access$700(VideoView$5.this.this$0));
                    }
                }
            }).setCancelable(false).show();
        }
        return true;
    }
}
