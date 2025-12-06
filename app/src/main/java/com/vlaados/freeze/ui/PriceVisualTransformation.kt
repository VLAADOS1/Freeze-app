package com.vlaados.freeze.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PriceVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = if (original.isNotEmpty()) {
            original.reversed().chunked(3).joinToString(",").reversed()
        } else ""

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (original.isEmpty()) return 0
                var transOffset = 0
                var origOffset = 0
                while (origOffset < offset) {
                    if (transOffset < formatted.length && formatted[transOffset] == ',') {
                        transOffset++
                    } else {
                        transOffset++
                        origOffset++
                    }
                }
                return transOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (formatted.isEmpty()) return 0
                var transOffset = 0
                var origOffset = 0
                while (transOffset < offset) {
                    if (transOffset < formatted.length && formatted[transOffset] != ',') {
                        origOffset++
                    }
                    transOffset++
                }
                return origOffset
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
