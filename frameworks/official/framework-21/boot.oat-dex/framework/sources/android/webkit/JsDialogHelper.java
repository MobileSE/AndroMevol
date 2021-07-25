package android.webkit;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;

public class JsDialogHelper {
    public static final int ALERT = 1;
    public static final int CONFIRM = 2;
    public static final int PROMPT = 3;
    private static final String TAG = "JsDialogHelper";
    public static final int UNLOAD = 4;
    private final String mDefaultValue;
    private final String mMessage;
    private final JsPromptResult mResult;
    private final int mType;
    private final String mUrl;

    public JsDialogHelper(JsPromptResult result, int type, String defaultValue, String message, String url) {
        this.mResult = result;
        this.mDefaultValue = defaultValue;
        this.mMessage = message;
        this.mType = type;
        this.mUrl = url;
    }

    public JsDialogHelper(JsPromptResult result, Message msg) {
        this.mResult = result;
        this.mDefaultValue = msg.getData().getString("default");
        this.mMessage = msg.getData().getString("message");
        this.mType = msg.getData().getInt("type");
        this.mUrl = msg.getData().getString("url");
    }

    public boolean invokeCallback(WebChromeClient client, WebView webView) {
        switch (this.mType) {
            case 1:
                return client.onJsAlert(webView, this.mUrl, this.mMessage, this.mResult);
            case 2:
                return client.onJsConfirm(webView, this.mUrl, this.mMessage, this.mResult);
            case 3:
                return client.onJsPrompt(webView, this.mUrl, this.mMessage, this.mDefaultValue, this.mResult);
            case 4:
                return client.onJsBeforeUnload(webView, this.mUrl, this.mMessage, this.mResult);
            default:
                throw new IllegalArgumentException("Unexpected type: " + this.mType);
        }
    }

    public void showDialog(Context context) {
        String title;
        String displayMessage;
        int positiveTextId;
        int negativeTextId;
        if (!canShowAlertDialog(context)) {
            Log.w(TAG, "Cannot create a dialog, the WebView context is not an Activity");
            this.mResult.cancel();
            return;
        }
        if (this.mType == 4) {
            title = context.getString(17040332);
            displayMessage = context.getString(17040335, this.mMessage);
            positiveTextId = 17040333;
            negativeTextId = 17040334;
        } else {
            title = getJsDialogTitle(context);
            displayMessage = this.mMessage;
            positiveTextId = 17039370;
            negativeTextId = 17039360;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setOnCancelListener(new CancelListener());
        if (this.mType != 3) {
            builder.setMessage(displayMessage);
            builder.setPositiveButton(positiveTextId, new PositiveListener(null));
        } else {
            View view = LayoutInflater.from(context).inflate(17367135, (ViewGroup) null);
            EditText edit = (EditText) view.findViewById(16909074);
            edit.setText(this.mDefaultValue);
            builder.setPositiveButton(positiveTextId, new PositiveListener(edit));
            ((TextView) view.findViewById(R.id.message)).setText(this.mMessage);
            builder.setView(view);
        }
        if (this.mType != 1) {
            builder.setNegativeButton(negativeTextId, new CancelListener());
        }
        builder.show();
    }

    private class CancelListener implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener {
        private CancelListener() {
        }

        @Override // android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialog) {
            JsDialogHelper.this.mResult.cancel();
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            JsDialogHelper.this.mResult.cancel();
        }
    }

    private class PositiveListener implements DialogInterface.OnClickListener {
        private final EditText mEdit;

        public PositiveListener(EditText edit) {
            this.mEdit = edit;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            if (this.mEdit == null) {
                JsDialogHelper.this.mResult.confirm();
            } else {
                JsDialogHelper.this.mResult.confirm(this.mEdit.getText().toString());
            }
        }
    }

    private String getJsDialogTitle(Context context) {
        String title = this.mUrl;
        if (URLUtil.isDataUrl(this.mUrl)) {
            return context.getString(17040331);
        }
        try {
            URL alertUrl = new URL(this.mUrl);
            return context.getString(17040330, alertUrl.getProtocol() + "://" + alertUrl.getHost());
        } catch (MalformedURLException e) {
            return title;
        }
    }

    private static boolean canShowAlertDialog(Context context) {
        return context instanceof Activity;
    }
}
