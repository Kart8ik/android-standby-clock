package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var previewClock: LcdClockView
    private var bgIndicator: View? = null
    private var activeIndicator: View? = null
    private var inactiveIndicator: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        scrollView.addView(layout)

        val title = TextView(this).apply {
            text = "Bedside Clock"
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        // --- Preview Area ---
        val previewTitle = TextView(this).apply {
            text = "Preview"
            textSize = 14f
            setPadding(0, 20, 0, 10)
        }

        previewClock = LcdClockView(this).apply {
            val density = resources.displayMetrics.density
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (180 * density).toInt()
            )
        }

        previewClock.updateTime()

        // --- Settings Controls ---
        val toggle24Hr = Switch(this).apply {
            text = "Use 24-hour format"
            isChecked = AppSettings.is24HourFormat(this@MainActivity)

            setOnCheckedChangeListener { _, isChecked ->
                AppSettings.set24HourFormat(
                    this@MainActivity,
                    isChecked
                )

                previewClock.updateTime()
            }

            setPadding(0, 20, 0, 20)
        }

        val toggleUnit = Switch(this).apply {
            val currentUnit =
                AppSettings.getTempUnit(this@MainActivity)

            text =
                "Temperature Unit: ${
                    if (currentUnit == "C")
                        "Celsius (°C)"
                    else
                        "Fahrenheit (°F)"
                }"

            isChecked = currentUnit == "F"

            setOnCheckedChangeListener { _, isChecked ->
                val newUnit =
                    if (isChecked) "F" else "C"

                AppSettings.setTempUnit(
                    this@MainActivity,
                    newUnit
                )

                text =
                    "Temperature Unit: ${
                        if (newUnit == "C")
                            "Celsius (°C)"
                        else
                            "Fahrenheit (°F)"
                    }"

                previewClock.updateTime()
            }

            setPadding(0, 0, 0, 20)
        }

        layout.addView(title)
        layout.addView(previewTitle)
        layout.addView(previewClock)
        layout.addView(toggle24Hr)
        layout.addView(toggleUnit)

        // --- Color Pickers ---

        addColorPickerRow(layout, "Background Color") {
            showColorPickerDialog(
                "Background Color",
                AppSettings.getColorBg(this)
            ) { color ->
                AppSettings.setColorBg(this, color)
            }
        }

        addColorPickerRow(layout, "Active Segment Color") {
            showColorPickerDialog(
                "Active Segment Color",
                AppSettings.getColorActive(this)
            ) { color ->
                AppSettings.setColorActive(this, color)
            }
        }

        addColorPickerRow(layout, "Inactive Segment Color") {
            showColorPickerDialog(
                "Inactive Segment Color",
                AppSettings.getColorInactive(this)
            ) { color ->
                AppSettings.setColorInactive(this, color)
            }
        }

        // --- Action Buttons ---

        val standbyButton = Button(this).apply {
            text = "Start StandBy"

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 40, 0, 0)
            }

            setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        StandbyActivity::class.java
                    )
                )
            }
        }

        val settingsButton = Button(this).apply {
            text = "Open Screen Saver Settings"

            setOnClickListener {
                try {
                    startActivity(
                        Intent(Settings.ACTION_DREAM_SETTINGS)
                    )
                } catch (e: Exception) {
                    try {
                        startActivity(
                            Intent(Settings.ACTION_DISPLAY_SETTINGS)
                        )
                    } catch (ex: Exception) {
                        // Nothing to do
                    }
                }
            }
        }

        val dreamHint = TextView(this).apply {
            text =
                "\nTo use as an automatic screensaver:\n" +
                        "1. Open Screen Saver settings\n" +
                        "2. Select 'Bedside Clock'"

            textSize = 12f
            setPadding(0, 40, 0, 40)
            gravity = Gravity.CENTER
        }

        layout.addView(standbyButton)
        layout.addView(settingsButton)
        layout.addView(dreamHint)

        setContentView(scrollView)
    }

    private fun addColorPickerRow(
        parent: LinearLayout,
        label: String,
        onClick: () -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 20, 0, 20)
            isClickable = true
            isFocusable = true

            val outValue = android.util.TypedValue()

            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
            )

            setBackgroundResource(outValue.resourceId)

            setOnClickListener {
                onClick()
            }
        }

        val text = TextView(this).apply {
            text = label
            textSize = 16f

            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
        }

        val colorIndicator = View(this).apply {
            val size =
                (30 * resources.displayMetrics.density).toInt()

            layoutParams =
                LinearLayout.LayoutParams(size, size)

            val color = when (label) {
                "Background Color" ->
                    AppSettings.getColorBg(this@MainActivity)

                "Active Segment Color" ->
                    AppSettings.getColorActive(this@MainActivity)

                else ->
                    AppSettings.getColorInactive(this@MainActivity)
            }

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setStroke(3, Color.LTGRAY)
            }

            when (label) {
                "Background Color" ->
                    bgIndicator = this

                "Active Segment Color" ->
                    activeIndicator = this

                else ->
                    inactiveIndicator = this
            }
        }

        row.addView(text)
        row.addView(colorIndicator)

        parent.addView(row)
    }

    private fun updateAllUIs() {
        previewClock.refreshColors()

        (bgIndicator?.background as? GradientDrawable)
            ?.setColor(
                AppSettings.getColorBg(this)
            )

        (activeIndicator?.background as? GradientDrawable)
            ?.setColor(
                AppSettings.getColorActive(this)
            )

        (inactiveIndicator?.background as? GradientDrawable)
            ?.setColor(
                AppSettings.getColorInactive(this)
            )
    }

    private fun showColorPickerDialog(
        title: String,
        currentColor: Int,
        onColorSelected: (Int) -> Unit
    ) {

        val density = resources.displayMetrics.density

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt(),
                (24 * density).toInt()
            )
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // --- Clock Preview ---

        val dialogPreview = LcdClockView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (120 * density).toInt()
            ).apply {
                setMargins(
                    0,
                    0,
                    0,
                    (24 * density).toInt()
                )
            }
        }

        dialogPreview.updateTime()
        dialogLayout.addView(dialogPreview)

        // --- Color Wheel ---

        val colorWheel = ColorWheelView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (360 * density).toInt()
            )

            selectedColor = currentColor
        }

        dialogLayout.addView(colorWheel)

        // --- Current Color Preview ---

        val selectedColorPreview = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                (48 * density).toInt(),
                (48 * density).toInt()
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(
                    0,
                    (16 * density).toInt(),
                    0,
                    (8 * density).toInt()
                )
            }

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(currentColor)
                setStroke(
                    (2 * density).toInt(),
                    Color.LTGRAY
                )
            }
        }

        dialogLayout.addView(selectedColorPreview)

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogLayout)
            .setPositiveButton("Done") { _, _ ->
                val color = colorWheel.selectedColor

                onColorSelected(color)

                updateAllUIs()
            }
            .setNegativeButton("Cancel", null)
            .create()

        /*
         * ColorWheelView handles the actual touch events itself.
         *
         * We attach a listener only to observe changes after the
         * ColorWheelView has processed the touch event.
         */
        colorWheel.setOnTouchListener { _, _ ->

            colorWheel.post {

                val color =
                    colorWheel.selectedColor

                // Update the little preview circle.
                (
                        selectedColorPreview.background
                                as? GradientDrawable
                        )?.setColor(color)

                /*
                 * Temporarily apply the color so the clock preview
                 * reflects the selected color while dragging.
                 */
                when (title) {

                    "Background Color" -> {
                        AppSettings.setColorBg(
                            this,
                            color
                        )
                    }

                    "Active Segment Color" -> {
                        AppSettings.setColorActive(
                            this,
                            color
                        )
                    }

                    "Inactive Segment Color" -> {
                        AppSettings.setColorInactive(
                            this,
                            color
                        )
                    }
                }

                dialogPreview.refreshColors()
                previewClock.refreshColors()

                updateAllUIs()
            }

            // Let ColorWheelView continue processing the event.
            false
        }

        dialog.setOnShowListener {
            /*
             * Make sure the preview starts with the current
             * persisted color.
             */
            dialogPreview.refreshColors()
        }

        dialog.show()
    }
}