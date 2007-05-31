/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

import java.io.IOException;
import java.util.Date;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.payment.PaymentServiceProcess;
import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.UserEntity;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("paymentServiceProcess")
@LocalBinding(jndiBinding = "SafeOnlinePaymentDemo/PaymentServiceProcessBean/local")
public class PaymentServiceProcessBean implements PaymentServiceProcess {

	public static final String SAFE_ONLINE_LOCATION = "localhost";

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@PersistenceContext(unitName = "DemoPaymentEntityManager")
	private EntityManager entityManager;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@In(value = "username", required = false)
	private String username;

	@In("user")
	private String user;

	@In("recipient")
	private String recipient;

	@In("amount")
	private Double amount;

	@In(value = "message", required = false)
	private String message;

	@In("target")
	private String target;

	private String visa;

	@SuppressWarnings("unused")
	@Out(value = "visaNumber", required = false)
	private String visaNumber;

	public String authenticate() {
		log.debug("authenticate");
		String result = SafeOnlineLoginUtils.login(this.facesMessages,
				this.log, "cards.seam");
		return result;
	}

	public String commit() {
		log.debug("commit");
		if (null == this.username) {
			this.facesMessages.add("username is null. user not authenticated");
			return null;
		}
		if (false == this.user.equals(this.username)) {
			this.facesMessages.add("authenticated user != requested user");
			return null;
		}
		UserEntity user = this.entityManager.find(UserEntity.class,
				this.username);
		if (user == null) {
			user = new UserEntity(this.username);
			this.entityManager.persist(user);
		}

		Date paymentDate = new Date();
		PaymentEntity newPayment = new PaymentEntity();
		newPayment.setRecipient(this.recipient);
		newPayment.setAmount(this.amount);
		newPayment.setMessage(this.message);
		newPayment.setPaymentDate(paymentDate);
		newPayment.setVisa(this.visa);
		newPayment.setOwner(user);

		this.entityManager.persist(newPayment);

		this.visaNumber = this.visa;

		return "success";
	}

	public String done() {
		log.debug("done. redirect to #0", this.target);
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		Seam.invalidateSession();
		try {
			externalContext.redirect(this.target);
		} catch (IOException e) {
			this.facesMessages.add("redirect error");
		}
		return null;
	}

	public String getVisa() {
		return this.visa;
	}

	public void setVisa(String visa) {
		this.visa = visa;
	}
}
