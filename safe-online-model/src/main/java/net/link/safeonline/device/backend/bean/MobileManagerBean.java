package net.link.safeonline.device.backend.bean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.device.backend.MobileManager;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClient;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClientImpl;
import net.link.safeonline.sdk.ws.encap.administration.EncapAdministrationClient;
import net.link.safeonline.sdk.ws.encap.administration.EncapAdministrationClientImpl;
import net.link.safeonline.sdk.ws.encap.authentication.EncapAuthenticationClient;
import net.link.safeonline.sdk.ws.encap.authentication.EncapAuthenticationClientImpl;

@Stateless
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class MobileManagerBean implements MobileManager {

	@Configurable(name = "Encap Server", group = "Encap")
	private String encapServerLocation = "localhost:8080/safe-online-encap-ws";

	@Configurable(name = "Encap Organisation ID", group = "Encap")
	private String encapOrganisationId = "encap";

	/*
	 * RemoteException are transformed to a MobileException, else they get
	 * wrapped by JBoss into EJBTransactionRolledbackException
	 */
	public String requestOTP(String mobile) throws MalformedURLException,
			MobileException {
		try {
			EncapAuthenticationClient encapAuthenticationClient = new EncapAuthenticationClientImpl(
					this.encapServerLocation);
			return encapAuthenticationClient.challenge(mobile,
					this.encapOrganisationId);
		} catch (RemoteException e) {
			throw new MobileException(e.getMessage());
		}
	}

	public boolean verifyOTP(String challengeId, String OTPValue)
			throws MalformedURLException, MobileException {
		try {
			EncapAuthenticationClient encapAuthenticationClient = new EncapAuthenticationClientImpl(
					this.encapServerLocation);
			return encapAuthenticationClient.verifyOTP(challengeId, OTPValue);
		} catch (RemoteException e) {
			throw new MobileException(e.getMessage());
		}
	}

	public String activate(String mobile, SubjectEntity subject)
			throws MobileException, MalformedURLException {
		try {
			EncapActivationClient encapActivationClient = new EncapActivationClientImpl(
					this.encapServerLocation);
			return encapActivationClient.activate(mobile,
					this.encapOrganisationId, subject.getUserId());
		} catch (RemoteException e) {
			throw new MobileException(e.getMessage());
		}
	}

	public void remove(String mobile) throws MobileException,
			MalformedURLException {
		try {
			EncapAdministrationClient encapAdministrationClient = new EncapAdministrationClientImpl(
					this.encapServerLocation);
			encapAdministrationClient.remove(mobile, this.encapOrganisationId);
		} catch (RemoteException e) {
			throw new MobileException(e.getMessage());
		}

	}
}