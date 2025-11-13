package com.kustom.mobile.sdk.reactnative.checkout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.kustom.mobile.sdk.reactnative.common.event.ComponentEventSender;
import com.kustom.mobile.sdk.reactnative.common.event.KustomEventHandlerEventsUtil;
import com.kustom.mobile.sdk.reactnative.common.ui.ResizeObserverWrapperView;
import com.kustom.mobile.sdk.reactnative.common.util.ArgumentsUtil;
import com.kustom.mobile.sdk.api.KustomMobileSDKError;
import com.kustom.mobile.sdk.api.KustomProductEvent;
import com.kustom.mobile.sdk.api.checkout.KustomCheckoutView;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KustomCheckoutViewEventSender extends ComponentEventSender<ResizeObserverWrapperView<KustomCheckoutView>> {

    private static final String EVENT_NAME_ON_RESIZED = "onResized";
    private static final String EVENT_NAME_ON_CHECKOUT_VIEW_READY = "onCheckoutViewReady";

    KustomCheckoutViewEventSender(@NonNull final Map<ResizeObserverWrapperView<KustomCheckoutView>, EventDispatcher> viewToDispatcher) {
        super(viewToDispatcher);
    }

    @Override
    public Collection<String> getCallbackEventNames() {
        return Arrays.asList(
                KustomEventHandlerEventsUtil.EVENT_NAME_ON_EVENT,
                KustomEventHandlerEventsUtil.EVENT_NAME_ON_ERROR,
                EVENT_NAME_ON_RESIZED,
                EVENT_NAME_ON_CHECKOUT_VIEW_READY
        );
    }

    public void sendKustomProductEvent(@Nullable KustomCheckoutView view, @NonNull KustomProductEvent kustomProductEvent) {
        KustomEventHandlerEventsUtil.sendKustomProductEvent(this, view, kustomProductEvent);
    }

    public void sendKustomMobileSDKError(@Nullable KustomCheckoutView view, @NonNull KustomMobileSDKError kustomMobileSDKError) {
        KustomEventHandlerEventsUtil.sendKustomMobileSDKError(this, view, kustomMobileSDKError);
    }

    public void sendOnResizedEvent(@Nullable KustomCheckoutView view, int height) {
        WritableMap params = ArgumentsUtil.createMap(
                new HashMap<String, Object>() {{
                    put("height", String.valueOf(height));
                }}
        );
        postEventForView(view, EVENT_NAME_ON_RESIZED, params);
    }

    public void sendOnKustomCheckoutViewReadyEvent(@Nullable KustomCheckoutView view) {
        postEventForView(view, EVENT_NAME_ON_CHECKOUT_VIEW_READY, null);
    }
}
