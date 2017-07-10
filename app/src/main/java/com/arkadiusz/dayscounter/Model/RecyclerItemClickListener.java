package com.arkadiusz.dayscounter.Model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Arkadiusz on 03.01.2017.
 */

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

  public static interface OnItemClickListener {

    public void onItemClick(View view, int position);

    public void onItemLongClick(View view, int position);
  }

  private OnItemClickListener mListener;
  private GestureDetector mGestureDetector;

  public RecyclerItemClickListener(Context context, final RecyclerView recyclerView,
      OnItemClickListener listener) {
    mListener = listener;
    mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
      public boolean onSingleTapUp(MotionEvent e) {
        return true;
      }

      public void onLongPress(MotionEvent motionEvent) {
        View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (childView != null && mListener != null) {
          mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
        }

      }
    });
  }

  public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
    View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
    if (childView != null && mListener != null && mGestureDetector.onTouchEvent(motionEvent)) {
      mListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
    }
    return false;
  }

  public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
  }

  @Override
  public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
  }

}
