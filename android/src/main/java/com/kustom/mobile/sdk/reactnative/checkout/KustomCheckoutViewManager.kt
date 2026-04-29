package com.kustom.mobile.sdk.reactnative.checkout

import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.kustom.mobile.sdk.reactnative.spec.RNKustomCheckoutViewSpec

@ReactModule(name = KustomCheckoutViewManager.NAME)
class KustomCheckoutViewManager : RNKustomCheckoutViewSpec<KustomReactCheckoutView>() {

    override fun getName() = NAME

    override fun createViewInstance(context: ThemedReactContext) =
        KustomReactCheckoutView(context)

    @ReactProp(name = "returnUrl")
    override fun setReturnUrl(view: KustomReactCheckoutView, url: String?) {
        view.setReturnUrl(url)
    }

    override fun setSnippet(view: KustomReactCheckoutView, snippet: String) {
        view.setSnippet(snippet)
    }

    override fun suspend(view: KustomReactCheckoutView) {
        view.suspend()
    }

    override fun resume(view: KustomReactCheckoutView) {
        view.resume()
    }

    override fun getExportedCustomDirectEventTypeConstants() = mapOf(
        "onEvent" to mapOf("registrationName" to "onEvent"),
        "onError" to mapOf("registrationName" to "onError"),
        "onResized" to mapOf("registrationName" to "onResized"),
        "onCheckoutViewReady" to mapOf("registrationName" to "onCheckoutViewReady"),
    )

    companion object {
        const val NAME = "RNKustomCheckoutView"
    }
}
