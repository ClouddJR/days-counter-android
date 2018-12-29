package com.arkadiusz.dayscounter.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Created by Arkadiusz on 03.01.2017
 */

open class RecyclerItemClickListener(context: Context, recyclerView: androidx.recyclerview.widget.RecyclerView,
                                     private val mListener: OnItemClickListener?) : androidx.recyclerview.widget.RecyclerView.OnItemTouchListener {

    private val mGestureDetector: GestureDetector

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onItemLongClick(view: View?, position: Int)
    }

    init {
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(motionEvent: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                if (childView != null && mListener != null) {
                    mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(recyclerView: androidx.recyclerview.widget.RecyclerView, motionEvent: MotionEvent): Boolean {
        val childView = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(motionEvent)) {
            mListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(recyclerView: androidx.recyclerview.widget.RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

}
