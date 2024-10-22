package com.example.userkabirstore.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

import androidx.appcompat.app.AppCompatActivity

import com.example.userkabirstore.R


class PrivacyPolicyActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        val webView = findViewById<WebView>(R.id.privacyPolicyWebView)

        // Enable JavaScript if necessary (Optional)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Check if the URL is an email link
                if (url != null && url.startsWith("mailto:")) {
                    // Open the email client
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    return true // Indicate that we've handled the URL
                }
                // For other URLs, let the WebView handle them
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }

        // Load the HTML file from the assets folder
        webView.loadUrl("file:///android_asset/privacy_policy.html")
    }
}