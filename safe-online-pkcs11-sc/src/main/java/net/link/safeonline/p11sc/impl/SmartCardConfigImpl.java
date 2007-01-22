/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc.impl;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.smartcardio.ATR;

import net.link.safeonline.p11sc.SmartCardConfig;

public class SmartCardConfigImpl implements SmartCardConfig {

	private String cardAlias;

	private Set<ATR> supportedATRs;

	private String authenticationKeyAlias;

	private String signatureKeyAlias;

	private List<Pkcs11Platform> pkcs11DriverLocations;

	private String identityExtractorClassname;

	private static class Pkcs11Platform {
		private String platformMatch;

		private List<File> driverLocations;

		public Pkcs11Platform(String platformMatch) {
			this.platformMatch = platformMatch;
			this.driverLocations = new LinkedList<File>();
		}

		public String getPlatformMatch() {
			return this.platformMatch;
		}

		public List<File> getDriverLocations() {
			return this.driverLocations;
		}

		public void addDriverLocation(File driverLocation) {
			this.driverLocations.add(driverLocation);
		}

		public void addDriverLocation(String driverLocation) {
			File driverLocationFile = new File(driverLocation);
			addDriverLocation(driverLocationFile);
		}
	}

	public SmartCardConfigImpl(String cardAlias) {
		this.cardAlias = cardAlias;
		this.supportedATRs = new HashSet<ATR>();
		this.pkcs11DriverLocations = new LinkedList<Pkcs11Platform>();
	}

	public void addSupportedATR(ATR supportedATR) {
		this.supportedATRs.add(supportedATR);
	}

	public void addSupportedATR(byte[] atrBytes) {
		ATR atr = new ATR(atrBytes);
		addSupportedATR(atr);
	}

	public String getCardAlias() {
		return this.cardAlias;
	}

	public boolean isSupportedATR(ATR atr) {
		boolean result = this.supportedATRs.contains(atr);
		return result;
	}

	public String getAuthenticationKeyAlias() {
		return this.authenticationKeyAlias;
	}

	public String getSignatureKeyAlias() {
		return this.signatureKeyAlias;
	}

	public void setAuthenticationKeyAlias(String authenticationKeyAlias) {
		this.authenticationKeyAlias = authenticationKeyAlias;
	}

	public void setSignatureKeyAlias(String signatureKeyAlias) {
		this.signatureKeyAlias = signatureKeyAlias;
	}

	public List<File> getPkcs11DriverLocations(String platform) {
		List<File> driverLocations = new LinkedList<File>();
		for (Pkcs11Platform pkcs11Platform : this.pkcs11DriverLocations) {
			if (false == platform.matches(pkcs11Platform.getPlatformMatch())) {
				continue;
			}
			driverLocations.addAll(pkcs11Platform.getDriverLocations());
		}
		return driverLocations;
	}

	public void addPkcs11DriverLocations(String platformMatch,
			List<String> driverLocations) {
		Pkcs11Platform platform = new Pkcs11Platform(platformMatch);
		for (String driverLocation : driverLocations) {
			// SOS-2: auto-reformatting XML config file can yield whitespaces
			driverLocation = driverLocation.trim();
			platform.addDriverLocation(driverLocation);
		}
		this.pkcs11DriverLocations.add(platform);
	}

	public String getIdentityExtractorClassname() {
		return this.identityExtractorClassname;
	}

	public void setIdentityExtractorClassname(String identityExtractorClassname) {
		this.identityExtractorClassname = identityExtractorClassname;
	}
}
