package com.android.internal.app;

import android.content.Context;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.app.AlertController;

public class RestrictionsPinActivity extends AlertActivity implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener {
    private Button mCancelButton;
    private Runnable mCountdownRunnable = new Runnable() {
        /* class com.android.internal.app.RestrictionsPinActivity.AnonymousClass1 */

        public void run() {
            if (RestrictionsPinActivity.this.updatePinTimer(-1)) {
                RestrictionsPinActivity.this.mPinErrorMessage.setVisibility(4);
            }
        }
    };
    protected boolean mHasRestrictionsPin;
    private Button mOkButton;
    protected TextView mPinErrorMessage;
    protected EditText mPinText;
    protected UserManager mUserManager;

    @Override // com.android.internal.app.AlertActivity, android.app.Activity
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUserManager = (UserManager) getSystemService("user");
        this.mHasRestrictionsPin = this.mUserManager.hasRestrictionsChallenge();
        initUi();
        setupAlert();
    }

    /* access modifiers changed from: protected */
    public void initUi() {
        AlertController.AlertParams ap = this.mAlertParams;
        ap.mTitle = getString(R.string.restr_pin_enter_admin_pin);
        ap.mView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.restrictions_pin_challenge, (ViewGroup) null);
        this.mPinErrorMessage = (TextView) ap.mView.findViewById(R.id.pin_error_message);
        this.mPinText = (EditText) ap.mView.findViewById(R.id.pin_text);
        this.mOkButton = (Button) ap.mView.findViewById(R.id.pin_ok_button);
        this.mCancelButton = (Button) ap.mView.findViewById(R.id.pin_cancel_button);
        this.mPinText.addTextChangedListener(this);
        this.mOkButton.setOnClickListener(this);
        this.mCancelButton.setOnClickListener(this);
    }

    /* access modifiers changed from: protected */
    public boolean verifyingPin() {
        return true;
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        setPositiveButtonState(false);
        if (this.mUserManager.hasRestrictionsChallenge()) {
            this.mPinErrorMessage.setVisibility(4);
            this.mPinText.setOnEditorActionListener(this);
            updatePinTimer(-1);
        } else if (verifyingPin()) {
            setResult(-1);
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void setPositiveButtonState(boolean enabled) {
        this.mOkButton.setEnabled(enabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updatePinTimer(int pinTimerMs) {
        boolean enableInput;
        if (pinTimerMs < 0) {
            pinTimerMs = this.mUserManager.checkRestrictionsChallenge(null);
        }
        if (pinTimerMs >= 200) {
            if (pinTimerMs <= 60000) {
                int seconds = (pinTimerMs + 200) / 1000;
                String formatString = getResources().getQuantityString(R.plurals.restr_pin_countdown, seconds);
                this.mPinErrorMessage.setText(String.format(formatString, Integer.valueOf(seconds)));
            } else {
                this.mPinErrorMessage.setText(R.string.restr_pin_try_later);
            }
            enableInput = false;
            this.mPinErrorMessage.setVisibility(0);
            this.mPinText.setText(ProxyInfo.LOCAL_EXCL_LIST);
            this.mPinText.postDelayed(this.mCountdownRunnable, (long) Math.min(1000, pinTimerMs));
        } else {
            enableInput = true;
            this.mPinErrorMessage.setText(R.string.restr_pin_incorrect);
        }
        this.mPinText.setEnabled(enableInput);
        setPositiveButtonState(enableInput);
        return enableInput;
    }

    /* access modifiers changed from: protected */
    public void performPositiveButtonAction() {
        int result = this.mUserManager.checkRestrictionsChallenge(this.mPinText.getText().toString());
        if (result == -1) {
            setResult(-1);
            finish();
        } else if (result >= 0) {
            this.mPinErrorMessage.setText(R.string.restr_pin_incorrect);
            this.mPinErrorMessage.setVisibility(0);
            updatePinTimer(result);
            this.mPinText.setText(ProxyInfo.LOCAL_EXCL_LIST);
        }
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        CharSequence pin = this.mPinText.getText();
        setPositiveButtonState(pin != null && pin.length() >= 4);
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable s) {
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        performPositiveButtonAction();
        return true;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v == this.mOkButton) {
            performPositiveButtonAction();
        } else if (v == this.mCancelButton) {
            setResult(0);
            finish();
        }
    }
}
