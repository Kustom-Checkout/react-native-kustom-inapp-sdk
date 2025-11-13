package com.kustom.mobile.sdk.reactnative.common.event;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.kustom.mobile.sdk.reactnative.common.serializer.DynamicMapSerializer;
import com.kustom.mobile.sdk.reactnative.common.util.ArgumentsUtil;
import com.kustom.mobile.sdk.reactnative.common.util.ParserUtil;
import com.kustom.mobile.sdk.api.KustomMobileSDKError;
import com.kustom.mobile.sdk.api.KustomProductEvent;

import java.util.HashMap;
import java.util.Map;

public class KustomEventHandlerEventsUtil {

    public static final String EVENT_NAME_ON_EVENT = "onEvent";
    public static final String EVENT_NAME_ON_ERROR = "onError";
    public static final String PARAM_NAME_IS_FATAL = "isFatal";
    public static final String PARAM_NAME_MESSAGE = "message";
    public static final String PARAM_NAME_NAME = "name";
    public static final String PARAM_NAME_SESSION_ID = "sessionId";

    private static final String PARAM_NAME_ACTION = "action";
    private static final String PARAM_NAME_PARAMS = "params";
    private static final String PARAM_NAME_PRODUCT_EVENT = "productEvent";

    public static <T extends View> void sendKustomProductEvent(@NonNull ComponentEventSender<T> componentEventSender, @Nullable View view, @NonNull KustomProductEvent kustomProductEvent) {
        String stringifiedParams = ParserUtil.INSTANCE.toJson(DynamicMapSerializer.INSTANCE, kustomProductEvent.getParams());
        String paramsJson = stringifiedParams == null ? "{}" : stringifiedParams;
        ReadableMap eventMap = ArgumentsUtil.createMap(new HashMap<String, Object>() {{
            put(PARAM_NAME_ACTION, kustomProductEvent.getAction());
            put(PARAM_NAME_PARAMS, paramsJson);
        }});
        WritableMap params = ArgumentsUtil.createMap(new HashMap<String, Object>() {{
            put(PARAM_NAME_PRODUCT_EVENT, eventMap);
        }});
        componentEventSender.postEventForView(view, EVENT_NAME_ON_EVENT, params);
    }
    public static <T extends View> void sendKustomMobileSDKError(@NonNull ComponentEventSender<T> componentEventSender, @Nullable View view, @NonNull KustomMobileSDKError kustomMobileSDKError) {
        ReadableMap eventMap = ArgumentsUtil.createMap(new HashMap<String, Object>() {{
            put(PARAM_NAME_NAME, kustomMobileSDKError.getName());
            put(PARAM_NAME_MESSAGE, kustomMobileSDKError.getMessage());
            put(PARAM_NAME_IS_FATAL, kustomMobileSDKError.isFatal());
        }});
        WritableMap params = ArgumentsUtil.createMap(new HashMap<String, Object>() {{
            put(PARAM_NAME_PRODUCT_EVENT, eventMap);
        }});
        componentEventSender.postEventForView(view, EVENT_NAME_ON_EVENT, params);
    }
    public static ReadableMap createMapFrom(HashMap<String, Object> map) {
        WritableMap writableMap = ArgumentsUtil.createMap(new HashMap<String, Object>());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                writableMap.putString(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Object) {

            }
        }
        return ArgumentsUtil.createMap(map);
    }
}
