package com.example.signiai

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val left = width * 0.2f
        val top = height * 0.3f
        val right = width * 0.8f
        val bottom = height * 0.7f

        canvas.drawRect(left, top, right, bottom, paint)
    }
}