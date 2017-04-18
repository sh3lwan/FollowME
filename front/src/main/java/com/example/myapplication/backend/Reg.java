package com.example.myapplication.backend;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.*;
import javax.xml.ws.RespectBinding;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

public class Reg {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Datastore datastore = DatastoreHelper.getDatastoreFromEnv();
    public boolean checkExist(HttpServletRequest req, String kind) {
        String email = req.getParameter("email");
        Entity newUser = new Entity(kind, email);
        try {
            datastore.get(newUser.getKey());
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    public Entity addUser(HttpServletRequest req) {
        // String name = req.getParameter("name");
        String email = req.getParameter("email");
        //String username = toHash(email);
        // int timeZone = Integer.valueOf(req.getParameter("time"));
        int suspended = 3;
        String access = randomName();
        Entity user = new Entity("UserModel", email);
        // user.setProperty("name", name);
        user.setProperty("token", access);
        user.setProperty("isVertified", false);
        user.setProperty("TimeZone", 2);
        user.setProperty("suspended", suspended);
        return user;
    }

    public boolean checkPassword(HttpServletRequest req, String password) {
        String inputPassword = req.getParameter("password");
        if (inputPassword == null) {
            return false;
        } else {
            return password.matches(inputPassword);
        }
    }

    public Entity getUser(HttpServletRequest req) {
        String email = req.getParameter("email");
        Entity user = new Entity("UserModel", email);
        try {
            return datastore.get(user.getKey());
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public boolean delUser(HttpServletRequest req) {
        String email = req.getParameter("email");
        Entity user = new Entity("UserModel", email);
        datastore.delete(user.getKey());
        try {
            datastore.get(user.getKey());
            return false;
        } catch (EntityNotFoundException e) {
            return true;
        }
    }

    private static String randomName() {
        String alphaCapital = "ABCDEFGHIJKLMNOPQRSTUVWXYX";
        String alphaSmall = alphaCapital.toLowerCase();
        String result = "";
        int j;
        int one;
        for (int i = 0; i < 80; i++) {
            one = (int) (Math.random() * 2);
            j = (int) (Math.random() * 26);
            if (one == 0) {
                result += alphaCapital.charAt(j);
            } else {
                result += alphaSmall.charAt(j);
            }

        }
        return result;
    }

    public boolean setSuspended(HttpServletRequest req, int i) {
        String email = req.getParameter("email");
        Entity user = new Entity("UserModel", email);
        try {
            if (i < 0) {
                datastore.get(user.getKey()).setProperty("suspended", 0);
            } else {
                datastore.get(user.getKey()).setProperty("suspended", i);
            }
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    public static String toHash(String plaintext) {
        MessageDigest m;
        String hashtext = null;
        try {
            m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(plaintext.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            hashtext = bigInt.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        return hashtext;
    }
}
