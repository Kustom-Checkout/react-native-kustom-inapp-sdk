package com.kustom.mobile.sdk.reactnative;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.kustom.mobile.sdk.reactnative.checkout.KustomCheckoutViewManager;

import java.util.List;

public class KustomMobileSDKPackage implements ReactPackage {

    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return List.of();
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return List.of(
                new KustomCheckoutViewManager(reactContext)
        );
    }
}
