package com.arkadiusz.dayscounter.util.purchaseutils;

/**
 * Created by Arkadiusz on 26.02.2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for the "com.android.vending.billing.PURCHASES_UPDATED" Action
 * from the Play Store.
 *
 * <p>It is possible that an in-app item may be acquired without the
 * application calling getBuyIntent(), for example if the item can be
 * redeemed from inside the Play Store using a promotional code. If this
 * application isn't running at the time, then when it is started a call
 * to getPurchases() will be sufficient notification. However, if the
 * application is already running in the background when the item is acquired,
 * a message to this BroadcastReceiver will indicate that the an item
 * has been acquired.</p>
 */
public class IabBroadcastReceiver extends BroadcastReceiver {
  /**
   * Listener interface for received broadcast messages.
   */
  public interface IabBroadcastListener {
    void receivedBroadcast();
  }

  /**
   * The Intent action that this Receiver should filter for.
   */
  public static final String ACTION = "com.android.vending.billing.PURCHASES_UPDATED";

  private final IabBroadcastListener mListener;

  public IabBroadcastReceiver(IabBroadcastListener listener) {
    mListener = listener;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (mListener != null) {
      mListener.receivedBroadcast();
    }
  }
}
