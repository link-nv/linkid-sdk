package net.link.safeonline.messaging.bean;

import java.io.Serializable;

public class EndUserMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String destination;

	private String subject;

	private String message;

	public EndUserMessage(String destination, String subject, String message) {
		this.destination = destination;
		this.subject = subject;
		this.message = message;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
