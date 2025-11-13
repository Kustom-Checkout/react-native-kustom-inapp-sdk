package com.kustom.mobile.sdk.reactnative;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.kustom.mobile.sdk.reactnative.checkout.KustomCheckoutViewManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class KustomMobileSDKPackageTest {

    private Application application;
    private ReactApplicationContext reactContext;

    @Before
    public void setup() {
        application = ApplicationProvider.getApplicationContext();
        reactContext = Mockito.mock(ReactApplicationContext.class);
        Mockito.when(reactContext.getApplicationContext()).thenAnswer(invocation -> application);
    }

    @Test
    public void testCreateViewManagers() {
        KustomMobileSDKPackage checkoutViewPackage = new KustomMobileSDKPackage();
        List<ViewManager> nativeModules = checkoutViewPackage.createViewManagers(reactContext);
        Assert.assertNotNull(nativeModules);
        Assert.assertFalse(nativeModules.isEmpty());
        Assert.assertEquals(1, nativeModules.size());
        Assert.assertTrue(nativeModules.get(0) instanceof KustomCheckoutViewManager);
    }
}
