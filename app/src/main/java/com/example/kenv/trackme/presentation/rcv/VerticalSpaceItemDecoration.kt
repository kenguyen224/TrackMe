package com.example.kenv.trackme.presentation.rcv

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Kenv on 07/01/2021.
 */
private const val SPACE_ITEM = 16

class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int = SPACE_ITEM) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val lastIndex = parent.adapter?.itemCount ?: 0 - 1
        if (parent.getChildAdapterPosition(view) != lastIndex) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}
