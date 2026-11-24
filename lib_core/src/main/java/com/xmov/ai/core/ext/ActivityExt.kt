package com.xmov.ai.core.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager


@SuppressLint("ServiceCast", "ClickableViewAccessibility")
fun Activity.hideKeyboardOnOutsideTouch(rootView: View) {
    rootView.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { view ->
                val touchArea = Rect().apply { view.getGlobalVisibleRect(this) }
                if (!touchArea.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        false
    }
}