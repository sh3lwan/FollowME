/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.example.myapplication.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.repackaged.com.google.api.client.util.store.DataStoreFactory;
import com.google.appengine.api.datastore.Entity;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.servlet.http.*;

public class MyServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        PrintWriter out = resp.getWriter();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        resp.setContentType("text/plain");
        String email = req.getParameter("email");
        if (email != null && isValidEmail(email)) {
            Reg reg = new Reg();
            boolean exist = reg.checkExist(req, "UserModel");
            if (exist) {
                out.print("Email Exist!");
            } else {
                Entity user = reg.addUser(req);
                Email emailSender = new Email();
                String message = "http://1-dot-folloupthen.appspot.com/Activation?token=";
                try {
                    String token = (String) user.getProperty("token");
                    message += token;
                    datastore.put(user);
                    //emailSender.send(email,"Vertification",message);
                    out.print("User Created Successfully!");
                } catch (Exception e) {
                    out.print("Error: "+e);
                }
            }
        } else {
           out.print("Email not Valid!");
        }
        out.close();
    }
    static boolean isValidEmail(String line) {
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
