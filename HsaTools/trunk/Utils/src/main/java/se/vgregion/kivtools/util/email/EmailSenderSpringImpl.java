/**
 * Copyright 2009 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 */
package se.vgregion.kivtools.util.email;

import java.util.List;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import se.vgregion.kivtools.util.Arguments;

/**
 * An implementation of the EmailSender interface using Spring as the backend.
 * 
 * @author Joakim Olsson
 */
public class EmailSenderSpringImpl implements EmailSender {
  private MailSender mailSender;

  public void setMailSender(MailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendEmail(String fromAddress, List<String> recipientAddresses, String subject, String body) {
    Arguments.notEmpty("fromAddress", fromAddress);
    Arguments.notEmpty("recipientAddresses", recipientAddresses);
    Arguments.notEmpty("subject", subject);
    Arguments.notEmpty("body", body);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipientAddresses.toArray(new String[recipientAddresses.size()]));
    message.setSubject(subject);
    message.setText(body);
    mailSender.send(message);
  }
}
