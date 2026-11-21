package com.example.bugs_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.example.bugs_android.R

class RulesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_rules,
            container,
            false
        )

        val webView = view.findViewById<WebView>(R.id.webViewRules)
        val html = resources.openRawResource(R.raw.rules).bufferedReader().use { it.readText() }
        webView.loadData(html, "text/html", "UTF-8")

        return view

    }
}