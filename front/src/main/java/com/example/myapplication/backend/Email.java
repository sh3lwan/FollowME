package com.example.myapplication.backend;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Crunchify.com
 *
 */
public class Email {

	Properties props = new Properties();
	Session session = Session.getDefaultInstance(props, null);

	public void send( String reciever, String subject,
			String msgBody) throws UnsupportedEncodingException,
			MessagingException {

		Message msg = new MimeMessage(session);
		// msg.setHeader("Header", "Header1");
		UserService userService = UserServiceFactory.getUserService();
		String email = userService.getCurrentUser().getEmail();
		msg.setFrom(new InternetAddress(email));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
				reciever, "Mr.User"));
		msg.setSubject(subject);
		msg.setContent(msgBody, "text/html");
		// msg.setText(msgBody);
		Transport.send(msg);
	}

}
