package com.kustom.mobile.sdk.reactnative.checkout;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerHelper;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.kustom.mobile.sdk.reactnative.common.WebViewResizeObserver;
import com.kustom.mobile.sdk.reactnative.common.ui.ResizeObserverWrapperView;
import com.kustom.mobile.sdk.reactnative.spec.RNKustomCheckoutViewSpec;
import com.kustom.mobile.sdk.api.checkout.KustomCheckoutView;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class KustomCheckoutViewManager extends RNKustomCheckoutViewSpec<ResizeObserverWrapperView<KustomCheckoutView>> {

    private static final int MAX_SET_SNIPPET_RETRIES = 3;
    public static final String KUSTOM_CHECKOUT_VIEW_REACT_CLASS = "RNKustomCheckoutView";
    // Commands that can be triggered from RN
    public static final String COMMAND_SET_SNIPPET = "setSnippet";
    public static final String COMMAND_SUSPEND = "suspend";
    public static final String COMMAND_RESUME = "resume";

    private final ReactApplicationContext reactAppContext;
    /**
     * Store a map of views to event dispatchers so we send up events via the right views.
     */
    private final Map<ResizeObserverWrapperView<KustomCheckoutView>, EventDispatcher> viewToDispatcher;
    private final Map<KustomCheckoutView, ResizeObserverWrapperView<KustomCheckoutView>> checkoutViewToResizeObserverWrapperMap = new WeakHashMap<>();
    private final Map<ResizeObserverWrapperView<KustomCheckoutView>, Integer> setSnippetRetriesMap = new WeakHashMap<>();
    // Tracks the pending 500ms retry runnable per view so it can be cancelled on a new load cycle.
    private final Map<ResizeObserverWrapperView<KustomCheckoutView>, Runnable> pendingRetryMap = new WeakHashMap<>();
    // Fallback injection scheduled after setSnippet; cancelled if onLoad fires first.
    private final Map<ResizeObserverWrapperView<KustomCheckoutView>, Runnable> fallbackInjectionMap = new WeakHashMap<>();

    private final KustomCheckoutViewEventSender eventSender;
    private final KustomCheckoutViewEventHandler eventHandler;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public KustomCheckoutViewManager(ReactApplicationContext reactApplicationContext) {
        super();
        viewToDispatcher = new WeakHashMap<>();
        reactAppContext = reactApplicationContext;
        eventSender = new KustomCheckoutViewEventSender(viewToDispatcher);
        eventHandler = new KustomCheckoutViewEventHandler(eventSender, kustomCheckoutView -> {
            ResizeObserverWrapperView<KustomCheckoutView> resizeObserverWrapperView =
                    checkoutViewToResizeObserverWrapperMap.get(kustomCheckoutView);
            if (resizeObserverWrapperView == null) {
                return;
            }

            // onLoad fired — cancel the setSnippet fallback, it is no longer needed.
            Runnable fallback = fallbackInjectionMap.remove(resizeObserverWrapperView);
            if (fallback != null) {
                handler.removeCallbacks(fallback);
            }

            // Cancel any stale retry from a previous load cycle and give this cycle a fresh counter.
            Runnable staleRetry = pendingRetryMap.remove(resizeObserverWrapperView);
            if (staleRetry != null) {
                handler.removeCallbacks(staleRetry);
            }
            setSnippetRetriesMap.put(resizeObserverWrapperView, 0);

            handler.post(resizeObserverWrapperView::injectListenerToWebView);

            Integer retries = setSnippetRetriesMap.getOrDefault(resizeObserverWrapperView, 0);
            if (retries == null || retries >= MAX_SET_SNIPPET_RETRIES) {
                return;
            }

            Runnable retryRunnable = () -> {
                pendingRetryMap.remove(resizeObserverWrapperView);

                if (kustomCheckoutView == null) {
                    return;
                }

                boolean shouldRetry = false;

                if (kustomCheckoutView.getHeight() == 0) {
                    shouldRetry = true;
                }

                if (kustomCheckoutView.getChildAt(0) instanceof WebView) {
                    WebView internalWebView = (WebView) kustomCheckoutView.getChildAt(0);
                    if (internalWebView != null && internalWebView.getHeight() == 0) {
                        shouldRetry = true;
                    }
                }

                if (shouldRetry) {
                    resizeObserverWrapperView.injectListenerToWebView();
                    setSnippetRetriesMap.put(resizeObserverWrapperView, retries + 1);
                }
            };
            pendingRetryMap.put(resizeObserverWrapperView, retryRunnable);
            handler.postDelayed(retryRunnable, 500);
        });
    }

    @NonNull
    @Override
    public String getName() {
        return KUSTOM_CHECKOUT_VIEW_REACT_CLASS;
    }

    @NonNull
    @Override
    protected ResizeObserverWrapperView<KustomCheckoutView> createViewInstance(@NonNull ThemedReactContext themedReactContext) {
        KustomCheckoutView kustomCheckoutView = new KustomCheckoutView(reactAppContext.getCurrentActivity(), null, 0, eventHandler);
        ResizeObserverWrapperView<KustomCheckoutView> view = new ResizeObserverWrapperView<>(reactAppContext, null, kustomCheckoutView);

        checkoutViewToResizeObserverWrapperMap.put(kustomCheckoutView, view);

        // Each view has its own event dispatcher.
        EventDispatcher eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag((ReactContext) view.getContext(), view.getId());
        viewToDispatcher.put(view, eventDispatcher);

        view.initiateWebViewResizeObserver(WebViewResizeObserver.TargetElement.CHECKOUT_CONTAINER, (resizeObserverWrapperView, newHeight) -> {
            if (resizeObserverWrapperView == null || eventDispatcher == null) {
                return;
            }
            eventSender.sendOnResizedEvent(resizeObserverWrapperView.getView(), newHeight);
        });
        view.addJavascriptInterfaceToWebView();

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                eventSender.sendOnKustomCheckoutViewReadyEvent(view.getView());
                v.removeOnAttachStateChangeListener(this);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
            }
        });

        return view;
    }

    /**
     * Exposes direct event types that will be accessible as prop "callbacks" from RN.
     * <p>
     * Structure must follow:
     * { "<eventName>": {"registrationName": "<eventName>"} }
     */
    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return eventSender.getExportedCustomDirectEventTypeConstants();
    }

    /**
     * Handles commands received from RN to a specific view.
     *
     * @param root      view receiving the command
     * @param commandId identifier of the command
     * @param args      array of command arguments
     */
    @Override
    public void receiveCommand(@NonNull ResizeObserverWrapperView<KustomCheckoutView> root, String commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_SET_SNIPPET:
                setSnippet(root, args != null ? args.getString(0) : null);
                break;
            case COMMAND_SUSPEND:
                suspend(root);
                break;
            case COMMAND_RESUME:
                resume(root);
                break;
        }
    }

    @ReactProp(name = "returnUrl")
    @Override
    public void setReturnUrl(ResizeObserverWrapperView<KustomCheckoutView> view, @Nullable String value) {
        KustomCheckoutView checkoutView = view.getView();
        if (checkoutView != null) {
            if (!Objects.equals(value, checkoutView.getReturnUrl())) {
                checkoutView.setReturnUrl(value);
            }
        }
    }

    @Override
    public void setSnippet(ResizeObserverWrapperView<KustomCheckoutView> view, String snippet) {
        if (view == null) {
            return;
        }

        KustomCheckoutView kustomCheckoutView = view.getView();
        if (kustomCheckoutView == null) {
            return;
        }

        if (snippet == null || snippet.isEmpty()) {
            return;
        }

        // Cancel any pending state from a previous snippet before loading the new one.
        Runnable staleRetry = pendingRetryMap.remove(view);
        if (staleRetry != null) {
            handler.removeCallbacks(staleRetry);
        }
        Runnable existingFallback = fallbackInjectionMap.remove(view);
        if (existingFallback != null) {
            handler.removeCallbacks(existingFallback);
        }

        kustomCheckoutView.setSnippet(snippet);
        view.addJavascriptInterfaceToWebView();

        // Safety net: if the onLoad event never fires, inject the listener after a generous delay.
        Runnable fallback = () -> {
            fallbackInjectionMap.remove(view);
            view.injectListenerToWebView();
        };
        fallbackInjectionMap.put(view, fallback);
        handler.postDelayed(fallback, 3000);
    }

    @Override
    public void suspend(ResizeObserverWrapperView<KustomCheckoutView> view) {
        KustomCheckoutView kustomCheckoutView = view.getView();
        if (kustomCheckoutView != null) {
            kustomCheckoutView.suspend();
        }
    }

    @Override
    public void resume(ResizeObserverWrapperView<KustomCheckoutView> view) {
        KustomCheckoutView kustomCheckoutView = view.getView();
        if (kustomCheckoutView != null) {
            kustomCheckoutView.resume();
        }
    }
}
