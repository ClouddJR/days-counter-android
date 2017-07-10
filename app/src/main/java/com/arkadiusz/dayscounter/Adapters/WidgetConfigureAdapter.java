package com.arkadiusz.dayscounter.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.arkadiusz.dayscounter.Database.Event;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Arkadiusz on 08.01.2017.
 */

public class WidgetConfigureAdapter extends RealmBaseAdapter<Event> implements ListAdapter {

  private OrderedRealmCollection<Event> events;
  private Context mContext;

  private static class ViewHolder {

    TextView eventitle;
  }

  public WidgetConfigureAdapter(Context context, OrderedRealmCollection<Event> realmResults) {
    super(context, realmResults);
    this.mContext = context;
    this.events = realmResults;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.eventitle = (TextView) convertView.findViewById(android.R.id.text1);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    Event item = events.get(position);
    viewHolder.eventitle.setText(item.getName());
    return convertView;
  }
}
