package utils;

import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MonitoringMail {

	public void sendMail(String mailServer, String from, String[] to, String subject, String messageBody,
			String attachmentPath, String attachmentName) {

		boolean debug = false;
		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.EnableSSL.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.smtp.host", mailServer);
		props.put("mail.debug", "true");

		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "587");
		props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
		//props.setProperty("mail.smtp.socketFactory.port", "465");

		Authenticator auth = new SMTPAuthenticator();
		Session session = Session.getDefaultInstance(props, auth);

		session.setDebug(debug);

		try {

			Transport bus = session.getTransport("smtp");
			bus.connect();
			Message message = new MimeMessage(session);
			
			message.addHeader("X-Priority", "1");
			message.setFrom(new InternetAddress(from));
			InternetAddress[] addressTo = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++)
				addressTo[i] = new InternetAddress(to[i]);
			message.setRecipients(Message.RecipientType.TO, addressTo);
			message.setSubject(subject);

			BodyPart body = new MimeBodyPart();

			body.setContent(messageBody, "text/html");

			BodyPart attachment = new MimeBodyPart();
			DataSource source = new FileDataSource(attachmentPath);
			attachment.setDataHandler(new DataHandler(source));
			attachment.setFileName(attachmentName);
			MimeMultipart multipart = new MimeMultipart();
			multipart.addBodyPart(body);
			multipart.addBodyPart(attachment);
			message.setContent(multipart);
			Transport.send(message);
			System.out.println("Sucessfully Sent mail to All Users");
			bus.close();

		} catch (Exception mex) {
			mex.printStackTrace();
		}
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator {

		public PasswordAuthentication getPasswordAuthentication() {
			String username = TestConfig.from;
			String password = TestConfig.password;
			return new PasswordAuthentication(username, password);
		}
	}

}
