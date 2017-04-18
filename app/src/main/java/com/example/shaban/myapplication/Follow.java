package com.example.shaban.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Follow extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String email;
    ArrayList<ListItem> follows = new ArrayList<ListItem>();
    SessionManager session;
    TextView em;
    CalendarView cal;
    ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(5);
    RequestQueue requestQueue;
    //FollowsActivity followsActivity = new FollowsActivity();
    TaskManager tasking;
    SwipeRefreshLayout swipeRefreshLayout;
    Date d = new Date();
    private GoogleApiClient client;
    ListView listView;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_follow);
        progress = new ProgressDialog(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        listView = (ListView) findViewById(R.id.newList);
        setSupportActionBar(toolbar);
        closeActivities();
        tasking = new TaskManager(this);
        session = new SessionManager(this);
        session.checkLogin();
        HashMap<String, String> userDetails = session.getUserDetails();
        email = userDetails.get(SessionManager.KEY_EMAIL);
        requestQueue = Volley.newRequestQueue(this);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
        updateTask();
        executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            updateTask();
                            JSONArray jsonArray = tasking.getTasks();
                            Date currentDate = new Date();
                            String currentTime = getRightDate(currentDate);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String time = jsonObject.getString("time");
                                if (time.matches(currentTime)) {
                                    String subject = jsonObject.getString("subject");
                                    String details = jsonObject.getString("details");
                                    createNotification(subject, details, time);
                                }
                            }

                        } catch (Exception e) {

                        }

                    }
                });

            }
        }, 0, 1, TimeUnit.MINUTES);
        cal = (CalendarView) findViewById(R.id.calendarView);
        ArrayList<ListItem> items = null;
        try {
            items = getFollows();
            MyCustomAdapter adapter = new MyCustomAdapter(items);
            listView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        cal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                d.setMonth(month);
                d.setYear(year - 1900);
                d.setDate(dayOfMonth);
                String date = getRightDay(d);
                try {
                    follows = todayFilter(date);
                    MyCustomAdapter adapter = new MyCustomAdapter(follows);
                    listView.setAdapter(adapter);
                } catch (JSONException e) {
                    Toast.makeText(Follow.this, "Couldn't get feed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                ListItem listItem = (ListItem) listView.getItemAtPosition(position);
                                deleteFollow(listItem.getId());
                                try {
                                    tasking.clearTasks();
                                    updateTask();
                                    follows = todayFilter(getRightDay(d));
                                    MyCustomAdapter adapter = new MyCustomAdapter(follows);
                                    listView.setAdapter(adapter);
                                } catch (JSONException e) {
                                    Toast.makeText(Follow.this, "Couldn't refresh feed!", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Follow.this);
                builder.setMessage("Are you sure you wanna delete?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                return true;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateTask();
                swipeRefreshLayout.setRefreshing(true);
                String date = getRightDay(d);
                // Toast.makeText(Follow.this, d.toString(), Toast.LENGTH_SHORT).show();
                try {
                    follows = todayFilter(date);
                    MyCustomAdapter myadpter = new MyCustomAdapter(follows);
                    listView.setAdapter(myadpter);
                } catch (JSONException e) {
                    Toast.makeText(Follow.this, "Can't get feed!", Toast.LENGTH_SHORT).show();
                } finally {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Follow.this, AddFollow.class);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Follow Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.shaban.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Follow Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.shaban.myapplication/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    class MyCustomAdapter extends BaseAdapter {
        ArrayList<ListItem> Items = new ArrayList<ListItem>();

        MyCustomAdapter(ArrayList<ListItem> Items) {
            this.Items = Items;
        }

        @Override
        public int getCount() {
            return Items.size();
        }

        @Override
        public ListItem getItem(int position) {
            return Items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater linflater = getLayoutInflater();
            View view1 = linflater.inflate(R.layout.card_item, null);
            TextView txtname = (TextView) view1.findViewById(R.id.subject);
            TextView txtdes = (TextView) view1.findViewById(R.id.details);
            TextView txtdate = (TextView) view1.findViewById(R.id.date);
            TextView txtcomp = (TextView) view1.findViewById(R.id.completed);
            txtname.setText(Items.get(i).getSubject());
            txtdes.setText(Items.get(i).getMasage());
            txtdate.setText(Items.get(i).getDate());
            String completed = "Completed";
            if (!Items.get(i).isCompleted()) {
                completed = "Not " + completed;
            }
            txtcomp.setText(completed);
            return view1;
        }
    }

    public void printDate(Date d) {
        String currentTime = getRightDate(d);
        Toast.makeText(Follow.this, currentTime, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.follow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.completed) {
            try {
                follows = completedFilter(d);
                MyCustomAdapter adapter = new MyCustomAdapter(follows);
                listView.setAdapter(adapter);
            } catch (JSONException e) {
                Toast.makeText(Follow.this, "Couldn't get feed!", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.signout) {
            session.logoutUser();
            executor.shutdown();
            tasking.clearTasks();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void closeActivities() {
        RegisterActivity act1 = new RegisterActivity();
        LoginActivity act2 = new LoginActivity();
        act1.finish();
        act2.finish();
    }

    private void updateTask() {
        String URL = "http://1-dot-sh3lwaan.appspot.com/services";
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tasking.clearTasks();
                tasking.addTasks(response);
                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(Follow.this, "Can't get feed!", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile", "True");
                params.put("email", email.trim());
                return params;
            }
        };
        requestQueue.add(sr);
    }

    private static String getRightDate(Date d) {
        StringBuilder time = new StringBuilder("");
        String[] t = d.toString().split(" ");
        for (int i = 0; i < t.length; i++) {
            if (i == 4) {
            } else if (i == 3) {
                String[] hours = t[i].split(":");
                time.append(hours[0] + ":" + hours[1] + ".");
            } else {
                time.append(t[i] + ".");
            }
        }
        return time.toString();
    }

    public static String returnTime(String time) {
        time = time.replace(".", " ");
        return time;
    }

    private ArrayList<ListItem> getFollows() throws JSONException {
        ArrayList<ListItem> follows = new ArrayList<ListItem>();
        JSONArray jsonArray = tasking.getTasks();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            follows.add(new ListItem(jsonObject.getString("id"), jsonObject.getString("subject"), jsonObject.getString("details")
                    , jsonObject.getString("time"), jsonObject.getBoolean("completed")));
        }
        return follows;
    }


    private static String getRightDay(Date d) {
        String[] time = d.toString().split(" ");
        String result = time[0] + '.' + time[1] + '.' + time[2];
        return result;
    }

    private static String getRightTime(String d) {
        String[] time = d.split("\\.");
        String result = time[0] + '.' + time[1] + '.' + time[2];
        return result;
    }

    private ArrayList<ListItem> todayFilter(String date) throws JSONException {
        ArrayList<ListItem> filtered = new ArrayList<ListItem>();
        JSONArray jsonArray = tasking.getTasks();
        String time = "";
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            time = getRightTime(jsonObject.getString("time"));
            if (date.matches(time)) {
                filtered.add(new ListItem(jsonObject.getString("id"), jsonObject.getString("subject"), jsonObject.getString("details"),
                        jsonObject.getString("time"), jsonObject.getBoolean("completed")));
            }
        }
        return filtered;
    }

    private ArrayList<ListItem> completedFilter(Date d) throws JSONException {
        String date = getRightDay(d);

        ArrayList<ListItem> filtered = new ArrayList<ListItem>();
        JSONArray jsonArray = tasking.getTasks();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String time = getRightTime(jsonObject.getString("time"));
            if (jsonObject.getBoolean("completed") && time.matches(date)) {
                filtered.add(new ListItem(jsonObject.getString("id"), jsonObject.getString("subject"), jsonObject.getString("details"),
                        jsonObject.getString("time"), jsonObject.getBoolean("completed")));
            }
        }
        return filtered;
    }

    private void deleteFollow(final String id) {

        StringRequest sr = new StringRequest(Request.Method.POST, "http://1-dot-sh3lwaan.appspot.com/delete", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(Follow.this, "Follow deleted successfully!", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Follow.this, "Couldn't delete Follow!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(sr);
    }

    public void createNotification(String subject, String details, String time) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, NotificationReceiverActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification noti = new Notification.Builder(this)
                .setContentTitle(details)
                .setContentText(subject).setSmallIcon(R.drawable.icon)
                .setContentIntent(pIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, noti);

    }
}
