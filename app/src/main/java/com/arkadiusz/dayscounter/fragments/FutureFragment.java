package com.arkadiusz.dayscounter.fragments;

import static android.content.Context.VIBRATOR_SERVICE;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.arkadiusz.dayscounter.activities.AddActivity;
import com.arkadiusz.dayscounter.activities.DetailActivity;
import com.arkadiusz.dayscounter.adapters.RecyclerViewAdapter;
import com.arkadiusz.dayscounter.database.Event;
import com.arkadiusz.dayscounter.model.Migration;
import com.arkadiusz.dayscounter.model.RecyclerItemClickListener;
import com.arkadiusz.dayscounter.model.RecyclerItemClickListener.OnItemClickListener;
import com.arkadiusz.dayscounter.R;
import com.arkadiusz.dayscounter.utils.SharedPreferencesUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;


public class FutureFragment extends Fragment {

  Realm realm;
  RealmResults<Event> results;
  private String[] options;
  private String sortType;
  RecyclerView recyclerView;
  RecyclerViewAdapter adapter;
  private DatabaseReference mDatabaseReference;


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    receiveSortType();
    setUpRealm();
    setUpOptions();
    setUpFirebase();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view;
    view = inflater.inflate(R.layout.future_fragment, container, false);


    recyclerView = (RecyclerView) view.findViewById(R.id.future_recycler_view);
    recyclerView
        .setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new RecyclerViewAdapter(getContext(), results);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(adapter);
    recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView,
        new OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            int id = results.get(position).getId();
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra("event_id", id);
            String transitionName = "transition_detail";
            ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                    view,   //
                    transitionName    //
                );

            startActivity(intent, options.toBundle());
          }

          @Override
          public void onItemLongClick(View view, final int position) {
            vibration();

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getContext());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            arrayAdapter.addAll(options);
            builder.setTitle(getString(R.string.fragment_main_dialog_title));
            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                  case 0:
                    int id = results.get(position).getId();
                    Intent intent = new Intent(getContext(), AddActivity.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("id", id);
                    startActivity(intent);
                    break;
                  case 1:
                    Builder builder;
                    builder = new Builder(getContext());
                    builder
                        .setTitle(getString(R.string.fragment_delete_dialog_title))
                        .setMessage(getString(R.string.fragment_delete_dialog_question))
                        .setPositiveButton(android.R.string.yes, new OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                            int id = results.get(position).getId();
                            final RealmResults<Event> results = realm.where(Event.class)
                                .equalTo("id", id).findAll();
                            Event event = results.first();

                            NotificationManager manager = (NotificationManager) getContext().getSystemService(
                                Context.NOTIFICATION_SERVICE);
                            manager.cancel(event.getId());

                            if (!SharedPreferencesUtils.getFirebaseEmail(getContext()).equals("")) {
                              mDatabaseReference
                                  .child(SharedPreferencesUtils.getFirebaseEmail(getContext()))
                                  .child(
                                      "Event " + event.getId() + " " + event.getName() + " " + event
                                          .getDate()).removeValue();
                            }

                            realm.executeTransaction(new Transaction() {
                              @Override
                              public void execute(Realm realm) {
                                results.deleteAllFromRealm();
                              }
                            });

                          }
                        })
                        .setNegativeButton(android.R.string.no, new OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                          }
                        })
                        .show();
                }
              }
            });
            builder.show();
          }
        }));

    return view;
  }

  public void vibration() {
    Vibrator v = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
    v.vibrate(25);
  }

  public void setUpRealm() {
    Realm.init(getContext());
    RealmConfiguration config = new RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(new Migration())
        .build();
    realm = Realm.getInstance(config);
    results = realm.where(Event.class).equalTo("type", "future").findAll();
    switch (sortType) {
      case "date_desc":
        results = results.sort("date", Sort.DESCENDING);
        break;
      case "date_asc":
        results = results.sort("date");
        break;
      case "date_order":
        break;
    }
  }

  public void setUpOptions() {
    options = new String[2];
    options[0] = getString(R.string.fragment_main_dialog_option_edit);
    options[1] = getString(R.string.fragment_main_dialog_option_delete);
  }

  public void receiveSortType() {
    SharedPreferences sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(getActivity());
    sortType = sharedPreferences.getString("sort_type", "date_order");
  }

  public void refreshData() {
    receiveSortType();
    results = realm.where(Event.class).equalTo("type", "future").findAll();
    switch (sortType) {
      case "date_desc":
        results = results.sort("date", Sort.DESCENDING);
        break;
      case "date_asc":
        results = results.sort("date");
        break;
      case "date_order":
        break;
    }
    adapter = new RecyclerViewAdapter(getContext(), results);
    recyclerView.setAdapter(adapter);
  }

  private void setUpFirebase() {
    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
  }

}
