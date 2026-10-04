package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip

class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val brightnessPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val brightnessIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var wheelBitmap: Bitmap? = null
    private var brightnessBitmap: Bitmap? = null

    private var wheelCenterX = 0f
    private var wheelCenterY = 0f
    private var wheelRadius = 0f

    private var hue = 0f
    private var saturation = 1f
    private var value = 1f

    private var draggingWheel = false
    private var draggingBrightness = false

    var selectedColor: Int
        get() = Color.HSVToColor(
            floatArrayOf(hue, saturation, value)
        )
        set(color) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)

            hue = hsv[0]
            saturation = hsv[1]
            value = hsv[2]

            invalidate()
        }

    init {
        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.strokeWidth = dp(2f)

        brightnessIndicatorPaint.style = Paint.Style.STROKE
        brightnessIndicatorPaint.strokeWidth = dp(2f)

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int
    ) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        wheelBitmap = null
        brightnessBitmap = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val padding = dp(24f)

        val brightnessHeight = dp(32f)
        val brightnessGap = dp(24f)

        wheelRadius = min(
            (width - padding * 2f) / 2f,
            (height - padding * 2f - brightnessHeight - brightnessGap) / 2f
        )

        wheelCenterX = width / 2f
        wheelCenterY = padding + wheelRadius

        drawColorWheel(canvas)
        drawBrightnessSlider(
            canvas,
            padding,
            height - padding - brightnessHeight,
            width - padding,
            brightnessHeight
        )

        drawWheelIndicator(canvas)
        drawBrightnessIndicator(
            canvas,
            padding,
            height - padding - brightnessHeight,
            width - padding,
            brightnessHeight
        )
    }

    private fun drawColorWheel(canvas: Canvas) {

        val diameter = (wheelRadius * 2f).toInt()

        if (diameter <= 0) return

        if (wheelBitmap == null ||
            wheelBitmap!!.width != diameter ||
            wheelBitmap!!.height != diameter
        ) {
            wheelBitmap = createBitmap(diameter, diameter)

            generateColorWheel(wheelBitmap!!)
        }

        val bitmap = wheelBitmap ?: return

        canvas.drawBitmap(
            bitmap,
            wheelCenterX - wheelRadius,
            wheelCenterY - wheelRadius,
            wheelPaint
        )
    }

    private fun generateColorWheel(bitmap: Bitmap) {

        val pixels = IntArray(bitmap.width * bitmap.height)

        val center = bitmap.width / 2f
        val radius = center

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {

                val dx = x - center
                val dy = y - center

                val distance = sqrt(
                    dx * dx + dy * dy
                )

                val index = y * bitmap.width + x

                if (distance > radius) {
                    pixels[index] = Color.TRANSPARENT
                    continue
                }

                val saturationValue =
                    (distance / radius).coerceIn(0f, 1f)

                var angle = Math.toDegrees(
                    atan2(dy.toDouble(), dx.toDouble())
                ).toFloat()

                angle += 90f

                if (angle < 0f) {
                    angle += 360f
                }

                if (angle >= 360f) {
                    angle -= 360f
                }

                pixels[index] = Color.HSVToColor(
                    floatArrayOf(
                        angle,
                        saturationValue,
                        1f
                    )
                )
            }
        }

        bitmap.setPixels(
            pixels,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )
    }

    private fun drawWheelIndicator(canvas: Canvas) {

        val angleRadians = Math.toRadians(
            (hue - 90f).toDouble()
        )

        val distance = saturation * wheelRadius

        val x = wheelCenterX +
                cos(angleRadians).toFloat() * distance

        val y = wheelCenterY +
                sin(angleRadians).toFloat() * distance

        val outerRadius = dp(10f)

        // Black outer ring
        indicatorPaint.color = Color.BLACK
        indicatorPaint.strokeWidth = dp(3f)

        canvas.drawCircle(
            x,
            y,
            outerRadius,
            indicatorPaint
        )

        // White inner ring
        indicatorPaint.color = Color.WHITE
        indicatorPaint.strokeWidth = dp(2f)

        canvas.drawCircle(
            x,
            y,
            outerRadius,
            indicatorPaint
        )
    }

    private fun drawBrightnessSlider(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        height: Float
    ) {
        val width = right - left

        if (brightnessBitmap == null ||
            brightnessBitmap!!.width != width.toInt() ||
            brightnessBitmap!!.height != height.toInt()
        ) {
            brightnessBitmap = createBitmap(
                width.toInt(),
                height.toInt()
            )

            generateBrightnessSlider(
                brightnessBitmap!!
            )
        }

        val bitmap = brightnessBitmap ?: return

        val rect = RectF(
            left,
            top,
            right,
            top + height
        )

        val radius = height / 2f

        canvas.withClip(
            Path().apply {
                addRoundRect(
                    rect,
                    radius,
                    radius,
                    Path.Direction.CW
                )
            }
        ) {

            // Clip the gradient itself into a rounded rectangle
            drawBitmap(
                bitmap,
                null,
                rect,
                brightnessPaint
            )

        }

        // Optional subtle border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = dp(1f)
            color = Color.WHITE
        }

        canvas.drawRoundRect(
            rect,
            radius,
            radius,
            borderPaint
        )
    }

    private fun generateBrightnessSlider(bitmap: Bitmap) {

        val pixels = IntArray(
            bitmap.width * bitmap.height
        )

        val hsv = floatArrayOf(
            hue,
            saturation,
            0f
        )

        for (x in 0 until bitmap.width) {

            val brightness =
                x.toFloat() /
                        (bitmap.width - 1).coerceAtLeast(1)

            hsv[2] = brightness

            val color = Color.HSVToColor(hsv)

            for (y in 0 until bitmap.height) {
                pixels[y * bitmap.width + x] = color
            }
        }

        bitmap.setPixels(
            pixels,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )
    }

    private fun drawBrightnessIndicator(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        height: Float
    ) {

        val x =
            left + value * (right - left)

        val centerY =
            top + height / 2f

        val radius =
            height / 2f + dp(2f)

        brightnessIndicatorPaint.color = Color.BLACK
        brightnessIndicatorPaint.strokeWidth = dp(3f)

        canvas.drawCircle(
            x,
            centerY,
            radius,
            brightnessIndicatorPaint
        )

        brightnessIndicatorPaint.color = Color.WHITE
        brightnessIndicatorPaint.strokeWidth = dp(2f)

        canvas.drawCircle(
            x,
            centerY,
            radius,
            brightnessIndicatorPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                val dx =
                    event.x - wheelCenterX

                val dy =
                    event.y - wheelCenterY

                val distance =
                    sqrt(dx * dx + dy * dy)

                val brightnessTop =
                    height -
                            dp(16f) -
                            dp(32f)

                if (distance <= wheelRadius) {

                    draggingWheel = true
                    draggingBrightness = false

                    updateWheelSelection(
                        event.x,
                        event.y
                    )

                    return true
                }

                if (event.y >= brightnessTop &&
                    event.y <= brightnessTop + dp(32f)
                ) {

                    draggingBrightness = true
                    draggingWheel = false

                    updateBrightness(
                        event.x
                    )

                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {

                if (draggingWheel) {

                    updateWheelSelection(
                        event.x,
                        event.y
                    )

                    return true
                }

                if (draggingBrightness) {

                    updateBrightness(
                        event.x
                    )

                    return true
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                draggingWheel = false
                draggingBrightness = false

                performClick()

                return true
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateWheelSelection(
        x: Float,
        y: Float
    ) {

        val dx =
            x - wheelCenterX

        val dy =
            y - wheelCenterY

        val distance =
            sqrt(dx * dx + dy * dy)

        saturation =
            (distance / wheelRadius)
                .coerceIn(0f, 1f)

        var angle =
            Math.toDegrees(
                atan2(
                    dy.toDouble(),
                    dx.toDouble()
                )
            ).toFloat()

        angle += 90f

        if (angle < 0f) {
            angle += 360f
        }

        if (angle >= 360f) {
            angle -= 360f
        }

        hue = angle

        regenerateBrightnessSlider()

        invalidate()
    }

    private fun updateBrightness(
        x: Float
    ) {

        val left = dp(16f)
        val right = width - dp(16f)

        value =
            ((x - left) / (right - left))
                .coerceIn(0f, 1f)

        invalidate()
    }

    private fun regenerateBrightnessSlider() {
        brightnessBitmap = null
    }

    private fun dp(value: Float): Float {
        return value *
                resources.displayMetrics.density
    }
}