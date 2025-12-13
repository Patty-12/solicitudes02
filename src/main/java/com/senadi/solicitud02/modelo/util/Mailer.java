/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.senadi.solicitud02.modelo.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 *
 * @author micharesp
 */
public class Mailer {

    public boolean sendEmail(String mailFrom, String mailTo, String subject, String htmlBody) throws FileNotFoundException, IOException {
        Properties properties = new Properties();

//        String password;
        Session session;

        properties.put("mail.smtp.host", "mail.senadi.gob.ec");
        properties.put("mail.smtp.ssl.trust", "mail.senadi.gob.ec");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", 25);
        properties.put("mail.smtp.mail.sender", mailFrom);
        properties.put("mail.smtp.user", "usuario");
        properties.put("mail.smtp.auth", "true");

        session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress((String) properties.get("mail.smtp.mail.sender")));
            
            
            String[] mails = getMails(mailTo, ", ");
            for (int i = 0; i < mails.length; i++) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(mails[i]));                
            }
            
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
            
            message.setSubject(subject);
            message.setText("Notificación");

//            URL weburl = new URL(url);
//            InputStream fi = weburl.openStream();

            Multipart mp = new MimeMultipart();

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html");
            mp.addBodyPart(htmlPart);

//            MimeBodyPart attachment = new MimeBodyPart();
//            DataSource dataSrc = new ByteArrayDataSource(fi, "application/pdf");
//            attachment.setDataHandler(new DataHandler(dataSrc));
//            attachment.setFileName(documentName);
//            mp.addBodyPart(attachment);

            message.setContent(mp);

            Transport t = session.getTransport("smtp");
            t.connect((String) properties.get("mail.smtp.user"), "password");
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            return true;
        } catch (MessagingException me) {
            System.out.println("Error: " + me);
            return false;
        }
    }
    
    public boolean sendEmailWithAttachDocument(InputStream attach, String mailFrom, String mailTo, String subject, String htmlBody, String documentName) throws FileNotFoundException, IOException {
        Properties properties = new Properties();

//        String password;
        Session session;

        properties.put("mail.smtp.host", "mail.senadi.gob.ec");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", 25);
        properties.put("mail.smtp.mail.sender", mailFrom);
        properties.put("mail.smtp.user", "usuario");
        properties.put("mail.smtp.auth", "true");

        session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress((String) properties.get("mail.smtp.mail.sender")));
            
            String[] mails = getMails(mailTo, ", ");
            for (int i = 0; i < mails.length; i++) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(mails[i]));
            }
            
            message.setSubject(subject);
            message.setText("Notificación");

//            URL weburl = new URL(url);
//            InputStream fi = weburl.openStream();

            Multipart mp = new MimeMultipart();

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html");
            mp.addBodyPart(htmlPart);

            MimeBodyPart attachment = new MimeBodyPart();
            DataSource dataSrc = new ByteArrayDataSource(attach, "application/pdf");
            attachment.setDataHandler(new DataHandler(dataSrc));
            attachment.setFileName(documentName);
            mp.addBodyPart(attachment);

            message.setContent(mp);

            Transport t = session.getTransport("smtp");
            t.connect((String) properties.get("mail.smtp.user"), "password");
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            return true;
        } catch (MessagingException me) {
            System.out.println("Error: " + me);
            return false;
        }
    }
    
    public String[] getMails(String mailscadena, String divisor) {
        String[] mails = mailscadena.split(divisor);
//        for (int i = 0; i < mails.length; i++) {
//            System.out.println(mails[i]);
//        }
        return mails;
    }
    
}
