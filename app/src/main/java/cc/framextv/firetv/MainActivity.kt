package cc.framextv.firetv

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUi()

        webView = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            isFocusable = true
            isFocusableInTouchMode = true
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = true
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                allowFileAccess = false
                allowContentAccess = false
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                loadsImagesAutomatically = true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val scheme = request.url.scheme ?: return true
                return scheme != "https" && scheme != "http"
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                applyTvEnhancements()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(
                view: View,
                callback: CustomViewCallback
            ) {
                if (customView != null) {
                    callback.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback
                (webView.parent as? ViewGroup)?.removeView(webView)

                val root = FrameLayout(this@MainActivity).apply {
                    setBackgroundColor(Color.BLACK)
                    addView(
                        view,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    )
                }

                setContentView(root)
                hideSystemUi()
            }

            override fun onHideCustomView() {
                closeCustomView()
            }
        }

        setContentView(webView)
        webView.requestFocus(View.FOCUS_DOWN)
        webView.loadUrl("https://framextv.cc")
    }

    private fun applyTvEnhancements() {
        webView.evaluateJavascript(
            """
            (function() {
                const styleId = 'framex-tv-navigation-style';

                if (!document.getElementById(styleId)) {
                    const style = document.createElement('style');
                    style.id = styleId;
                    style.textContent = `
                        html {
                            scroll-behavior: smooth !important;
                            scrollbar-width: none !important;
                        }

                        body {
                            -webkit-tap-highlight-color: transparent;
                        }

                        ::-webkit-scrollbar {
                            display: none !important;
                            width: 0 !important;
                            height: 0 !important;
                        }

                        a:focus,
                        button:focus,
                        [role="button"]:focus,
                        [tabindex]:focus,
                        input:focus,
                        select:focus {
                            outline: 3px solid #00d9ff !important;
                            outline-offset: 3px !important;
                            box-shadow: 0 0 0 5px rgba(0, 217, 255, 0.22) !important;
                            border-radius: 5px;
                        }

                        .framex-tv-focused {
                            outline: 3px solid #00d9ff !important;
                            outline-offset: 4px !important;
                            box-shadow: 0 0 16px rgba(0, 217, 255, 0.48) !important;
                            border-radius: 8px;
                            z-index: 10;
                        }

                        a, button, [role="button"] {
                            -webkit-tap-highlight-color: transparent;
                        }

                        @media (prefers-reduced-motion: no-preference) {
                            .framex-tv-focused {
                                transition: outline-color 100ms ease,
                                            box-shadow 100ms ease;
                            }
                        }
                    `;
                    (document.head || document.documentElement).appendChild(style);
                }

                if (!window.__framexTvFocusHandler) {
                    window.__framexTvFocusHandler = true;

                    document.addEventListener('focusin', function(event) {
                        document.querySelectorAll('.framex-tv-focused')
                            .forEach(function(el) {
                                el.classList.remove('framex-tv-focused');
                            });

                        const target = event.target;
                        if (!target || !target.closest) return;

                        const card = target.closest(
                            'article, li, [class*="card" i], ' +
                            '[class*="poster" i], [class*="movie" i], ' +
                            '[class*="tile" i]'
                        );

                        if (card && card !== document.body) {
                            card.classList.add('framex-tv-focused');
                        }

                        if (target.scrollIntoView) {
                            target.scrollIntoView({
                                behavior: 'smooth',
                                block: 'nearest',
                                inline: 'nearest'
                            });
                        }
                    }, true);
                }
            })();
            """.trimIndent(),
            null
        )
    }

    private fun closeCustomView() {
        customView?.let { (it.parent as? ViewGroup)?.removeView(it) }
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null

        setContentView(webView)
        hideSystemUi()
        webView.requestFocus()
    }

    private fun hideSystemUi() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (customView != null) {
            closeCustomView()
        } else if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
