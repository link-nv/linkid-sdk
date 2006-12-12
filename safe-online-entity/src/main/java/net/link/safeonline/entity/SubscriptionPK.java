package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
public class SubscriptionPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private String application;

	private String subject;

	public SubscriptionPK() {
		// empty
	}

	public SubscriptionPK(String subject, String application) {
		this.subject = subject;
		this.application = application;
	}

	public SubscriptionPK(SubjectEntity subject, ApplicationEntity application) {
		this.subject = subject.getLogin();
		this.application = application.getName();
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof SubscriptionPK) {
			return false;
		}
		SubscriptionPK rhs = (SubscriptionPK) obj;
		return new EqualsBuilder().append(this.subject, rhs.subject).append(
				this.application, rhs.application).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.subject).append(
				this.application).hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("subject", this.subject)
				.append("application", this.application).toString();
	}
}
