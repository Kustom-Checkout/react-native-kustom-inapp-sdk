package com.kustom.mobile.sdk.reactnative.spec;

import android.view.View;

import androidx.annotation.Nullable;

import com.facebook.react.uimanager.SimpleViewManager;

public abstract class RNKustomCheckoutViewSpec<T extends View> extends SimpleViewManager<T> {

    public abstract void setReturnUrl(T view, @Nullable String value);

    public abstract void setSnippet(T view, String snippet);

    public abstract void suspend(T view);

    public abstract void resume(T view);
}
