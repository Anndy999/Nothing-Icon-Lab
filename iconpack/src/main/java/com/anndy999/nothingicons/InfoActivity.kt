package com.anndy999.nothingicons

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class InfoActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this)
        view.text = getString(R.string.info_body)
        view.setPadding(48, 48, 48, 48)
        setContentView(view)
    }
}
