package com.arkadiusz.dayscounter.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.arkadiusz.dayscounter.R


class ClearableEditText : AppCompatEditText, View.OnTouchListener, View.OnFocusChangeListener, TextWatcher {

    private lateinit var clearTextIcon: Drawable

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_clear)
        val wrappedDrawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(wrappedDrawable, currentHintTextColor)

        clearTextIcon = wrappedDrawable
        clearTextIcon.setBounds(0, 0, clearTextIcon.intrinsicHeight, clearTextIcon.intrinsicHeight)
        setClearIconVisible(false)

        setOnTouchListener(this)
        onFocusChangeListener = this
        addTextChangedListener(this)
    }


    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            setClearIconVisible(text!!.isNotEmpty())
        } else {
            setClearIconVisible(false)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val x = event?.x?.toInt() ?: 0
        if (clearTextIcon.isVisible && x > width - paddingRight - clearTextIcon.intrinsicWidth) {
            if (event?.action == MotionEvent.ACTION_UP) {
                error = null
                setText("")
            }
            return true
        }
        return false
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (isFocused) {
            setClearIconVisible(s.isNotEmpty())
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {}


    private fun setClearIconVisible(visible: Boolean) {
        clearTextIcon.setVisible(visible, false)
        val compoundDrawables = compoundDrawables
        setCompoundDrawables(
                compoundDrawables[0],
                compoundDrawables[1],
                if (visible) clearTextIcon else null,
                compoundDrawables[3])
    }
}
