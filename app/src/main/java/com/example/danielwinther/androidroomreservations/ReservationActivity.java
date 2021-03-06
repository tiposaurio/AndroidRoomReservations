package com.example.danielwinther.androidroomreservations;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReservationActivity extends FragmentActivity {
    private Room room;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return DoIt(e1, e2);
            }

            private boolean DoIt(MotionEvent e1, MotionEvent e2) {
                boolean right = e1.getX() < e2.getX();
                if (right) {
                    Intent intent = new Intent(getApplicationContext(), CreateReservationActivity.class);
                    int position = getIntent().getIntExtra("position", 0);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return DoIt(e1, e2);
            }
        });
        room = (Room) getIntent().getSerializableExtra("room");

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SelectedItemFragment selectedItemFragment = new SelectedItemFragment();
        Bundle parameters = new Bundle();
        parameters.putString("text1", room.getName());
        parameters.putString("text2", room.getDescription());
        parameters.putString("text3", room.getRemarks());
        parameters.putString("text4", String.valueOf(room.getCapacity()));
        selectedItemFragment.setArguments(parameters);
        fragmentTransaction.replace(android.R.id.content, selectedItemFragment);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            fragmentTransaction.replace(android.R.id.content, selectedItemFragment);
        }
        else {
            fragmentTransaction.remove(selectedItemFragment);
        }
        fragmentTransaction.commit();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(HelperClass.URL + "reservations/room/" + room.getRoomId(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        final List<Reservation> reservations = new ArrayList<>();
                        final ListView listView = (ListView) findViewById(R.id.listView);
                        ArrayAdapter<Reservation> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, reservations);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Reservation value = (Reservation) listView.getItemAtPosition(position);
                                Intent intent = new Intent(getApplicationContext(), ReservationDetailActivity.class);
                                intent.putExtra("reservation", value);
                                startActivity(intent);
                            }
                        });

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                reservations.add(new Reservation(jsonObject.getInt("Id"), jsonObject.getString("DeviceId"), jsonObject.getString("Purpose"), jsonObject.getString("FromTimeString"), jsonObject.getString("ToTimeString"), jsonObject.getInt("RoomId"), jsonObject.getInt("UserId")));
                            } catch (JSONException e) {
                                new HelperClass().ErrorDialog(ReservationActivity.this, null, null);
                                Log.e(HelperClass.ERROR, e.toString());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new HelperClass().ErrorDialog(ReservationActivity.this, null, null);
                Log.e(HelperClass.ERROR, error.toString());
            }
        });
        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    public void back(View view) {
        finish();
    }
}