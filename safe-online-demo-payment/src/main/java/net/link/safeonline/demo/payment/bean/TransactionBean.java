/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

import java.net.ConnectException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.payment.Transaction;
import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.UserEntity;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;

import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("transactionBean")
@Scope(ScopeType.CONVERSATION)
@LocalBinding(jndiBinding = "SafeOnlinePaymentDemo/TransactionBean/local")
@SecurityDomain("demo-payment")
public class TransactionBean implements Transaction {

	public static final String SAFE_ONLINE_LOCATION = "localhost";

	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(TransactionBean.class);

	@Logger
	private Log log;

	@Resource
	private SessionContext sessionContext;

	@PersistenceContext(unitName = "DemoPaymentEntityManager")
	private EntityManager entityManager;

	public static final String NEW_PAYMENT_NAME = "newPayment";

	@In(value = NEW_PAYMENT_NAME, required = false)
	private PaymentEntity newPayment;

	private transient AttributeClient attributeClient;

	private PrivateKey privateKey;

	private X509Certificate certificate;

	@In(create = true)
	FacesMessages facesMessages;

	@PostConstruct
	public void postConstructCallback() {
		PrivateKeyEntry privateKeyEntry = DemoPaymentKeyStoreUtils
				.getPrivateKeyEntry();
		this.privateKey = privateKeyEntry.getPrivateKey();
		this.certificate = (X509Certificate) privateKeyEntry.getCertificate();
		this.attributeClient = new AttributeClientImpl(SAFE_ONLINE_LOCATION,
				this.certificate, this.privateKey);
	}

	@PrePassivate
	public void prePassivateCallback() {
		// next is not really required
		this.attributeClient = null;
	}

	@PostActivate
	public void postActivateCallback() {
		this.attributeClient = new AttributeClientImpl(SAFE_ONLINE_LOCATION,
				this.certificate, this.privateKey);
	}

	private String getUsername() {
		Principal principal = this.sessionContext.getCallerPrincipal();
		String name = principal.getName();
		log.debug("username #0", name);
		return name;
	}

	@RolesAllowed("user")
	public String confirm() {
		log.debug("confirm");
		LOG.debug("confirm");
		UserEntity user = this.entityManager.find(UserEntity.class, this
				.getUsername());
		if (user == null) {
			user = new UserEntity(this.getUsername());
			this.entityManager.persist(user);
		}

		Date paymentDate = new Date();
		this.newPayment.setPaymentDate(paymentDate);
		this.newPayment.setOwner(user);

		this.entityManager.persist(this.newPayment);

		return "confirmed";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}

	@Factory(NEW_PAYMENT_NAME)
	@RolesAllowed("user")
	public PaymentEntity newPaymentEntityFactory() {
		return new PaymentEntity();
	}

	@Factory("visas")
	@RolesAllowed("user")
	public List<SelectItem> visasFactory() {
		log.debug("visas factory");
		String username = getUsername();
		String[] values;
		try {
			values = this.attributeClient.getAttributeValue(username,
					"urn:net:lin-k:safe-online:attribute:visaCardNumber",
					String[].class);
		} catch (AttributeNotFoundException e) {
			String msg = "attribute not found: " + e.getMessage();
			log.debug(msg);
			this.facesMessages.add(msg);
			return new LinkedList<SelectItem>();
		} catch (RequestDeniedException e) {
			String msg = "request denied";
			log.debug(msg);
			this.facesMessages.add(msg);
			return new LinkedList<SelectItem>();
		} catch (ConnectException e) {
			String msg = "Connection error. Check your SSL setup.";
			log.debug(msg);
			this.facesMessages.add(msg);
			return new LinkedList<SelectItem>();
		}
		List<SelectItem> visas = new LinkedList<SelectItem>();
		for (Object value : values) {
			visas.add(new SelectItem(value));
		}
		return visas;
	}
}
