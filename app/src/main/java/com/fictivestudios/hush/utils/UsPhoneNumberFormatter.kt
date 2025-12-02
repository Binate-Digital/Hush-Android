package com.fictivestudios.hush.utils

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import java.lang.ref.WeakReference


class UsPhoneNumberFormatter(private val mWeakEditText: WeakReference<TextInputEditText>) :
    TextWatcher {
    //This TextWatcher sub-class formats entered numbers as 1 (123) 456-7890
    private var mFormatting // this is a flag which prevents the
            = false

    // stack(onTextChanged)
    private var clearFlag = false
    private var mLastStartLocation = 0
    private var mLastBeforeText: String? = null
    override fun beforeTextChanged(
        s: CharSequence, start: Int, count: Int,
        after: Int
    ) {
        if ((after == 0) && s.toString() == "1") {
            clearFlag = true
        }
        mLastStartLocation = start
        mLastBeforeText = s.toString()
    }

    override fun onTextChanged(
        s: CharSequence, start: Int, before: Int,
        count: Int
    ) {
    }

    override fun afterTextChanged(s: Editable) {
        // Make sure to ignore calls to afterTextChanged caused by the work
        // done below
        if (!mFormatting) {
            mFormatting = true
            val curPos = mLastStartLocation
            val beforeValue = mLastBeforeText
            val currentValue = s.toString()
            val formattedValue = formatUsNumber(s)
            if (currentValue.length > beforeValue!!.length) {
                val setCusorPos = (formattedValue.length
                        - (beforeValue.length - curPos))
                mWeakEditText.get()!!.setSelection(if (setCusorPos < 0) 0 else setCusorPos)
            } else {
                var setCusorPos = (formattedValue.length
                        - (currentValue.length - curPos))
                if (setCusorPos > 0 && !Character.isDigit(formattedValue[setCusorPos - 1])) {
                    setCusorPos--
                }
                mWeakEditText.get()!!.setSelection(if (setCusorPos < 0) 0 else setCusorPos)
            }
            mFormatting = false
        }
    }

    private fun formatUsNumber(text: Editable): String {
        val formattedString = StringBuilder()
        // Remove everything except digits
        var p = 0
        while (p < text.length) {
            val ch = text[p]
            if (!Character.isDigit(ch)) {
                text.delete(p, p + 1)
            } else {
                p++
            }
        }
        // Now only digits are remaining
        val allDigitString = text.toString()
        val totalDigitCount = allDigitString.length
        if ((totalDigitCount == 0 || (totalDigitCount > 10) && !allDigitString.startsWith("1")) || totalDigitCount > 11) {
            // May be the total length of input length is greater than the
            // expected value so we'll remove all formatting
            text.clear()
            text.append(allDigitString)
            return allDigitString
        }
        var alreadyPlacedDigitCount = 0
        // Only '1' is remaining and user pressed backspace and so we clear
        // the edit text.
        if (allDigitString == "1" && clearFlag) {
            text.clear()
            clearFlag = false
            return ""
        }
        //        if (allDigitString.startsWith("1")) {
//            formattedString.append("1 ");
//            alreadyPlacedDigitCount++;
//        }
        // The first 3 numbers beyond '1' must be enclosed in brackets "()"
        if (totalDigitCount - alreadyPlacedDigitCount > 3) {
            formattedString.append(
                "("
                        + allDigitString.substring(
                    alreadyPlacedDigitCount,
                    alreadyPlacedDigitCount + 3
                ) + ") "
            )
            alreadyPlacedDigitCount += 3
        }
        // There must be a '-' inserted after the next 3 numbers
        if (totalDigitCount - alreadyPlacedDigitCount > 3) {
            formattedString.append(
                (allDigitString.substring(
                    alreadyPlacedDigitCount, alreadyPlacedDigitCount + 3
                )
                        + "-")
            )
            alreadyPlacedDigitCount += 3
        }
        // All the required formatting is done so we'll just copy the
        // remaining digits.
        if (totalDigitCount > alreadyPlacedDigitCount) {
            formattedString.append(
                allDigitString
                    .substring(alreadyPlacedDigitCount)
            )
        }
        text.clear()
        text.append(formattedString.toString())
        return formattedString.toString()
    }
}







