package com.example.koti.ui.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BestProductsItemDecoration(private val amount: Int = 1) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(rect: Rect, v: View, parent: RecyclerView, s: RecyclerView.State) {
        parent.adapter?.let { adapter ->
            val childAdapterPosition = parent.getChildAdapterPosition(v)
                .let { if (it == RecyclerView.NO_POSITION) return else it }
            if (childAdapterPosition % 2 == 1) {
                rect.left = amount
            } else {
                rect.right = amount
            }
            rect.bottom = amount
        }
    }
}