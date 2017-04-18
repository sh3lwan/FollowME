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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // Email, password edittext
    AppCompatButton loginButton;
    EditText txtUsername, txtPassword;
    SessionManager session;
    String email, password, URL;
    RequestQueue requestQueue;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        txtUsername = (EditText) findViewById(R.id.input_email);
        txtPassword = (EditText) findViewById(R.id.input_password);
        loginButton = (AppCompatButton) findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
        requestQueue = Volley.newRequestQueue(this);
        session = new SessionManager(this);
        progress = new ProgressDialog(this);
        if (session.isLoggedIn()) {
            Intent intent = new Intent(this, Follow.class);
            startActivity(intent);
            finish();
        }
        final TextView createLink = (TextView) findViewById(R.id.link_signup);
        createLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    public void onLogin() {
        final String URL = "http://1-dot-sh3lwaan.appspot.com/Login";
        Check check = new Check();
        email = txtUsername.getText().toString();
        password = txtPassword.getText().toString();
        boolean emailValid = check.isValidEmail(email);
        if (email.trim().equals("") || !emailValid || password.equals("")) {
            Toast.makeText(LoginActivity.this, "Enter fields correctly!", Toast.LENGTH_SHORT).show();
        } else {
            progress.show();
            StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.matches("Done")) {
                        session.createLoginSession(email, password);
                        Intent intent = new Intent(LoginActivity.this, Follow.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        progress.dismiss();
                        finish();
                    } else {
                        progress.dismiss();
                        Toast.makeText(LoginActivity.this, "Please try later", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.dismiss();
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {
                        String statusCode = String.valueOf(networkResponse.statusCode);
                        if (statusCode.matches("404")) {
                            Toast.makeText(LoginActivity.this, "Email Or Password Invalid", Toast.LENGTH_SHORT).show();
                        } else if (statusCode.matches("303")) {
                            Toast.makeText(LoginActivity.this, "Please vertify your account first!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Please try agian later!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                    }
                }

            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("email", email);
                    params.put("password", password);
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
    }


}
