package com.example.phoneinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    String phoneString;

    TextView tvOperator;
    TextView tvRegion;

    EditText etPhone;
    ImageButton bClear;
    Button bSearch;

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
            // Скрываем клавиатуру
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(bSearch.getWindowToken(),inputMethodManager.HIDE_NOT_ALWAYS);

            // Очищаем строку с номером телефона от ненужных символов, оставляем только цифры
            phoneString = etPhone.getText().toString().replaceAll("[^0-9]","");;
            // Если строка пустая, просим пользователя ввести номер телефона
            if (phoneString.length() == 0) {
                Toast.makeText(this,R.string.toast_error_enter_correct_phone,Toast.LENGTH_LONG).show();
                return;
            }
            // Формируем URL (строку) для запроса
            String requestUrl = "https://num.voxlink.ru/get/?num=" + phoneString;
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
                // В случае успеха
                @Override
                public void onResponse(JSONObject response) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    PhoneNumber p = gson.fromJson(response.toString(),PhoneNumber.class);
                    tvOperator.setText(String.format(getString(R.string.tv_operator_title),p.getOperator()));
                    tvRegion.setText(String.format(getString(R.string.tv_region_title),p.getRegion()));
                }
            }, new Response.ErrorListener() {
                // В случае ошибки
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Если сервер вернул ответ с кодом 404, то это не совсем ошибка
                    // Просто сообщаем, что номер введён неправильно или номер не найден
                    if ("404".equals(String.valueOf(error.networkResponse.statusCode))) {
                        String errResponseBody;
                        //Преобразуем содержимое ответа сервера из массива byte'ов в строку
                        errResponseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson = gsonBuilder.create();
                        // Готовим текст сообщения для Toast'а
                        String toastMessage;
                        PhoneError phoneError = gson.fromJson(errResponseBody,PhoneError.class);
                        switch (phoneError.ErrorType()) {
                            case "PHONE_NOT_FOUND": toastMessage = getString(R.string.toast_error_phone_not_found);
                                break;
                            case "WRONG_PHONE_FORMAT": toastMessage = getString(R.string.toast_error_wrong_phone_format);
                                break;
                            default: toastMessage = getString(R.string.toast_error_wrong_format_or_not_found);
                        }

                        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();

                        // Если возникла такая ошибка, очищаем результаты возможного предыдущего корректного запроса
                        tvOperator.setText("");
                        tvRegion.setText("");

                        Log.i("asd123","Содержимое ответа: " + errResponseBody);
                    // Если код ответа отличается от 404, действительно, какая-то ошибка
                    // Сообщаем об этом пользователю
                    } else {
                        String toastMessage;
                        if (error instanceof TimeoutError) {
                            toastMessage = getString(R.string.toast_error_server_response_timeout);
                            Log.i("asd123", "Сервер  долго отвечает: " + error.toString());
                        } else if (error instanceof NoConnectionError || error instanceof ServerError) {
                            toastMessage = getString(R.string.toast_error_no_internet_connection);
                            Log.i("asd123", "Нет соединения с сервером: " + error.toString());
                        } else {
                            toastMessage = getString(R.string.toast_error_unknown);
                            Log.i("asd123", "Ошибка в ответе сервера: " + error.toString());
                        }
                        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
        };

        View.OnClickListener clearClick = view -> {
            // Очищаем название оператора, название региона и поле поиска
            tvOperator.setText("");
            tvRegion.setText("");
            etPhone.setText("");

            // Показываем клавиатуру
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(etPhone,0);
        };

        bClear.setOnClickListener(clearClick);
        bSearch.setOnClickListener(searchClick);
    }
}