package com.arkadiusz.dayscounter.Fragments;

import static android.content.Context.VIBRATOR_SERVICE;

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
import com.arkadiusz.dayscounter.Activities.AddActivity;
import com.arkadiusz.dayscounter.Activities.DetailActivity;
import com.arkadiusz.dayscounter.Activities.MainActivity;
import com.arkadiusz.dayscounter.Adapters.RecyclerViewAdapter;
import com.arkadiusz.dayscounter.Database.Event;
import com.arkadiusz.dayscounter.Model.Migration;
import com.arkadiusz.dayscounter.Model.RecyclerItemClickListener;
import com.arkadiusz.dayscounter.Model.RecyclerItemClickListener.OnItemClickListener;
import com.arkadiusz.dayscounter.R;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;


public class PastFragment extends Fragment {

  Realm realm;
  RealmResults<Event> results;
  RecyclerItemClickListener mListener;
  MainActivity activity;
  private String[] options;
  private String sortType;
  RecyclerView recyclerView;
  RecyclerViewAdapter adapter;


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
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.past_fragment_xml, container, false);
    recyclerView = (RecyclerView) view.findViewById(R.id.past_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.fragment_main_dialog_title));
            builder.setItems(options, new DialogInterface.OnClickListener() {
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
                    new Builder(getContext())
                        .setTitle(getString(R.string.fragment_delete_dialog_title))
                        .setMessage(getString(R.string.fragment_delete_dialog_question))
                        .setPositiveButton(android.R.string.yes, new OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                            int id = results.get(position).getId();
                            final RealmResults<Event> results = realm.where(Event.class)
                                .equalTo("id", id).findAll();
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
                    break;
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
    results = realm.where(Event.class).equalTo("type", "past").findAll();
    switch (sortType) {
      case "date_desc":
        results = results.sort("date", Sort.ASCENDING);
        break;
      case "date_asc":
        results = results.sort("date",Sort.DESCENDING);
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
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    sortType = sharedPreferences.getString("sort_type", "date_order");
  }

  public void refreshData() {
    receiveSortType();
    results = realm.where(Event.class).equalTo("type", "past").findAll();
    switch (sortType) {
      case "date_desc":
        results = results.sort("date", Sort.ASCENDING);
        break;
      case "date_asc":
        results = results.sort("date",Sort.DESCENDING);
        break;
      case "date_order":
        break;
    }
    adapter = new RecyclerViewAdapter(getContext(), results);
    recyclerView.setAdapter(adapter);
  }

}
