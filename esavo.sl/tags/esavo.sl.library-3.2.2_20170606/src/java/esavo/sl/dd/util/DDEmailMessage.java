/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.sl.dd.util;


import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

//log4j classes
 import org.apache.log4j.Logger;


/** Used to handle email messages in a machine-independent context
*   using the javamail API
**/
public class DDEmailMessage {

	static Logger logger = Logger.getLogger(DDEmailMessage.class);

	String smtpHost;
	String fromAddress;
	List<String> toAddress;
	List<String> ccAddress;
	String subject;
	String msgText;
	boolean debug = false;

	public DDEmailMessage(String smtpHost, String fromAddress, List<String> toAddress, List<String> ccAddress, String subject,
			String msgText) {

		this.smtpHost = smtpHost;
		this.fromAddress = fromAddress;
		this.toAddress = toAddress;
		this.ccAddress = ccAddress;
		this.subject = subject;
		this.msgText = msgText;

	}

	public void send() {

		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);

		// Create some properties and get the default session

		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);

		try {

			// Create a message
			Message msg = new MimeMessage(session);

			// Set the "from" address

			InternetAddress from = new InternetAddress(fromAddress);
			msg.setFrom(from);

			// Set the "to" address

			for (int i = 0; i < toAddress.size(); i++) {
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(this.toAddress.get(i)));
			}

			Address[] myToRecipients = msg.getRecipients(Message.RecipientType.TO);
			msg.setRecipients(Message.RecipientType.TO, myToRecipients);

			// Set the "cc" address
			for (int i = 0; i < ccAddress.size(); i++) {
				msg.addRecipient(Message.RecipientType.CC, new InternetAddress(this.ccAddress.get(i)));
			}
			Address[] myCcRecipients = msg.getRecipients(Message.RecipientType.CC);
			msg.setRecipients(Message.RecipientType.CC, myCcRecipients);

			// logger.info("Recipients "+msg.getAllRecipients());
			// Set the subject

			msg.setSubject(subject);

			// Set the content of the mail to plain text

			// logger.info("Message Text:"+msgText);

			msg.setContent(msgText, "text/plain");

			// Send the message
			// for (int i=0;i<10000000;i++){}

			Transport.send(msg);

			logger.info("Message: '" + this.subject + "'  sent.");

		} catch (MessagingException mex) {
			logger.error(this.getClass().getName() + " - Failure sending mail...:");
			mex.printStackTrace();
		}

	}

}
