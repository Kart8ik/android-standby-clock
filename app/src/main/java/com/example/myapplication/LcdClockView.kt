package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import java.util.*
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A highly realistic LCD bedside clock view.
 * Strictly follows the 430x190 reference coordinate system.
 */
class LcdClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val designWidth = 430f
    private val designHeight = 190f

    private var colorBg = AppSettings.getColorBg(context)
    private var colorActive = AppSettings.getColorActive(context)
    private var colorInactive = AppSettings.getColorInactive(context)
    private val colorSegmentEdge = Color.argb(45, 0, 0, 0)

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorActive
        style = Paint.Style.FILL
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorInactive
        style = Paint.Style.FILL
    }

    private val segmentEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorSegmentEdge
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val pmIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textScaleX = 1.8f
    }

    private val stableRandom = Random(42)
    private var calendar = Calendar.getInstance()
    private var is24HourFormat = AppSettings.is24HourFormat(context)
    private var tempUnit = AppSettings.getTempUnit(context)
    private var weatherState: WeatherState? = null

    private var offsetX = 0f
    private var offsetY = 0f
    private var lastShiftTime = 0L

    private val twoSegmentLookup = intArrayOf(0b000, 0b011)
    private val sevenSegmentLookup = intArrayOf(
        0b00111111, 0b00000110, 0b01011011, 0b01001111, 0b01100110,
        0b01101101, 0b01111101, 0b00000111, 0b01111111, 0b01100111
    )

    private val fourteenSegmentLookup = mapOf(
        'A' to 0xF7, 'B' to 0x128F, 'C' to 0x39, 'D' to 0x120F, 'E' to 0xF9,
        'F' to 0xF1, 'G' to 0xBD, 'H' to 0xF6, 'I' to 0x1209, 'J' to 0x1E,
        'K' to 0x2470, 'L' to 0x38, 'M' to 0x536, 'N' to 0x2136, 'O' to 0x3F,
        'P' to 0xF3, 'Q' to 0x203F, 'R' to 0x20F3, 'S' to 0xED, 'T' to 0x1201,
        'U' to 0x3E, 'V' to 0xC30, 'W' to 0x2836, 'X' to 0x2D00, 'Y' to 0x1500, 'Z' to 0xC09
    )

    companion object {
        val PREVIEW_MOCK_WEATHER = WeatherState(
            temperature = 23f,
            condition = WeatherCondition.PARTLY_CLOUDY,
            isDay = true
        )
    }

    fun updateTime() {
        calendar.timeInMillis = System.currentTimeMillis()
        is24HourFormat = AppSettings.is24HourFormat(context)
        tempUnit = overrideTempUnit ?: AppSettings.getTempUnit(context)
        refreshColors()

        val now = System.currentTimeMillis()
        if (now - lastShiftTime > 60000) {
            offsetX = (stableRandom.nextFloat() * 8 - 4)
            offsetY = (stableRandom.nextFloat() * 6 - 3)
            lastShiftTime = now
        }
        invalidate()
    }

    private var overrideColorBg: Int? = null
    private var overrideColorActive: Int? = null
    private var overrideColorInactive: Int? = null
    private var overrideTempUnit: String? = null

    fun setOverrideParams(
        bg: Int? = null,
        active: Int? = null,
        inactive: Int? = null,
        tempUnit: String? = null
    ) {
        this.overrideColorBg = bg
        this.overrideColorActive = active
        this.overrideColorInactive = inactive
        this.overrideTempUnit = tempUnit
        refreshColors()
    }

    fun setOverrideColors(bg: Int? = null, active: Int? = null, inactive: Int? = null) {
        setOverrideParams(
            bg = bg,
            active = active,
            inactive = inactive,
            tempUnit = this.overrideTempUnit
        )
    }

    fun refreshColors() {
        colorBg = overrideColorBg ?: AppSettings.getColorBg(context)
        colorActive = overrideColorActive ?: AppSettings.getColorActive(context)
        colorInactive = overrideColorInactive ?: AppSettings.getColorInactive(context)
        activePaint.color = colorActive
        inactivePaint.color = colorInactive
        invalidate()
    }

    fun setWeather(state: WeatherState?) {
        this.weatherState = state
        postInvalidate()
    }

    fun getWeatherState(): WeatherState? = weatherState

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(colorBg)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val scale = min(w / designWidth, h / designHeight)
        
        canvas.save()
        canvas.translate((w - designWidth * scale) / 2f + offsetX, (h - designHeight * scale) / 2f + offsetY)
        canvas.scale(scale, scale)

        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val displayHour = if (is24HourFormat) {
            hour24
        } else {
            val h12 = calendar.get(Calendar.HOUR)
            if (h12 == 0) 12 else h12
        }
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        drawWeather(canvas, weatherState)
        drawTopRow(canvas)
        drawMainTime(canvas, displayHour, minute, second)

        canvas.restore()
    }

    private fun drawWeather(canvas: Canvas, state: WeatherState?) {
        if (state == null) return
        val unit = tempUnit
        val tempValue = if (unit == "F") (state.temperature * 9 / 5) + 32 else state.temperature
        val displayTemp = Math.round(tempValue)

        val x = 28f
        val y = 14f
        drawTemperature(canvas, displayTemp, unit, x + 63, y)
        drawWeatherIcon(canvas, state.condition, state.isDay, x, y)
    }

    private fun drawTemperature(canvas: Canvas, temp: Int, unit: String, x: Float, y: Float) {
        val dw = 17f
        val dh = 34f
        val ds = 4f
        var curX = x

        val t = Math.abs(temp) % 100
        draw7SegDigit(canvas, t / 10, curX, y, dw, dh)
        curX += dw + ds
        draw7SegDigit(canvas, t % 10, curX, y, dw, dh)
        curX += dw + ds

        // Degree dot
        val dotSize = 4f
        canvas.drawOval(curX, y + 2f, curX + dotSize, y + 2f + dotSize, activePaint)
        canvas.drawOval(curX, y + 2f, curX + dotSize, y + 2f + dotSize, segmentEdgePaint)
        curX += dotSize + 4f

        draw14SegCharacter(canvas, unit[0], curX, y, 8f, 17f)
    }

    private fun drawWeatherIcon(canvas: Canvas, condition: WeatherCondition, isDay: Boolean, x: Float, y: Float) {
        val resId = when (condition) {
            WeatherCondition.CLEAR -> if (isDay) R.drawable.day_clear else R.drawable.night_clear
            WeatherCondition.PARTLY_CLOUDY -> if (isDay) R.drawable.day_partly_cloudy else R.drawable.night_partly_cloudy
            WeatherCondition.CLOUDY -> R.drawable.cloudy
            WeatherCondition.RAIN -> R.drawable.rain
            WeatherCondition.HEAVY_RAIN -> R.drawable.heavy_rain
            WeatherCondition.THUNDERSTORM -> R.drawable.thunderstorm
            WeatherCondition.SNOW -> R.drawable.snow
            WeatherCondition.FOGGY -> R.drawable.fog
            else -> null
        }

        resId?.let { id ->
            val drawable = AppCompatResources.getDrawable(context, id)
            drawable?.let {
                val size = 34
                it.setBounds(x.toInt(), y.toInt(), (x + size).toInt(), (y + size).toInt())
                it.setTint(colorActive)
                it.draw(canvas)
            }
        }
    }

    private fun drawTopRow(canvas: Canvas) {
        val dayNames = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        val dayStr = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        
        var wx = 243f
        val wy = 14f
        val ww = 17f
        val wh = 34f
        val gap = 4f
        for (char in dayStr) {
            draw14SegCharacter(canvas, char, wx, wy, ww, wh)
            wx += ww + gap
        }

        val month = calendar.get(Calendar.MONTH) + 1
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        drawSmallDate(canvas, month, date, 315f, 14f, 0.38f)
    }

    private fun drawMainTime(canvas: Canvas, hour: Int, min: Int, sec: Int) {
        if (!is24HourFormat) {
            val isPm = calendar.get(Calendar.AM_PM) == Calendar.PM
            pmIndicatorPaint.color = if (isPm) colorActive else colorInactive
            canvas.drawText("PM", 45f, 172f, pmIndicatorPaint)
        }

        var x = 50f
        val y = 83f
        val dw = 50f
        val dh = 108f
        val ds = 9f
        val cw = 16f

        if (is24HourFormat) {
            draw7SegDigit(canvas, hour / 10, x, y, dw, dh)
        } else {
            draw2SegDigit(canvas, hour / 10, x, y, dw, dh)
        }
        x += dw + ds

        draw7SegDigit(canvas, hour % 10, x, y, dw, dh)
        x += dw + ds

        drawColon(canvas, x, y - 8, cw, dh, false)
        x += cw + ds

        draw7SegDigit(canvas, min / 10, x, y, dw, dh)
        x += dw + ds

        draw7SegDigit(canvas, min % 10, x, y, dw, dh)
        x += dw + 3 * ds

        val sdw = 36f
        val sdh = 78f
        draw7SegDigit(canvas, sec / 10, x, y + 21f, sdw, sdh)
        x += sdw + 6f
        draw7SegDigit(canvas, sec % 10, x, y + 21f, sdw, sdh)
    }

    private fun drawSmallDate(canvas: Canvas, month: Int, date: Int, xStart: Float, yStart: Float, scale: Float) {
        var x = xStart
        val dw = 48f * scale
        val dh = 83f * scale
        val ds = 8f * scale

        if (month >= 10) {
            draw2SegDigit(canvas, month / 10, x, yStart, dw, dh)
            x += dw + ds
        }
        draw7SegDigit(canvas, month % 10, x, yStart, dw, dh)
        x += dw + ds

        val dashW = dw * 0.3f
        val dashH = dh * 0.1f
        drawLcdSegment(canvas, x, yStart + (dh - dashH) / 2f, dashW, dashH, 0, 0.2f, true)
        x += dashW + ds

        draw7SegDigit(canvas, date / 10, x, yStart, dw, dh)
        x += dw + ds

        draw7SegDigit(canvas, date % 10, x, yStart, dw, dh)
    }

    private fun draw2SegDigit(canvas: Canvas, digit: Int, x: Float, y: Float, w: Float, h: Float) {
        val data = twoSegmentLookup[digit % 10]
        val bits = BooleanArray(2) { i -> (data shr i) and 1 == 1 }
        val segmentScale = 3f
        val spacingRatio = 0.2f
        val sw = min(w / 5f, h / 9f)
        val sl = segmentScale * sw

        drawSegment(canvas, x + sl + sw, y + sw, sw, sl, 1, spacingRatio, bits[0]) // b
        drawSegment(canvas, x + sl + sw, y + sl + sw * 2, sw, sl, 1, spacingRatio, bits[1]) // c
    }

    private fun draw7SegDigit(canvas: Canvas, digit: Int, x: Float, y: Float, w: Float, h: Float) {
        val data = sevenSegmentLookup[digit % 10]
        val bits = BooleanArray(7) { i -> (data shr i) and 1 == 1 }
        val segmentScale = 3f
        val spacingRatio = 0.2f
        val sw = min(w / 5f, h / 9f)
        val sl = segmentScale * sw

        drawSegment(canvas, x + sw, y, sl, sw, 0, spacingRatio, bits[0]) // a
        drawSegment(canvas, x + sl + sw, y + sw, sw, sl, 1, spacingRatio, bits[1]) // b
        drawSegment(canvas, x + sl + sw, y + sl + sw * 2, sw, sl, 1, spacingRatio, bits[2]) // c
        drawSegment(canvas, x + sw, y + sl + sw + sl + sw, sl, sw, 0, spacingRatio, bits[3]) // d
        drawSegment(canvas, x, y + sl + sw * 2, sw, sl, 1, spacingRatio, bits[4]) // e
        drawSegment(canvas, x, y + sw, sw, sl, 1, spacingRatio, bits[5]) // f
        drawSegment(canvas, x + sw, y + sl + sw, sl, sw, 0, spacingRatio, bits[6]) // g
    }

    private fun draw14SegCharacter(canvas: Canvas, char: Char, x: Float, y: Float, w: Float, h: Float) {
        val data = fourteenSegmentLookup[char.uppercaseChar()] ?: 0
        val bits = BooleanArray(14) { i -> (data shr i) and 1 == 1 }
        
        val segmentScale = 4f
        val spacingRatio = 0.2f
        val scaleWidth = 6f
        val scaleHeight = 11f

        val sw = min(w / scaleWidth, h / scaleHeight)
        val sl = segmentScale * sw
        val fullWidth = sw * scaleWidth
        val quarterGapWidth = (fullWidth - sw * 3) / 2f

        drawSegment(canvas, x + sw, y, sl, sw, 0, spacingRatio, bits[0]) // a
        drawSegment(canvas, x + sl + sw, y + sw, sw, sl, 1, spacingRatio, bits[1]) // b
        drawSegment(canvas, x + sl + sw, y + sl + sw * 2, sw, sl, 1, spacingRatio, bits[2]) // c
        drawSegment(canvas, x + sw, y + sl + sw + sl + sw, sl, sw, 0, spacingRatio, bits[3]) // d
        drawSegment(canvas, x, y + sl + sw * 2, sw, sl, 1, spacingRatio, bits[4]) // e
        drawSegment(canvas, x, y + sw, sw, sl, 1, spacingRatio, bits[5]) // f
        drawSegment(canvas, x + sw, y + sl + sw, sl / 2f, sw, 2, spacingRatio, bits[6]) // g1
        drawSegment(canvas, x + quarterGapWidth + sw * 2, y + sl + sw, sl / 2f, sw, 2, spacingRatio, bits[7]) // g2
        drawSegment(canvas, x + sw, y + sw, quarterGapWidth, sl, 4, spacingRatio, bits[8]) // h
        drawSegment(canvas, x + quarterGapWidth + sw, y + sw + sw / 2, sw, sl - sw / 2, 3, spacingRatio, bits[9]) // i
        drawSegment(canvas, x + quarterGapWidth + sw * 2, y + sw, quarterGapWidth, sl, 5, spacingRatio, bits[10]) // j
        drawSegment(canvas, x + sw, y + sl + sw * 2, quarterGapWidth, sl, 5, spacingRatio, bits[11]) // k
        drawSegment(canvas, x + quarterGapWidth + sw, y + sl + sw * 2, sw, sl - sw / 2, 3, spacingRatio, bits[12]) // l
        drawSegment(canvas, x + quarterGapWidth + sw * 2, y + sl + sw * 2, quarterGapWidth, sl, 4, spacingRatio, bits[13]) // m
    }

    private fun drawSegment(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, type: Int, spacingRatio: Float, active: Boolean) {
        val paint = if (active) activePaint else inactivePaint
        val path = when (type) {
            0 -> createHorizontalPath(x, y, w, h, spacingRatio)
            1 -> createVerticalPath(x, y, w, h, spacingRatio)
            2 -> createSmallHorizontalPath(x, y, w, h, spacingRatio)
            3 -> createSmallVerticalPath(x, y, w, h, spacingRatio)
            4 -> createBackSlashPath(x, y, w, h, spacingRatio)
            else -> createForwardSlashPath(x, y, w, h, spacingRatio)
        }
        canvas.drawPath(path, paint)
        if (active) canvas.drawPath(path, segmentEdgePaint)
    }

    private fun createHorizontalPath(x: Float, y: Float, w: Float, h: Float, spacingRatio: Float): Path {
        val radius = h / 2f; val cx = x + w / 2; val cy = y + h / 2; val sp = radius * spacingRatio
        return Path().apply {
            moveTo(cx, cy + radius - sp); lineTo(cx - w / 2, cy + radius - sp)
            lineTo(cx - w / 2 - radius + sp, cy); lineTo(cx - w / 2, cy - radius + sp)
            lineTo(cx + w / 2, cy - radius + sp); lineTo(cx + w / 2 + radius - sp, cy)
            lineTo(cx + w / 2, cy + radius - sp); close()
        }
    }

    private fun createVerticalPath(x: Float, y: Float, w: Float, h: Float, spacingRatio: Float): Path {
        val radius = w / 2f; val cx = x + w / 2; val cy = y + h / 2; val sp = radius * spacingRatio
        return Path().apply {
            moveTo(cx, cy + radius + h / 2 - sp); lineTo(cx - radius + sp, cy + h / 2)
            lineTo(cx - radius + sp, cy - h / 2); lineTo(cx, cy - radius - h / 2 + sp)
            lineTo(cx + radius - sp, cy - h / 2); lineTo(cx + radius - sp, cy + h / 2); close()
        }
    }

    private fun createSmallHorizontalPath(x: Float, y: Float, w: Float, h: Float, spacingRatio: Float): Path {
        val radius = h / 2f; val cx = x + w / 2; val cy = y + h / 2; val sp = radius * spacingRatio
        return Path().apply {
            moveTo(cx - w / 2, cy + radius - sp); lineTo(cx - w / 2 - radius + sp, cy); lineTo(cx - w / 2, cy - radius + sp)
            lineTo(cx + w / 2 - radius, cy - radius + sp); lineTo(cx + w / 2 - sp, cy); lineTo(cx + w / 2 - radius, cy + radius - sp); close()
        }
    }

    private fun createSmallVerticalPath(x: Float, y: Float, w: Float, h: Float, spacingRatio: Float): Path {
        val radius = w / 2f; val cx = x + w / 2; val cy = y + h / 2; val sp = radius * spacingRatio
        return Path().apply {
            moveTo(cx, cy + radius + h / 2 - sp); lineTo(cx - radius + sp, cy + h / 2)
            lineTo(cx - radius + sp, cy - h / 2); lineTo(cx, cy - radius - h / 2 + sp)
            lineTo(cx + radius - sp, cy - h / 2); lineTo(cx + radius - sp, cy + h / 2); close()
        }
    }

    private fun createBackSlashPath(x: Float, y: Float, w: Float, h: Float, spacingRatio: Float): Path {
        val thickness = 3f; val dw = sqrt(thickness.toDouble().pow(2.0) / 2.0).toFloat() * 2f; val sp = (dw / 4f) * spacingRatio
        return Path().apply {
            moveTo(x + sp, y + sp); lineTo(x + w - sp, y + h - dw); lineTo(x + w - sp, y + h - sp); lineTo(x + sp, y + dw); close()
        }
    }

    private fun createForwardSlashPath(x: Float, y: Float, w: Float, h: Float, spacingRatio: Float): Path {
        val thickness = 3f; val dw = sqrt(thickness.toDouble().pow(2.0) / 2.0).toFloat() * 2f; val sp = (dw / 4f) * spacingRatio
        return Path().apply {
            moveTo(x + sp, y + h - sp); lineTo(x + sp, y + h - dw); lineTo(x + w - sp, y + sp); lineTo(x + w - sp, y + dw); close()
        }
    }

    private fun drawLcdSegment(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, type: Int, spacingRatio: Float, active: Boolean) {
        val paint = if (active) activePaint else inactivePaint
        val path = if (type == 0) createHorizontalPath(x, y, w, h, spacingRatio) else createVerticalPath(x, y, w, h, spacingRatio)
        canvas.drawPath(path, paint)
        if (active) canvas.drawPath(path, segmentEdgePaint)
    }

    private fun drawColon(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, narrower: Boolean) {
        val dotSize = w * (if (narrower) 0.5f else 0.7f); val dx = x + (w - dotSize) / 2f
        canvas.drawOval(dx, y + h * 0.28f, dx + dotSize, y + h * 0.28f + dotSize, inactivePaint)
        canvas.drawOval(dx, y + h * 0.62f, dx + dotSize, y + h * 0.62f + dotSize, inactivePaint)
        canvas.drawOval(dx, y + h * 0.28f, dx + dotSize, y + h * 0.28f + dotSize, activePaint)
        canvas.drawOval(dx, y + h * 0.62f, dx + dotSize, y + h * 0.62f + dotSize, activePaint)
    }
}
