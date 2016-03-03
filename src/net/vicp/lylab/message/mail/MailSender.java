package net.vicp.lylab.message.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

/**
 * 简单邮件（不带附件的邮件）发送器
 */
public class MailSender extends NonCloneableBaseObject {
	
	// 邮件发送服务器的用户名
	private String userName;
	// 邮件发送服务器的密码
	private String password;
	// 邮件发送服务器的域名
	private String smtpServer;
	// 邮件发送服务器的端口
	// SSL=465, normal=25
	private int smtpPort = 465;

	public Properties getSesstionProperties() {
		Properties p = new Properties();
		p.put("mail.smtp.host", smtpServer);
		p.put("mail.smtp.port", String.valueOf(smtpPort));
		p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		p.put("mail.smtp.auth", "true");
//		p.put("mail.imap.ssl.enable", "true");
		return p;
	}

	public void sendMail(Mail mail) {
		try {
			// 根据mail创建一个邮件消息
			Message message = encode(mail);
			// 发送邮件
			Transport.send(message);
		} catch (Exception e) {
			throw new LYException("Send email failed", e);
		}
	}

	public Message encode(Mail mail) {
		try {
			// 根据session创建一个邮件消息
			Message message = new MimeMessage(
					Session.getDefaultInstance(getSesstionProperties(), new MailAuthenticator(userName, password)));
			// 创建邮件发送者地址
			Address from = new InternetAddress(mail.getFromAddress());
			// 设置邮件消息的发送者
			message.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
			Address to = new InternetAddress(mail.getToAddress());
			message.setRecipient(Message.RecipientType.TO, to);
			// 设置邮件消息的主题
			message.setSubject(mail.getSubject());
			// 设置邮件消息发送的时间
			message.setSentDate(new Date());

			if (mail.isPlainText()) {
				// MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
				Multipart mainPart = new MimeMultipart();
				// 创建一个包含HTML内容的MimeBodyPart
				BodyPart html = new MimeBodyPart();
				// 设置HTML内容
				html.setContent(mail.getContent(), "text/html; charset=utf-8");
				mainPart.addBodyPart(html);
				// 将MiniMultipart对象设置为邮件内容
				message.setContent(mainPart);
			} else {
				// 设置邮件消息的主要内容
				message.setText(mail.getContent());
			}

			// 设置邮件消息的主要内容
			String mailContent = mail.getContent();
			message.setText(mailContent);
			return message;
		} catch (Exception e) {
			throw new LYException("Encode mail message failed", e);
		}
	}

	class MailAuthenticator extends Authenticator {
		String userName = null;
		String password = null;

		public MailAuthenticator(String userName, String password) {
			if (userName == null)
				throw new LYException("Parameter username is null");
			if (password == null)
				throw new LYException("Parameter password is null");
			this.userName = userName;
			this.password = password;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(userName, password);
		}

	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
