package com.example.phoneinfo;

public class PhoneError {

    private String info;
    private String[] example;

    public String getInfo() {
        return info;
    }

    public String ErrorType() {
        if (this.info.contains("Номер не найден. Проверьте код города") == true) {
            return "PHONE_NOT_FOUND";
        } else if (this.info.contains("Неверный формат номера") == true) {
            return "WRONG_PHONE_FORMAT";
        } else {
            return "WRONG_OR_NOT_FOUND";
        }
    }
}
