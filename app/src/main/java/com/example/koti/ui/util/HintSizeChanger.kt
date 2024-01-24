package com.example.koti.ui.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.koti.R
import com.google.android.material.textfield.TextInputLayout

fun changeHintSizeWhenTextExists(editText: EditText) {
    val textInputLayout = editText.parent.parent as? TextInputLayout
    textInputLayout?.setHintTextAppearance(R.style.CustomHintTextAppearance)

    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            textInputLayout?.setHintTextAppearance(R.style.CustomHintTextAppearance_Large)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.isNullOrEmpty()) {
                textInputLayout?.setHintTextAppearance(R.style.CustomHintTextAppearance_Large)
            } else {
                textInputLayout?.setHintTextAppearance(R.style.CustomHintTextAppearance)
            }
        }

        override fun afterTextChanged(s: Editable?) {
            textInputLayout?.setHintTextAppearance(R.style.CustomHintTextAppearance)
        }
    }

    editText.addTextChangedListener(textWatcher)
}

fun changeEdBackgroundDrawable(textField: TextInputLayout, drawable: Int, context: Context) {
    textField.background = ContextCompat.getDrawable(context, drawable)
}

fun changeButtonBackgroundDrawable(button: Button, drawable: Int, context: Context) {
    button.background = ContextCompat.getDrawable(context, drawable)
}
