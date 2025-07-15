package com.example.demo.utils;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class ConvertDate {

    public  String currentDateShamsi() {
        DateConverter dateConverter = new DateConverter();
        String date = dateConverter.nowAsJalali().toString();
        String[] parts = date.split("-");
        String year = parts[0];
        String month = String.format("%02d", Integer.parseInt(parts[1]));
        String day = String.format("%02d", Integer.parseInt(parts[2]));
        return year + month + day;
    }
    public  String currentTimeShamsi() {
//
        OffsetDateTime tehranTime = OffsetDateTime.now(ZoneOffset.ofHoursMinutes(3, 30));
        String formatted = tehranTime.format(DateTimeFormatter.ofPattern("HHmmss"));
        return formatted.toString();
    }
    public  String gregorianToJalali(int year,int month, int day){
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(year, month, day);
        return jalaliDate.toString();
    }
    public  String jalaliToGregorian(int year,int month, int day){
        DateConverter dateConverter = new DateConverter();
        LocalDate localdate = dateConverter.jalaliToGregorian(year, month, day);
        return localdate.toString();
    }
}
