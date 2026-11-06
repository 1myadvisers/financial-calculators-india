package com.rkcalc.financecalculatorindia

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.setBackgroundColor(Color.parseColor("#F3EFE6"))
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        setContentView(webView)
        webView.webViewClient = LocalAssetClient()
        webView.loadUrl("$ORIGIN/index.html")

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) webView.goBack() else finish()
                }
            },
        )
    }
}

private const val ORIGIN = "https://appassets.androidplatform.net"

private class LocalAssetClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url
        if (url.host == "appassets.androidplatform.net") return false
        return try {
            view.context.startActivity(Intent(Intent.ACTION_VIEW, url))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url
        if (url.host != "appassets.androidplatform.net") return null
        val relative = (url.path ?: "/").trimStart('/')
        if (relative.isEmpty()) return open(view, "index.html")
        return open(view, relative) ?: if ('.' !in relative.substringAfterLast('/')) {
            open(view, "index.html")
        } else {
            null
        }
    }

    private fun open(view: WebView, assetPath: String): WebResourceResponse? {
        val stream = try {
            view.context.assets.open(assetPath)
        } catch (_: FileNotFoundException) {
            return null
        }
        val text = assetPath.endsWith(".html") ||
            assetPath.endsWith(".js") ||
            assetPath.endsWith(".css") ||
            assetPath.endsWith(".svg") ||
            assetPath.endsWith(".json") ||
            assetPath.endsWith(".webmanifest")
        return WebResourceResponse(mime(assetPath), if (text) "utf-8" else null, stream)
    }

    private fun mime(path: String): String = when {
        path.endsWith(".html") -> "text/html"
        path.endsWith(".js") -> "text/javascript"
        path.endsWith(".css") -> "text/css"
        path.endsWith(".svg") -> "image/svg+xml"
        path.endsWith(".png") -> "image/png"
        path.endsWith(".json") -> "application/json"
        path.endsWith(".webmanifest") -> "application/manifest+json"
        else -> "application/octet-stream"
    }
}
