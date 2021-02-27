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

        this.setTitle("");

        bSearch = findViewById(R.id.b_searchButton);
        etPhone = findViewById(R.id.et_phoneNumber);
        tvOperator = findViewById(R.id.tv_operator);
        tvRegion = findViewById(R.id.tv_region);
        bClear = findViewById(R.id.b_clear);

//        //Если в поле пусто, ставим в него курсор
//        Log.i("asd123", String.valueOf(phoneNumber.getText().toString().length()));
//        if (phoneNumber.getText().length() == 0){
//            phoneNumber.setSelection(0);
//        }

        View.OnClickListener searchClick = view -> {
            phoneString = etPhone.getText().toString();
            String requestUrl = "https://num.voxlink.ru/get/?num=" + phoneString.replaceAll("[^+0-9]","");
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    PhoneNumber p = gson.fromJson(response.toString(),PhoneNumber.class);
                    tvOperator.setText(String.format("Оператор: %s",p.getOperator()));
                    tvRegion.setText(String.format("Регион: %s",p.getRegion()));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: обрабатывать ошибки 404, исходя из сообщения в JSON-поле "info" (неправильный формат номера или номер не найден)
                    // Если сервер вернул ответ с кодом 404, то это не совсем ошибка
                    // Просто сообщаем, что номер введён неправильно или номер не найден
                    if ("404".equals(String.valueOf(error.networkResponse.statusCode))) {
                        Log.i("asd123", String.valueOf(error.networkResponse.statusCode));
                        Toast.makeText(MainActivity.this, "Неправильно введён номер\nили нет информации о номере", Toast.LENGTH_LONG).show();
                    //Если что-то отличное от кода 404, действительно, какая-то ошибка
                    } else {
                        if (error instanceof TimeoutError) {
                            Toast.makeText(MainActivity.this, "Сервер долго отвечает", Toast.LENGTH_LONG).show();
                            Log.i("asd123", "Сервер  долго отвечает: " + error.toString());
                        }
                        if (error instanceof NoConnectionError || error instanceof ServerError) {
                            Toast.makeText(MainActivity.this, "Нет соединения с интернетом", Toast.LENGTH_LONG).show();
                            Log.i("asd123", "Нет соединения с сервером: " + error.toString());
                        }
                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
        };

        View.OnClickListener clearClick = view -> {
            etPhone.setText("");
            etPhone.setSelection(0);
        };

        bClear.setOnClickListener(clearClick);
        bSearch.setOnClickListener(searchClick);
    }
}