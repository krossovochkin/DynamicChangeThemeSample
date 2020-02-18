package by.krossovochkin.dynamicthemechangesample

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.hypot

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(
                LayoutInflater.from(this),
                MyLayoutInflater(delegate)
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val newTheme = when (ThemeManager.theme) {
                ThemeManager.Theme.DARK -> ThemeManager.Theme.LIGHT
                ThemeManager.Theme.LIGHT -> ThemeManager.Theme.DARK
            }
            setTheme(newTheme, animate = true)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        setTheme(ThemeManager.Theme.LIGHT, animate = false)
    }

    private fun setTheme(theme: ThemeManager.Theme, animate: Boolean = true) {
        if (!animate) {
            ThemeManager.theme = theme
            return
        }

        if (imageView.isVisible) {
            return
        }

        val w = container.measuredWidth
        val h = container.measuredHeight

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        container.draw(canvas)

        imageView.setImageBitmap(bitmap)
        imageView.isVisible = true

        val finalRadius = hypot(w.toFloat(), h.toFloat())

        ThemeManager.theme = theme

        val anim = ViewAnimationUtils.createCircularReveal(imageView, w / 2, h / 2, finalRadius, 0f)
        anim.duration = 400L
        anim.doOnEnd {
            imageView.setImageDrawable(null)
            imageView.isVisible = false
        }
        anim.start()
    }
}

class MyLayoutInflater(
        private val delegate: AppCompatDelegate
) : LayoutInflater.Factory2 {

    override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet
    ): View? {
        return when (name) {
            "TextView" -> MyTextView(context, attrs)
            "LinearLayout" -> MyLinearLayout(context, attrs)
            "Button" -> MyButton(context, attrs, R.attr.buttonStyle)
            else -> delegate.createView(parent, name, context, attrs)
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }
}


object ThemeManager {

    private val listeners = mutableSetOf<ThemeChangedListener>()
    var theme = Theme.LIGHT
        set(value) {
            field = value
            listeners.forEach { listener -> listener.onThemeChanged(value) }
        }

    interface ThemeChangedListener {

        fun onThemeChanged(theme: Theme)
    }

    data class ButtonTheme(
            @ColorRes
            val backgroundTint: Int,
            @ColorRes
            val textColor: Int
    )

    data class TextViewTheme(
            @ColorRes
            val textColor: Int
    )

    data class ViewGroupTheme(
            @ColorRes
            val backgroundColor: Int
    )

    enum class Theme(
            val buttonTheme: ButtonTheme,
            val textViewTheme: TextViewTheme,
            val viewGroupTheme: ViewGroupTheme
    ) {
        DARK(
                buttonTheme = ButtonTheme(
                        backgroundTint = android.R.color.holo_green_dark,
                        textColor = android.R.color.white
                ),
                textViewTheme = TextViewTheme(
                        textColor = android.R.color.white
                ),
                viewGroupTheme = ViewGroupTheme(
                        backgroundColor = android.R.color.background_dark
                )
        ),
        LIGHT(
                buttonTheme = ButtonTheme(
                        backgroundTint = android.R.color.holo_green_light,
                        textColor = android.R.color.black
                ),
                textViewTheme = TextViewTheme(
                        textColor = android.R.color.black
                ),
                viewGroupTheme = ViewGroupTheme(
                        backgroundColor = android.R.color.background_light
                )
        )
    }

    fun addListener(listener: ThemeChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ThemeChangedListener) {
        listeners.remove(listener)
    }
}

class MyTextView
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr),
        ThemeManager.ThemeChangedListener {

    override fun onFinishInflate() {
        super.onFinishInflate()
        ThemeManager.addListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }

    override fun onThemeChanged(theme: ThemeManager.Theme) {
        setTextColor(
                ContextCompat.getColor(
                        context,
                        theme.textViewTheme.textColor
                )
        )
    }
}

class MyLinearLayout
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
        ThemeManager.ThemeChangedListener {

    override fun onFinishInflate() {
        super.onFinishInflate()
        ThemeManager.addListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }

    override fun onThemeChanged(theme: ThemeManager.Theme) {
        setBackgroundColor(
                ContextCompat.getColor(
                        context,
                        theme.viewGroupTheme.backgroundColor
                )
        )
    }
}

class MyButton
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr),
        ThemeManager.ThemeChangedListener {

    override fun onFinishInflate() {
        super.onFinishInflate()
        ThemeManager.addListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }

    override fun onThemeChanged(theme: ThemeManager.Theme) {
        setTextColor(
                ContextCompat.getColor(
                        context,
                        theme.buttonTheme.textColor
                )
        )
        backgroundTintList =
                ColorStateList.valueOf(
                        ContextCompat.getColor(
                                context,
                                theme.buttonTheme.backgroundTint
                        )
                )
    }
}
