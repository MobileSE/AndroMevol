package com.android.internal.util;

import android.os.Message;

public class State implements IState {
    protected State() {
    }

    @Override // com.android.internal.util.IState
    public void enter() {
    }

    @Override // com.android.internal.util.IState
    public void exit() {
    }

    @Override // com.android.internal.util.IState
    public boolean processMessage(Message msg) {
        return false;
    }

    @Override // com.android.internal.util.IState
    public String getName() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(36) + 1);
    }
}
