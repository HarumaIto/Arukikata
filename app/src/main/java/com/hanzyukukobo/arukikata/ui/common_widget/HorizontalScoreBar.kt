package com.hanzyukukobo.arukikata.ui.common_widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.util.AttributeSet
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.HorizontalScoreBarBinding

class HorizontalScoreBar constructor(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.horizontal_score_bar, this)
    }

    fun setMinMaxValue(min: String, max: String) {
        findViewById<TextView>(R.id.minValueText).apply {
            this.text = min
        }
        findViewById<TextView>(R.id.minValueText).apply {
            this.text = max
        }
    }

    fun setProgressValue(value: Int) {
        findViewById<ProgressBar>(R.id.scoreValueBar).apply {
            this.progress = value
        }
    }
}
