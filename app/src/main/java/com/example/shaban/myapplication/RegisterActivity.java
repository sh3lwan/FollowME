package com.example.shaban.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class RegisterActivity extends AppCompatActivity {
    EditText editText;
    RequestQueue requestQueue;
    String email, URL;
    AppCompatButton registerButton;
    TextView linkLoginButton;
    SessionManager session;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        editText = (EditText) findViewById(R.id.input_email);
        requestQueue = Volley.newRequestQueue(this);
        registerButton = (AppCompatButton) findViewById(R.id.btn_signup);
        linkLoginButton = (TextView) findViewById(R.id.link_login);
        session = new SessionManager(this);
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (session.isLoggedIn()) {
            Intent intent = new Intent(this, Follow.class);
            startActivity(intent);
            finish();
        }
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegister();
            }
        });
        linkLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    public void onRegister() {
        email = editText.getText().toString();
        Check checkEmail = new Check();
        if (email.trim().equals("") || !checkEmail.isValidEmail(email)) {
            Toast.makeText(RegisterActivity.this, "Enter Valid email Please!", Toast.LENGTH_SHORT).show();

        } else {

            URL = String.format("http://1-dot-sh3lwaan.appspot.com/Register?email=%1$s", email);
            progress.show();

            StringRequest myRequest = new StringRequest(Request.Method.GET, URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(RegisterActivity.this,LoginActivity.class);
                            progress.dismiss();
                            startActivity(i);
                            finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.dismiss();
                    NetworkResponse response = error.networkResponse;
                    // Toast.makeText(RegisterActivity.this, error + "", Toast.LENGTH_SHORT).show();
                    if (response == null) {
                        Toast.makeText(RegisterActivity.this, "Check network connection!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (response.statusCode == 404) {
                            Toast.makeText(RegisterActivity.this, "Email not Available!", Toast.LENGTH_SHORT).show();
                        }
                       // Toast.makeText(RegisterActivity.this, response.statusCode + "", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            requestQueue.add(myRequest);
        }
    }


}

