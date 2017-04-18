package com.example.shaban.myapplication;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddFollow extends AppCompatActivity {
    static int mYear;
    static int mMonth;
    static int mDay;
    static int mHour, mMinute;
    String txtDate, txtTime;
    RequestQueue requestQueue;
    String email, subject, details, URL;
    boolean sms, task;
    TextView mTxtDisplay;
    EditText subjectEdit;
    EditText detailsEdit;
    AppCompatButton submitButton;
    SessionManager session;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_newfollow);
        subjectEdit = (EditText) findViewById(R.id.Subject);
        detailsEdit = (EditText) findViewById(R.id.Details);
        requestQueue = Volley.newRequestQueue(this);
        submitButton = (AppCompatButton) findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFollow();
            }
        });
        session = new SessionManager(this);
        session.checkLogin();
        HashMap<String, String> userDetails = session.getUserDetails();
        email = userDetails.get(SessionManager.KEY_EMAIL);
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int hight = dm.heightPixels;
        getWindow().setLayout((int) width, (int) (hight * .70));
        getWindow().setGravity(Gravity.BOTTOM);
    }


    Date d = new Date();

    public void Datepick(View view) {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        //System.out.println(mYear+":"+mMonth+":"+mDay+" >>>>>>>>>>>");
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        d.setYear(year - 1900);
                        d.setDate(dayOfMonth);
                        d.setMonth(monthOfYear);
                        // System.out.println(d.toString());

                        //txtDate=(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        //System.out.println(txtDate);
                    }
                }, mYear, mMonth, mDay);

        dpd.show();

    }

    public void timepicker(View view) {
        TimePickerDialog tpd = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        //txtTime=(hourOfDay + ":" + minute);
                        d.setHours(hourOfDay);
                        d.setMinutes(minute);
                    }
                }, mHour, mMinute, false);
        tpd.show();

    }


    private void addFollow() {
        subject = subjectEdit.getText().toString();
        details = detailsEdit.getText().toString();
        //sms = smsEdit.isChecked();
        //task = taskEdit.isChecked();
        // System.out.println(sms+" ..."+task);
        progress.show();
        StringRequest sr = new StringRequest(Request.Method.POST, "http://1-dot-sh3lwaan.appspot.com/follows_servlet", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(AddFollow.this, "Couldn't add Follow!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                String ss = mDay + "-" + mMonth + "-" + mYear + " " + mHour + ":" + mMinute;

                Map<String, String> params = new HashMap<String, String>();
                Date currentDate = new Date();
                currentDate.setMinutes(currentDate.getMinutes() + 2);
                params.put("mobile", "1");
                params.put("subject", subject);
                params.put("details", details);
                params.put("email", email);
                //  params.put("sms1", sms + "");
                //params.put("task1", task + "");
                params.put("when", parseDate(d));
                //System.out.println(parseDate(d)+"*********************************8");
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

    public static String parseDate(Date myDate) {
        SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String strDate = sm.format(myDate);
        return strDate;
    }

}
