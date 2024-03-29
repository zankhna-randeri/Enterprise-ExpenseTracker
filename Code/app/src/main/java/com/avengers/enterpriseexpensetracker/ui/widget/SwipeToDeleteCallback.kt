package com.avengers.enterpriseexpensetracker.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R

abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.Callback() {

    private var mContext: Context? = context
    private var mClearPaint: Paint? = null
    private var mBackground: ColorDrawable? = null
    private var backgroundColor = 0
    private var deleteDrawable: Drawable? = null
    private var intrinsicWidth = 0
    private var intrinsicHeight = 0

    init {
        mBackground = ColorDrawable()
        mClearPaint = Paint()
        mClearPaint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        mContext?.let {
            backgroundColor = ContextCompat.getColor(mContext!!, R.color.color_delete)
            deleteDrawable = ContextCompat.getDrawable(mContext!!, R.drawable.ic_delete_white)
        }
        deleteDrawable?.let {
            intrinsicWidth = it.intrinsicWidth
            intrinsicHeight = it.intrinsicHeight
        }
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas,
                             recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder,
                             dX: Float,
                             dY: Float,
                             actionState: Int,
                             isCurrentlyActive: Boolean) {

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val itemHeight = itemView.height
        val isCancelled = (dX == 0f && !isCurrentlyActive)

        if (isCancelled) {
            clearCanvas(c,
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        mBackground?.color = backgroundColor
        mBackground?.setBounds(itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom)
        mBackground?.draw(c)

        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        deleteDrawable?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteDrawable?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas,
                            left: Float,
                            top: Float,
                            right: Float,
                            bottom: Float) {
        mClearPaint?.let { c.drawRect(left, top, right, bottom, it) }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }
}