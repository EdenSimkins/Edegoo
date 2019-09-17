package com.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment

        val view = inflater?.inflate(R.layout.fragment_webview, container, false)

        val address = "http://192.168.4.1:8000"
        val webV: WebView = view.findViewById(R.id.webView)
        webV.settings.javaScriptEnabled = true
        webV.setWebViewClient(WebViewClient())
        webV.loadUrl(address)

        return view
    }
}