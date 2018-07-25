package com.moovel.gpsrecorderplayer.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import com.moovel.gpsrecorderplayer.utils.dpToPx

class BottomDrawer @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0) :
        RelativeLayout(ctx, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val STATE_COLLAPSED = 0
        const val STATE_EXPANDED = 1
        private const val CLICK_TIME = 500
        private const val CLICK_DISTANCE = 10
        private const val ANIMATION_DURATION = 200L
    }

    private var heightListener: ((height: Float) -> Unit)? = null
    private var clickListener: (() -> Unit)? = null
    private var state: Int = STATE_COLLAPSED
        set(value) {
            if (field != value) {
                field = value
                animate(STATE_EXPANDED == value)
            }
        }

    private var animator: ValueAnimator? = null
    private var yRawStart = 0f
    private var yStart = 0f
    private var xStart = 0f
    private var downAt = 0L

    init {
        setOnTouchListener { _, event ->
            val wasExpanded = state == STATE_EXPANDED
            if (event.action == ACTION_UP && wasExpanded) state = STATE_COLLAPSED
            wasExpanded
        }
    }

    fun isOpen() = state == STATE_EXPANDED

    fun open() {
        state = STATE_EXPANDED
    }

    fun close() {
        state = STATE_COLLAPSED
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        animate(STATE_EXPANDED == state, 0)
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        getChild().setOnTouchListener { _, event -> onTouchChild(event) }
    }

    private fun onTouchChild(event: MotionEvent): Boolean {
        val view = getChild()
        val height = measuredHeight.toFloat()
        val childHeight = view.measuredHeight.toFloat()
        val rawY = event.rawY
        when (event.action) {
            ACTION_DOWN -> {
                yRawStart = rawY
                yStart = view.y
                xStart = view.x
                downAt = System.currentTimeMillis()
            }
            ACTION_UP -> when {
                isClick(xStart, view.x, yStart, view.y, downAt) -> clickListener?.invoke()
                else -> animate(height - childHeight + childHeight / 3 > view.y)
            }
            ACTION_MOVE -> updateUI(Math.max(Math.min(yStart + (rawY - yRawStart), height), (height - childHeight)))
        }
        return true
    }

    private fun colorBackground(factor: Float) {
        setBackgroundColor(adjustAlpha(Color.BLACK, factor))
    }

    private fun adjustAlpha(@ColorInt color: Int, factor: Float) =
            Color.argb(Math.round(Color.alpha(color) * factor), Color.red(color), Color.green(color), Color.blue(color))

    private fun isClick(startX: Float, endX: Float, startY: Float, endY: Float, downAt: Long): Boolean =
            !(Math.abs(startX - endX) > CLICK_DISTANCE.dpToPx() || Math.abs(startY - endY) > CLICK_DISTANCE.dpToPx())
                    && (System.currentTimeMillis() - downAt) < CLICK_TIME

    private fun animate(up: Boolean, animDuration: Long = ANIMATION_DURATION) {
        val view = getChild()
        val toY = if (up) (measuredHeight - view.measuredHeight) else measuredHeight
        animator?.cancel()
        animator = ValueAnimator.ofFloat(view.y, toY.toFloat()).apply {
            interpolator = if (up) AccelerateDecelerateInterpolator() else DecelerateInterpolator()
            duration = animDuration
            addUpdateListener { updateUI(it.animatedValue as Float) }
            start()
        }
    }

    private fun updateUI(y: Float) {
        val view = getChild()
        val viewHeight = view.measuredHeight.toFloat()
        val currentHeight = measuredHeight.toFloat() - y
        view.y = y
        colorBackground(currentHeight / viewHeight / 2)
        heightListener?.invoke(currentHeight)
    }

    private fun getChild(): View {
        if (childCount != 1) throw IllegalArgumentException("BottomDrawer only allows exactly one child")
        return getChildAt(0)
    }
}
