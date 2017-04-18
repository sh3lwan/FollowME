package com.example.shaban.myapplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sha'ban on 4/17/2016.
 */
public class Check {
    public boolean isValidEmail(String line) {
        String pattern = "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        if (m.find()) {
            return true;
        } else {
            return false;
        }

    }
}
