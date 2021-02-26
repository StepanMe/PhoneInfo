package com.example.phoneinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.ClientError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    String phoneString;

    TextView phoneNumber;
    Button searchButton;
    TextView resultOperator;
    TextView resultRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = findViewById(R.id.b_searchButton);
        phoneNumber = findViewById(R.id.et_phoneNumber);
        resultOperator = findViewById(R.id.tv_operator);
        resultRegion = findViewById(R.id.tv_region);

        View.OnClickListener searchClick = view -> {
            phoneString = phoneNumber.getText().toString();
            String requestUrl = "https://num.voxlink.ru/get/?num=" + phoneString.replaceAll("[^+0-9]","");
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    PhoneNumber p = gson.fromJson(response.toString(),PhoneNumber.class);
                    resultOperator.setText(p.getOperator());
                    resultRegion.setText(p.getRegion());

//                    Log.i("asd123", "Всё ОК: " + response.toString());

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof ClientError) {
                        Toast.makeText(MainActivity.this,"Похоже, неправильно введён номер",Toast.LENGTH_LONG).show();
                    }
                    if (error instanceof TimeoutError) {
//                        Log.i("asd123","Сервер  долго отвечает: " + error.toString());
                    }
                    if (error instanceof NoConnectionError || error instanceof ServerError) {
                        Toast.makeText(MainActivity.this,"Нет соединения с интернетом/сервером",Toast.LENGTH_LONG).show();
//                        Log.i("asd123","Нет соединения с сервером: " + error.toString());
                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
        };
        searchButton.setOnClickListener(searchClick);
    }
}