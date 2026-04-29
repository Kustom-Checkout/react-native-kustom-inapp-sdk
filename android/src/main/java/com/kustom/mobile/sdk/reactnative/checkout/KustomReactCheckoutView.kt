package com.kustom.mobile.sdk.reactnative.checkout

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.kustom.mobile.sdk.api.KustomEventHandler
import com.kustom.mobile.sdk.api.KustomMobileSDKError
import com.kustom.mobile.sdk.api.KustomProductEvent
import com.kustom.mobile.sdk.api.checkout.KustomCheckoutView
import com.kustom.mobile.sdk.api.component.KustomComponent
import com.kustom.mobile.sdk.reactnative.DynamicMapSerializer
import kotlinx.serialization.json.Json

class KustomReactCheckoutView(context: Context) : FrameLayout(context), KustomEventHandler {

    private val reactContext: ReactContext = context as ReactContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val checkoutView: KustomCheckoutView
    private var readyDispatched = false

    init {
        Log.d(TAG, "init: creating KustomCheckoutView")
        checkoutView = KustomCheckoutView(context, null, 0, this)
        addView(checkoutView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        Log.d(TAG, "init: checkoutView added, childCount=${checkoutView.childCount}")
    }

    // region KustomEventHandler

    override fun onEvent(component: KustomComponent, event: KustomProductEvent) {
        Log.d(TAG, "onEvent: action=${event.action} params=${event.params}")
        if (event.action.equals("load", ignoreCase = true)) {
            Log.d(TAG, "onEvent: load received — injecting resize listener")
            mainHandler.post { injectResizeListener() }
        }
        val params = Arguments.createMap().apply {
            putMap("productEvent", Arguments.createMap().apply {
                putString("action", event.action)
                putString("params", Json.encodeToString(DynamicMapSerializer, event.params))
            })
        }
        dispatch("onEvent", params)
    }

    override fun onError(component: KustomComponent, error: KustomMobileSDKError) {
        Log.e(TAG, "onError: isFatal=${error.isFatal} name=${error.name} message=${error.message}")
        val params = Arguments.createMap().apply {
            putMap("error", Arguments.createMap().apply {
                putBoolean("isFatal", error.isFatal)
                putString("message", error.message ?: "")
                putString("name", error.name ?: "")
            })
        }
        dispatch("onError", params)
    }

    // endregion

    // region JS interface (called from WebView JS thread → hop to main)

    @JavascriptInterface
    fun onResized(heightDp: Int) {
        Log.d(TAG, "onResized: heightDp=$heightDp")
        mainHandler.post {
            val heightPx = dpToPx(heightDp)
            Log.d(TAG, "onResized: applying heightPx=$heightPx")
            checkoutView.layoutParams = checkoutView.layoutParams?.also { it.height = heightPx }
                ?: LayoutParams(LayoutParams.MATCH_PARENT, heightPx)
            checkoutView.requestLayout()

            val params = Arguments.createMap().apply {
                putString("height", heightDp.toString())
            }
            dispatch("onResized", params)
        }
    }

    // endregion

    // region Commands

    fun setSnippet(snippet: String) {
        if (snippet.isBlank()) {
            Log.w(TAG, "setSnippet: blank snippet, ignoring")
            return
        }
        Log.d(TAG, "setSnippet: length=${snippet.length} preview=${snippet.take(80)}")
        checkoutView.setSnippet(snippet)
        // addJavascriptInterface only takes effect on the NEXT page load, so register before the page starts loading.
        val webView = findWebView()
        if (webView != null) {
            Log.d(TAG, "setSnippet: WebView found, registering NativeResizeObserver before page load")
            webView.addJavascriptInterface(this, "NativeResizeObserver")
        } else {
            Log.w(TAG, "setSnippet: WebView not found after setSnippet — NativeResizeObserver will be missing")
        }
    }

    fun setReturnUrl(url: String?) { checkoutView.returnUrl = url }

    fun suspend() = checkoutView.suspend()

    fun resume() = checkoutView.resume()

    // endregion

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow: readyDispatched=$readyDispatched")
        if (!readyDispatched) {
            readyDispatched = true
            dispatch("onCheckoutViewReady", null)
        }
    }

    // region Private helpers

    private fun injectResizeListener() {
        Log.d(TAG, "injectResizeListener: checkoutView.childCount=${checkoutView.childCount}")
        val webView = findWebView()
        if (webView == null) {
            Log.w(TAG, "injectResizeListener: no WebView found — resize listener NOT injected")
            return
        }
        Log.d(TAG, "injectResizeListener: found WebView url=${webView.url}")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                Log.d("$TAG/JS", "${msg.message()} [${msg.sourceId()}:${msg.lineNumber()}]")
                return true
            }
        }
        webView.evaluateJavascript(RESIZE_LISTENER_JS, null)
        Log.d(TAG, "injectResizeListener: JS injected")
    }

    private fun findWebView(): WebView? {
        for (i in 0 until checkoutView.childCount) {
            val child = checkoutView.getChildAt(i)
            if (child is WebView) return child
        }
        return null
    }

    private fun dispatch(eventName: String, params: WritableMap?) {
        UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
            ?.dispatchEvent(RNEvent(id, eventName, params))
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

    // endregion

    private class RNEvent(
        viewId: Int,
        private val name: String,
        private val data: WritableMap?,
    ) : Event<RNEvent>(viewId) {
        override fun getEventName() = name
        override fun getEventData() = data ?: Arguments.createMap()
    }

    companion object {
        private const val TAG = "KustomCheckoutView"

        private val RESIZE_LISTENER_JS = """
            (function() {
                const kcoIframe = document.getElementById('klarna-checkout-iframe');
                console.log('[KustomSDK] resize listener injected, kcoIframe=' + kcoIframe);
                window.addEventListener('message', function(message) {
                    console.log('[KustomSDK] message received origin=' + message.origin + ' data=' + JSON.stringify(message.data));
                    if (message.source === kcoIframe || !message.data) return;
                    try {
                        const data = JSON.parse(message.data);
                        console.log('[KustomSDK] parsed event=' + data.event);
                        if (data.event === 'frame:checkout:resize') {
                            const listener = window.NativeResizeObserver;
                            console.log('[KustomSDK] resize event, height=' + data.args[0] + ' listener=' + listener);
                            if (listener) listener.onResized(data.args[0]);
                        }
                    } catch (e) {
                        console.log('[KustomSDK] parse failed: ' + e);
                    }
                });
            })();
        """.trimIndent()
    }
}
