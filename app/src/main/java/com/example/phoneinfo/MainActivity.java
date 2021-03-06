package com.example.phoneinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class MainActivity extends AppCompatActivity {
    String phoneString;

    ProgressBar progressBar;
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

        tvOperator = findViewById(R.id.tv_operator);
        tvRegion = findViewById(R.id.tv_region);
        bClear = findViewById(R.id.b_clear);
        bSearch = findViewById(R.id.b_searchButton);
        etPhone = findViewById(R.id.et_phoneNumber);
        progressBar = findViewById(R.id.progress_bar);

        // Добавляем маску и "слушатель" на поле ввода номера
        MaskImpl mask = MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER);
        FormatWatcher watcher = new MaskFormatWatcher(mask);
        watcher.installOnAndFill(etPhone);

        //Ставим курсор в поле ввода номера при запуске приложения
        etPhone.requestFocus();

        View.OnClickListener searchClick = view -> {

            // Прячем курсор
            etPhone.clearFocus();
            // Очищаем результаты предыдущего, возможно, корректного запроса
            tvOperator.setText("");
            tvRegion.setText("");
            // Показываем прогресс-бар
            progressBar.setVisibility(View.VISIBLE);

            // Скрываем клавиатуру
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(etPhone.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            // Очищаем строку с номером телефона от ненужных символов, оставляем только цифры
            phoneString = etPhone.getText().toString().replaceAll("[^0-9]","");
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
                    // Скрываем прогресс-бар
                    progressBar.setVisibility(View.INVISIBLE);
                    // Выводим полученный текст
                    tvOperator.setText(String.format(getString(R.string.tv_operator_title),p.getOperator()));
                    tvRegion.setText(String.format(getString(R.string.tv_region_title),p.getRegion()));
                }
            }, new Response.ErrorListener() {
                // В случае ошибки
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.INVISIBLE);
                    // Если сервер вернул ответ с кодом 404, то это не совсем ошибка
                    // Просто сообщаем, что номер введён неправильно или номер не найден
                    if ("404".equals(String.valueOf(error.networkResponse.statusCode))) {
                        String errResponseBody;
                        // Преобразуем содержимое ответа сервера из массива byte'ов в строку
                        errResponseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson = gsonBuilder.create();
                        // Если возникла  ошибка, готовим текст сообщения для Toast'а
                        String toastMessage;
                        PhoneError phoneError = gson.fromJson(errResponseBody,PhoneError.class);
                        switch (phoneError.ErrorType()) {
                            case "PHONE_NOT_FOUND": toastMessage = getString(R.string.toast_error_phone_not_found);
                                break;
                            case "WRONG_PHONE_FORMAT": toastMessage = getString(R.string.toast_error_wrong_phone_format);
                                break;
                            default: toastMessage = getString(R.string.toast_error_wrong_format_or_not_found);
                        }
                        // Сообщаем...
                        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                        Log.i("asd123","Содержимое ответа: " + errResponseBody);
                    // Если код ответа отличается от 404, действительно, какая-то ошибка
                    // Выявляем её и сообщаем пользователю
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
            }) {
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap();
                    params.put("User-Agent","PhoneInfo; Author: github.com/StepanMe/");
                    params.put("Accept","application/json");
                    params.put("accept-language","ru-RU,ru");
                    return params;
                }
            };
            requestQueue.add(jsonObjectRequest);
        };

        View.OnClickListener clearClick = view -> {
            // Очищаем название оператора, название региона и поле поиска
            tvOperator.setText("");
            tvRegion.setText("");
            etPhone.setText("");
            // Вставляем курсор в конец строки
            etPhone.requestFocus();
            etPhone.setSelection(etPhone.getText().toString().length());

            // Показываем клавиатуру
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(etPhone,InputMethodManager.SHOW_FORCED);
        };

        // Вешаем события на кнопки
        bClear.setOnClickListener(clearClick);
        bSearch.setOnClickListener(searchClick);

        // Добавляем прослушку нажатия кнопки на виртуальной клавиатуре
        // Если нажата кнопка Поиск...
        etPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // IME_ACTION_SEARCH = 3
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchClick.onClick(etPhone);
                }
                return false;
            }
        });
    }
}