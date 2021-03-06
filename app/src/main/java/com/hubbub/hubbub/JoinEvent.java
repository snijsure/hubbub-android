package com.hubbub.hubbub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hubbub.hubbub.adapters.JoinEventAdapter;
import com.hubbub.hubbub.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class JoinEvent extends BaseActivity {

    private ListView mDaysForEvents;
    private ArrayList<HashMap.Entry<String, ArrayList<Event>>> daysOfEvents =
            new ArrayList<HashMap.Entry<String, ArrayList<Event>>>();

    private HashMap<String, ArrayList<Event>> days = new HashMap<String, ArrayList<Event>>();
    private JoinEventAdapter adapter;
    SimpleDateFormat day = new SimpleDateFormat("dd/YY");

    private static final String TAG = "JoiningEventsPage";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuItem menuItem = menu.add(Menu.NONE, 1000, Menu.NONE, R.string.button_text_save);
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent nextScreen = new Intent(this, MainActivity.class);
        startActivity(nextScreen);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event);

        mDaysForEvents = (ListView) findViewById(R.id.daysOfEvents);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        final String userId = getUid();
        setUpListView();

        Long currentTime = (new Date()).getTime() / 1000;
        mDatabase.child("events")
                .orderByChild("startAt")
                .startAt(currentTime)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        daysOfEvents.clear(); //TODO (since # of Events is small recreating is fine.
                                             // Would be better to use Firebase's recycle view)
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Event event = postSnapshot.getValue(Event.class);
                            if (event.state.contains("open")) {
                                addSlotToDay(event);
                            }
                        }
                        daysOfEvents.addAll(days.entrySet());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getAccount:onCancelled", databaseError.toException());
                    }
                }
        );
    }

    // TODO: is this super inefficient? Look into that...
    private void addSlotToDay(Event event) {

        String dayYearOfSlot = day.format(new Date(event.startAt * 1000));
        ArrayList<Event> slotsInDay;
        if (days.containsKey(dayYearOfSlot)) {
            slotsInDay = days.get(dayYearOfSlot);
        } else {
            slotsInDay = new ArrayList<Event>();
            days.put(dayYearOfSlot, slotsInDay);
        }

        if (!slotsInDay.contains(event)) {
            slotsInDay.add(event);
        }

    }

    private void setUpListView() {
        adapter = new JoinEventAdapter(this, R.layout.multi_events_view, daysOfEvents);
        mDaysForEvents.setAdapter(adapter);
    }

}
