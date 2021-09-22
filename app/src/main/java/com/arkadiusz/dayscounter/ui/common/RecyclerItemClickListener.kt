package com.arkadiusz.dayscounter.ui.common

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(
    context: Context, recyclerView: RecyclerView,
    private val clickListener: OnItemClickListener
) : RecyclerView.OnItemTouchListener {

    private val mGestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(motionEvent: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                if (childView != null) {
                    clickListener.onItemLongClick(
                        childView,
                        recyclerView.getChildAdapterPosition(childView)
                    )
                }
            }
        })

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onItemLongClick(view: View?, position: Int)
    }

    override fun onInterceptTouchEvent(
        recyclerView: RecyclerView,
        motionEvent: MotionEvent
    ): Boolean {
        val childView = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
        if (childView != null && mGestureDetector.onTouchEvent(motionEvent)) {
            clickListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
