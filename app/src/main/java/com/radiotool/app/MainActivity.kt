package com.radiotool.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var downloadId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        setupWebView()
        setupDownloadReceiver()

        // Load the web app
        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(true)
            setSupportMultipleWindows(false)
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            setGeolocationEnabled(true)

            // Media playback
            mediaPlaybackRequiresUserGesture = false
        }

        // Enable hardware acceleration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(false)
        }

        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false

                // Handle external URLs
                return if (url.startsWith("http://") || url.startsWith("https://")) {
                    // Open in WebView for HTTP/HTTPS
                    false
                } else if (url.startsWith("intent://")) {
                    // Handle app intents
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        startActivity(intent)
                    } catch (e: Exception) {
                        // Intent not parseable
                    }
                    true
                } else {
                    false
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    android.util.Log.d("RadioDBTool", "${it.message()} -- Line ${it.lineNumber()}")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    private fun setupDownloadReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        // Intercept download requests from JavaScript
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            handleDownload(url, contentDisposition, mimeType)
        }
    }

    private fun handleDownload(url: String, contentDisposition: String?, mimeType: String?) {
        try {
            val fileName = extractFileName(contentDisposition, url)

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("Downloading $fileName")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType(mimeType ?: "application/octet-stream")
            }

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractFileName(contentDisposition: String?, url: String): String {
        // Try to extract filename from content-disposition header
        if (!contentDisposition.isNullOrEmpty()) {
            val regex = Regex("filename=\"?([^\";\\n]+)\"?")
            regex.find(contentDisposition)?.let { match ->
                return URLDecoder.decode(match.groupValues[1], "UTF-8")
            }
        }

        // Fallback: extract from URL
        val path = url.substringBefore("?")
        return path.substringAfterLast("/").ifEmpty { "download" }
    }

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                // Download completed
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                "Download completed",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        DownloadManager.STATUS_FAILED -> {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                "Download failed",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                cursor.close()
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(downloadCompleteReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        webView.destroy()
        super.onDestroy()
    }
}
