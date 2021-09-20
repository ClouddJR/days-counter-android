package com.arkadiusz.dayscounter.ui.common

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

open class RecyclerItemClickListener(
    context: Context, recyclerView: androidx.recyclerview.widget.RecyclerView,
    private val mListener: OnItemClickListener?
) : androidx.recyclerview.widget.RecyclerView.OnItemTouchListener {

    private val mGestureDetector: GestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(motionEvent: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                if (childView != null && mListener != null) {
                    mListener.onItemLongClick(
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
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        motionEvent: MotionEvent
    ): Boolean {
        val childView = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(motionEvent)) {
            mListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        motionEvent: MotionEvent
    ) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

}
