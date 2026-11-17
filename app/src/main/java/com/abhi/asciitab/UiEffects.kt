package com.abhi.asciitab

import android.view.View
import android.view.animation.OvershootInterpolator

object UiEffects {
    fun applySpring(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                    if (android.os.Build.VERSION.SDK_INT >= 27) v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setInterpolator(OvershootInterpolator(2f)).setDuration(180).start()
                }
            }
            false
        }
    }
}
