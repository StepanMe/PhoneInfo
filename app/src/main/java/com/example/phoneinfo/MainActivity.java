package com.example.phoneinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    String phoneString;

    EditText etPhone;
    Button bSearch;
    Button bClear;
    TextView tvOperator;
    TextView tvRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setTitle(getString(R.string.main_activity_title));

        bSearch = findViewById(R.id.b_searchButton);
        etPhone = findViewById(R.id.et_phoneNumber);
        tvOperator = findViewById(R.id.tv_operator);
        tvRegion = findViewById(R.id.tv_region);
        bClear = findViewById(R.id.b_clear);

        View.OnClickListener searchClick = view -> {
            phoneString = etPhone.getText().toString();
            if (phoneString.length() == 0) {
                Toast.makeText(this,"Введите номер телефона\nв формате +7XXXXXXXXXX",Toast.LENGTH_LONG).show();
                return;
            }
            String requestUrl = "https://num.voxlink.ru/get/?num=" + phoneString.replaceAll("[^+0-9]","");
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    PhoneNumber p = gson.fromJson(response.toString(),PhoneNumber.class);
                    tvOperator.setText(String.format(getString(R.string.tv_operator_title),p.getOperator()));
                    tvRegion.setText(String.format(getString(R.string.tv_region_title),p.getRegion()));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Если сервер вернул ответ с кодом 404, то это не совсем ошибка
                    // Просто сообщаем, что номер введён неправильно или номер не найден
                    if ("404".equals(String.valueOf(error.networkResponse.statusCode))) {
                        String errResponseBody = null;
                        try {
                            //Пробуем преобразовать содержимое ответа сервера из массива byte'ов в строку
                            errResponseBody = new String(error.networkResponse.data,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            Toast.makeText(MainActivity.this, "Произошла ошибка при обработке ответа от сервера", Toast.LENGTH_LONG).show();
                            Log.i("asd123","Ошибка при обработке содержимого ответа с кодом 404");
                            e.printStackTrace();
                        }

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson = gsonBuilder.create();
                        // Готовим сообщение для тоста
                        String toastMessage;
                        PhoneError phoneError = gson.fromJson(errResponseBody,PhoneError.class);
                        switch (phoneError.ErrorType()) {
                            case "PHONE_NOT_FOUND": toastMessage = getString(R.string.error_phone_not_found);
                                break;
                            case "WRONG_PHONE_FORMAT": toastMessage = getString(R.string.error_wrong_phone_format);
                                break;
                            default: toastMessage = getString(R.string.error_wrong_format_or_not_found);
                        }
                        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                        Log.i("asd123","Содержимое ответа: " + errResponseBody);
                    //Если код ответа отличается от 404, действительно, какая-то ошибка
                    } else {
                        String toastMessage = null;
                        if (error instanceof TimeoutError) {
                            toastMessage = getString(R.string.error_server_response_timeout);
//                            Toast.makeText(MainActivity.this, "Сервер долго отвечает", Toast.LENGTH_LONG).show();
                            Log.i("asd123", "Сервер  долго отвечает: " + error.toString());
                        } else if (error instanceof NoConnectionError || error instanceof ServerError) {
                            toastMessage = getString(R.string.error_no_internet_connection);
//                            Toast.makeText(MainActivity.this, "Нет соединения с интернетом", Toast.LENGTH_LONG).show();
                            Log.i("asd123", "Нет соединения с сервером: " + error.toString());
                        } else {
                            toastMessage = getString(R.string.error_unknown);
                        }
                        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
        };

        View.OnClickListener clearClick = view -> {
            tvOperator.setText("");
            tvRegion.setText("");
            etPhone.setText("");
            etPhone.setSelection(0);
        };

        bClear.setOnClickListener(clearClick);
        bSearch.setOnClickListener(searchClick);
    }
}