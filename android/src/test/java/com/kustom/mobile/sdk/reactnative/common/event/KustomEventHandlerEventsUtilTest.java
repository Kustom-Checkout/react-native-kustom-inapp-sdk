package com.kustom.mobile.sdk.reactnative.common.event;

import android.view.View;

import com.kustom.mobile.sdk.reactnative.common.util.ArgumentsUtil;
import com.kustom.mobile.sdk.api.KustomMobileSDKError;
import com.kustom.mobile.sdk.api.KustomProductEvent;
import com.kustom.mobile.sdk.api.checkout.KustomCheckoutSDKError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class KustomEventHandlerEventsUtilTest {

    private MockedStatic<ArgumentsUtil> mockedStaticArgumentsUtil;
    private ComponentEventSender componentEventSender;
    private View view;

    @Before
    public void setUp() {
        componentEventSender = Mockito.mock(ComponentEventSender.class);
        view = Mockito.mock(View.class);

        mockedStaticArgumentsUtil = Mockito.mockStatic(ArgumentsUtil.class);
        Mockito.when(ArgumentsUtil.createMap(Mockito.anyMap())).thenAnswer(invocation -> null);
    }

    @After
    public void teardown() {
        mockedStaticArgumentsUtil.close();
    }

    @Test
    public void sendKustomProductEvent() {
        // Given
        KustomProductEvent kustomProductEvent = new KustomProductEvent("action", new HashMap<>(), "testSessionId");

        // When
        KustomEventHandlerEventsUtil.sendKustomProductEvent(componentEventSender, view, kustomProductEvent);

        // Then
        Mockito.verify(componentEventSender).postEventForView(view, KustomEventHandlerEventsUtil.EVENT_NAME_ON_EVENT, null);
    }

    @Test
    public void sendKustomMobileSDKError() {
        // Given
        KustomMobileSDKError kustomMobileSDKError = new KustomCheckoutSDKError("name", "message", true, "sessionId");

        // When
        KustomEventHandlerEventsUtil.sendKustomMobileSDKError(componentEventSender, view, kustomMobileSDKError);

        // Then
        Mockito.verify(componentEventSender).postEventForView(view, KustomEventHandlerEventsUtil.EVENT_NAME_ON_EVENT, null);
    }
}
