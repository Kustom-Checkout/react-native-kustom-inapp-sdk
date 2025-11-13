package com.kustom.mobile.sdk.reactnative.checkout;

import androidx.annotation.NonNull;

import com.kustom.mobile.sdk.api.checkout.KustomCheckoutView;

import com.kustom.mobile.sdk.api.KustomEventHandler;
import com.kustom.mobile.sdk.api.KustomProductEvent;
import com.kustom.mobile.sdk.api.KustomMobileSDKError;
import com.kustom.mobile.sdk.api.component.KustomComponent;

public class KustomCheckoutViewEventHandler implements KustomEventHandler {

    public interface OnLoadListener {
        void onLoad(KustomCheckoutView kustomCheckoutView);
    }

    private static final String EVENT_LOAD = "load";
    private final KustomCheckoutViewEventSender kustomCheckoutViewEventSender;
    private final OnLoadListener onLoadListener;

    public KustomCheckoutViewEventHandler(@NonNull KustomCheckoutViewEventSender kustomCheckoutViewEventSender, OnLoadListener onLoadListener) {
        this.kustomCheckoutViewEventSender = kustomCheckoutViewEventSender;
        this.onLoadListener = onLoadListener;
    }

    @Override
    public void onEvent(@NonNull KustomComponent kustomComponent, @NonNull KustomProductEvent kustomProductEvent) {
        if (kustomComponent instanceof KustomCheckoutView) {
            String eventName = kustomProductEvent.getAction();
            if (EVENT_LOAD.equalsIgnoreCase(eventName)) {
                if (onLoadListener != null) {
                    onLoadListener.onLoad((KustomCheckoutView) kustomComponent);
                }
            }
            kustomCheckoutViewEventSender.sendKustomProductEvent((KustomCheckoutView) kustomComponent, kustomProductEvent);
        }
    }

    @Override
    public void onError(@NonNull KustomComponent kustomComponent, @NonNull KustomMobileSDKError kustomMobileSDKError) {
        if (kustomComponent instanceof KustomCheckoutView) {
            kustomCheckoutViewEventSender.sendKustomMobileSDKError((KustomCheckoutView) kustomComponent, kustomMobileSDKError);
        }
    }
}
