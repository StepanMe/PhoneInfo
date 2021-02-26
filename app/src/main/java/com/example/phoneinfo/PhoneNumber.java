package com.example.phoneinfo;

public class PhoneNumber {

    private String info;
    private String code;
    private String num;
    private String full_num;
    private String operator;
    private String old_operator;
    private String region;
    private String number;

    //Если у номера не было смены оператора
    public PhoneNumber (String code, String num, String full_num, String operator, String region) {
        this.code = code;
        this.num = num;
        this.full_num = full_num;
        this.operator = operator;
        this.old_operator = null;
        this.region = region;
    }

    //Если номер ПЕРЕХОДИЛ от одного оператора к другому (добавляется old_operator)
    public PhoneNumber (String code, String num, String full_num, String operator, String old_operator, String region) {
        this.code = code;
        this.num = num;
        this.full_num = full_num;
        this.operator = operator;
        this.old_operator = old_operator;
        this.region = region;
    }

    public PhoneNumber (String full_num, String operator, String region) {
        this.number = full_num;
        this.operator = operator;
        this.region = region;
    }

    public String getRegion(){
        return this.region;
    }

    public String getOperator(){
        return this.operator;
    }

    public String getFullNumber(){
        return this.full_num;
    }

    public String getOldOperator(){
        return this.old_operator;
    }

    public boolean hasInfoField(){
        return (this.info != null);
    }

    public boolean isPorted(){
        return !(this.old_operator == null);
    }

}
