package com.example.music.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.max

/**
 * A modern music player seek bar with:
 *  - Thin track (3 dp) that grows to 5 dp while the user scrubs
 *  - Thumb that is a subtle 4 dp dot at rest, expands to 14 dp on touch
 *  - Soft glow halo behind the thumb that uses the current accent colour
 *  - Gradient-fill progress (accent → white) so it always pops against the background
 *  - All transitions are smooth 200–250 ms animations
 *  - Overshoots on thumb-appear for a springy feel
 *  - Touch target is always 48 dp tall regardless of the thin visual track
 */
class MusicSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── public state ─────────────────────────────────────────────────────────

    /** Progress in the range 0f..1f */
    var progress: Float = 0f
        private set

    /** Call this from your ViewModel observer. Ignored while the user is scrubbing. */
    fun setProgress(value: Float, animate: Boolean = false) {
        if (isTouching) return
        val clamped = value.coerceIn(0f, 1f)
        if (animate && clamped != progress) {
            ValueAnimator.ofFloat(progress, clamped).apply {
                duration = 180
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            progress = clamped
            invalidate()
        }
    }

    /** Accent colour – usually extracted from the album art via Palette. */
    var accentColor: Int = Color.WHITE
        set(value) {
            field = value
            rebuildProgressGradient()
            glowPaint.color = value
            invalidate()
        }

    // ── callbacks ─────────────────────────────────────────────────────────────

    /** Called every time progress changes (both from user touch and [setProgress]). */
    var onProgressChanged: ((progress: Float, fromUser: Boolean) -> Unit)? = null

    /** Called when the user first touches the bar. */
    var onStartTracking: (() -> Unit)? = null

    /** Called when the user lifts their finger. Final progress value is provided. */
    var onStopTracking: ((progress: Float) -> Unit)? = null

    // ── dimensions ────────────────────────────────────────────────────────────

    private val trackHeightIdle    = dp(3f)
    private val trackHeightActive  = dp(5f)
    private val thumbRadiusIdle    = dp(4f)   // small always-visible dot
    private val thumbRadiusActive  = dp(10f)  // expanded while scrubbing
    private val glowRadiusActive   = dp(20f)
    private val cornerRadius       = dp(8f)
    private val minTouchTarget     = dp(48f)

    // ── animated values ───────────────────────────────────────────────────────

    private var animTrackH  = trackHeightIdle
    private var animThumbR  = thumbRadiusIdle
    private var animGlowR   = 0f
    private var animGlowA   = 0f   // glow alpha 0..255

    // ── state ─────────────────────────────────────────────────────────────────

    private var isTouching = false

    // ── reusable RectFs ───────────────────────────────────────────────────────

    private val bgRect   = RectF()
    private val fillRect = RectF()

    // ── paints ────────────────────────────────────────────────────────────────

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
        style = Paint.Style.FILL
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    /** Outer glow rendered in a separate software layer so BlurMaskFilter works. */
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(dp(14f), BlurMaskFilter.Blur.NORMAL)
    }

    // ── animators ─────────────────────────────────────────────────────────────

    private var trackAnim : ValueAnimator? = null
    private var thumbAnim : ValueAnimator? = null
    private var glowAnim  : ValueAnimator? = null

    // ── init ──────────────────────────────────────────────────────────────────

    init {
        // BlurMaskFilter requires software rendering
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    // ── gradient helper ───────────────────────────────────────────────────────

    private fun rebuildProgressGradient() {
        if (width == 0) return
        val left  = paddingLeft.toFloat()
        val right = (width - paddingRight).toFloat()
        fillPaint.shader = LinearGradient(
            left, 0f, right, 0f,
            intArrayOf(accentColor, blendWithWhite(accentColor, 0.6f)),
            null,
            Shader.TileMode.CLAMP
        )
    }

    private fun blendWithWhite(color: Int, ratio: Float): Int {
        val r = Color.red(color)   + ((255 - Color.red(color))   * ratio).toInt()
        val g = Color.green(color) + ((255 - Color.green(color)) * ratio).toInt()
        val b = Color.blue(color)  + ((255 - Color.blue(color))  * ratio).toInt()
        return Color.rgb(r.coerceIn(0,255), g.coerceIn(0,255), b.coerceIn(0,255))
    }

    // ── measure ───────────────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = resolveSize(minTouchTarget.toInt(), heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildProgressGradient()
    }

    // ── draw ──────────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        val left   = paddingLeft.toFloat()
        val right  = (width - paddingRight).toFloat()
        val cy     = height / 2f
        val half   = animTrackH / 2f
        val thumbX = left + (right - left) * progress

        // 1 – track background
        bgRect.set(left, cy - half, right, cy + half)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)

        // 2 – progress fill (gradient)
        if (progress > 0f) {
            fillRect.set(left, cy - half, thumbX, cy + half)
            canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, fillPaint)
        }

        // 3 – glow halo (only while alpha > 0)
        if (animGlowA > 0f) {
            glowPaint.alpha = animGlowA.toInt().coerceIn(0, 180)
            canvas.drawCircle(thumbX, cy, animGlowR, glowPaint)
        }

        // 4 – thumb dot
        if (animThumbR > 0f) {
            canvas.drawCircle(thumbX, cy, animThumbR, thumbPaint)
        }
    }

    // ── touch ─────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val left  = paddingLeft.toFloat()
        val right = (width - paddingRight).toFloat()

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                parent?.requestDisallowInterceptTouchEvent(true)
                onStartTracking?.invoke()
                animateToActive()
                updateProgressFromX(event.x, left, right)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                updateProgressFromX(event.x, left, right)
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                parent?.requestDisallowInterceptTouchEvent(false)
                onStopTracking?.invoke(progress)
                animateToIdle()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun updateProgressFromX(x: Float, left: Float, right: Float) {
        val span = max(1f, right - left)
        progress = ((x - left) / span).coerceIn(0f, 1f)
        onProgressChanged?.invoke(progress, true)
        invalidate()
    }

    // ── animations ────────────────────────────────────────────────────────────

    private fun animateToActive() {
        animateTrack(trackHeightActive)
        animateThumb(thumbRadiusActive, overshoot = true)
        animateGlow(glowRadiusActive, targetAlpha = 180f)
    }

    private fun animateToIdle() {
        animateTrack(trackHeightIdle)
        animateThumb(thumbRadiusIdle, overshoot = false)
        animateGlow(0f, targetAlpha = 0f)
    }

    private fun animateTrack(target: Float) {
        trackAnim?.cancel()
        trackAnim = ValueAnimator.ofFloat(animTrackH, target).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animTrackH = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun animateThumb(target: Float, overshoot: Boolean) {
        thumbAnim?.cancel()
        thumbAnim = ValueAnimator.ofFloat(animThumbR, target).apply {
            duration = if (overshoot) 280 else 200
            interpolator = if (overshoot) OvershootInterpolator(2.5f)
                           else DecelerateInterpolator()
            addUpdateListener {
                animThumbR = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun animateGlow(targetRadius: Float, targetAlpha: Float) {
        glowAnim?.cancel()
        val startRadius = animGlowR
        val startAlpha  = animGlowA
        glowAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                val t = va.animatedFraction
                animGlowR = startRadius + (targetRadius - startRadius) * t
                animGlowA = startAlpha  + (targetAlpha  - startAlpha)  * t
                invalidate()
            }
            start()
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun dp(value: Float): Float =
        value * context.resources.displayMetrics.density
}
