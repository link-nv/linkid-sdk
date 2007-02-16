/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Copyright 2005-2006 Frank Cornelis.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.security.auth.x500.X500Principal;

import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.entity.TrustPointPK;
import net.link.safeonline.model.PkiValidator;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.ocsp.BasicOCSPResp;
import org.bouncycastle.ocsp.CertificateID;
import org.bouncycastle.ocsp.CertificateStatus;
import org.bouncycastle.ocsp.OCSPException;
import org.bouncycastle.ocsp.OCSPReq;
import org.bouncycastle.ocsp.OCSPReqGenerator;
import org.bouncycastle.ocsp.OCSPResp;
import org.bouncycastle.ocsp.OCSPRespStatus;
import org.bouncycastle.ocsp.SingleResp;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

@Stateless
public class PkiValidatorBean implements PkiValidator {

	private static final Log LOG = LogFactory.getLog(PkiValidatorBean.class);

	@EJB
	private TrustPointDAO trustPointDAO;

	public boolean validateCertificate(TrustDomainEntity trustDomain,
			X509Certificate certificate) {
		/*
		 * We don't use the JDK certificate path builder API here, since it
		 * doesn't bring anything but unnecessary complexity. Keep It Simple,
		 * Stupid.
		 */

		if (null == certificate) {
			throw new IllegalArgumentException("certificate is null");
		}

		LOG.debug("validate certificate "
				+ certificate.getSubjectX500Principal() + " in domain "
				+ trustDomain.getName());

		List<TrustPointEntity> trustPointPath = buildTrustPointPath(
				trustDomain, certificate);

		boolean verificationResult = verifyPath(trustDomain, certificate,
				trustPointPath);
		if (false == verificationResult) {
			return false;
		}
		return true;
	}

	private boolean checkValidity(X509Certificate certificate) {
		try {
			certificate.checkValidity();
			return true;
		} catch (CertificateExpiredException e) {
			LOG.debug("certificate expired");
			return false;
		} catch (CertificateNotYetValidException e) {
			LOG.debug("certificate not yet valid");
			return false;
		}
	}

	/**
	 * Build the trust point path for a given certificate.
	 * 
	 * @param trustDomain
	 * @param certificate
	 * @return the path, or an empty list otherwise.
	 */
	private List<TrustPointEntity> buildTrustPointPath(
			TrustDomainEntity trustDomain, X509Certificate certificate) {

		List<TrustPointEntity> trustPoints = this.trustPointDAO
				.getTrustPoints(trustDomain);
		HashMap<TrustPointPK, TrustPointEntity> trustPointMap = new HashMap<TrustPointPK, TrustPointEntity>();
		for (TrustPointEntity trustPoint : trustPoints) {
			trustPointMap.put(trustPoint.getPk(), trustPoint);
		}

		List<TrustPointEntity> trustPointPath = new LinkedList<TrustPointEntity>();

		LOG.debug("build path for cert: "
				+ certificate.getSubjectX500Principal());

		X509Certificate currentRootCertificate = certificate;
		while (true) {
			byte[] authorityKeyIdentifierData = currentRootCertificate
					.getExtensionValue(X509Extensions.AuthorityKeyIdentifier
							.getId());
			if (null == authorityKeyIdentifierData) {
				/*
				 * PKIX RFC allows this for the root CA certificate.
				 */
				LOG
						.warn("certificate has no authority key indentifier extension");
				break;
			}
			AuthorityKeyIdentifierStructure authorityKeyIdentifierStructure;
			try {
				authorityKeyIdentifierStructure = new AuthorityKeyIdentifierStructure(
						authorityKeyIdentifierData);
			} catch (IOException e) {
				LOG.error("error parsing authority key identifier structure");
				break;
			}
			String keyId = new String(Hex
					.encodeHex(authorityKeyIdentifierStructure
							.getKeyIdentifier()));
			String issuer = currentRootCertificate.getIssuerX500Principal()
					.toString();
			LOG.debug("issuer: " + issuer);
			LOG.debug("keyId: " + keyId);
			TrustPointPK trustPointPK = new TrustPointPK(trustDomain, issuer,
					keyId);
			TrustPointEntity matchingTrustPoint = trustPointMap
					.get(trustPointPK);
			if (null == matchingTrustPoint) {
				LOG.debug("no matching trust point found");
				break;
			}
			LOG.debug("found path node: "
					+ matchingTrustPoint.getCertificate()
							.getSubjectX500Principal());
			trustPointPath.add(0, matchingTrustPoint);
			currentRootCertificate = matchingTrustPoint.getCertificate();
			if (isSelfIssued(currentRootCertificate)) {
				break;
			}
		}

		LOG.debug("path construction completed");
		return trustPointPath;
	}

	private boolean isSelfIssued(X509Certificate certificate) {
		X500Principal issuer = certificate.getIssuerX500Principal();
		X500Principal subject = certificate.getSubjectX500Principal();
		boolean result = subject.equals(issuer);
		return result;
	}

	boolean verifyPath(TrustDomainEntity trustDomain,
			X509Certificate certificate, List<TrustPointEntity> trustPointPath) {
		if (trustPointPath.isEmpty()) {
			LOG.debug("trust point path is empty");
			return false;
		}

		boolean performOcspCheck = trustDomain.isPerformOcspCheck();

		X509Certificate rootCertificate = trustPointPath.get(0)
				.getCertificate();
		X509Certificate issuerCertificate = rootCertificate;
		PublicKey issuerPublicKey = issuerCertificate.getPublicKey();

		for (TrustPointEntity trustPoint : trustPointPath) {
			X509Certificate trustPointCertificate = trustPoint.getCertificate();
			LOG.debug("verifying: "
					+ trustPointCertificate.getSubjectX500Principal());
			if (false == checkValidity(trustPointCertificate)) {
				return false;
			}
			if (false == verifySignature(trustPointCertificate, issuerPublicKey)) {
				return false;
			}
			if (false == verifyConstraints(trustPointCertificate)) {
				LOG.debug("verify constraints did not pass");
				return false;
			}
			issuerCertificate = trustPointCertificate;
			issuerPublicKey = issuerCertificate.getPublicKey();
		}

		if (false == checkValidity(certificate)) {
			return false;
		}
		if (false == verifySignature(certificate, issuerPublicKey)) {
			return false;
		}
		if (true == performOcspCheck) {
			LOG.debug("performing OCSP check");
			if (false == performOcspCheck(certificate, issuerCertificate)) {
				return false;
			}
		}

		return true;
	}

	private boolean verifyConstraints(X509Certificate certificate) {
		byte[] basicConstraintsValue = certificate
				.getExtensionValue(X509Extensions.BasicConstraints.getId());
		if (null == basicConstraintsValue) {
			LOG.debug("no basic contraints extension present");
			return false;
		}
		ASN1Encodable basicConstraintsDecoded;
		try {
			basicConstraintsDecoded = X509ExtensionUtil
					.fromExtensionValue(basicConstraintsValue);
		} catch (IOException e) {
			LOG.error("IO error: " + e.getMessage(), e);
			return false;
		}
		if (false == basicConstraintsDecoded instanceof ASN1Sequence) {
			LOG.debug("basic constraints extension is not an ASN1 sequence");
			return false;
		}
		ASN1Sequence basicConstraintsSequence = (ASN1Sequence) basicConstraintsDecoded;
		BasicConstraints basicConstraints = new BasicConstraints(
				basicConstraintsSequence);
		if (false == basicConstraints.isCA()) {
			LOG.debug("basic contraints says not a CA");
			return false;
		}
		return true;
	}

	public boolean performOcspCheck(X509Certificate certificate,
			X509Certificate issuerCertificate) {
		URI ocspUri = getOcspUri(certificate);
		if (null == ocspUri) {
			/*
			 * Nothing to do.
			 */
			return true;
		}
		boolean result = verifyOcspStatus(ocspUri, certificate,
				issuerCertificate);
		return result;
	}

	public boolean verifyOcspStatus(URI ocspUri, X509Certificate certificate,
			X509Certificate issuerCertificate) {
		byte[] resultOcspData = getOcsp(ocspUri, certificate, issuerCertificate);
		if (null == resultOcspData) {
			return false;
		}
		LOG.debug("result OCSP data: " + resultOcspData.length);
		OCSPResp resp;
		try {
			resp = new OCSPResp(resultOcspData);
		} catch (IOException e) {
			throw new RuntimeException("OCSP response IO error: "
					+ e.getMessage(), e);
		}
		int ocspStatus = resp.getStatus();
		LOG.debug("OCSP result status: " + ocspStatus);
		LOG.debug("OCSP malformed request: "
				+ (OCSPRespStatus.MALFORMED_REQUEST == ocspStatus));
		LOG.debug("OCSP unauthorized request: "
				+ (OCSPRespStatus.UNAUTHORIZED == ocspStatus));
		LOG.debug("OCSP internal error request: "
				+ (OCSPRespStatus.INTERNAL_ERROR == ocspStatus));
		LOG.debug("OCSP successful: "
				+ (OCSPRespStatus.SUCCESSFUL == ocspStatus));
		if (OCSPRespStatus.SUCCESSFUL != ocspStatus) {
			return false;
		}
		Object responseObject;
		try {
			responseObject = resp.getResponseObject();
		} catch (OCSPException e) {
			throw new RuntimeException("OCSP error: " + e.getMessage(), e);
		}
		LOG.debug("response object class: "
				+ responseObject.getClass().getName());
		BasicOCSPResp basicOCSPResp = (BasicOCSPResp) responseObject;
		LOG
				.debug("OCSP response produced at: "
						+ basicOCSPResp.getProducedAt());
		LOG.debug("OCSP version: " + basicOCSPResp.getVersion());
		LOG.debug("signature alg oid: " + basicOCSPResp.getSignatureAlgOID());
		basicOCSPResp.getResponderId();
		X509Certificate[] certs;
		try {
			certs = basicOCSPResp.getCerts(BouncyCastleProvider.PROVIDER_NAME);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException("no such provider error: "
					+ e.getMessage(), e);
		} catch (OCSPException e) {
			throw new RuntimeException("OCSP error: " + e.getMessage(), e);
		}
		if (null == certs) {
			LOG.debug("certs is null");
			return false;
		}
		for (X509Certificate cert : certs) {
			String subjectName = cert.getSubjectDN().getName();
			LOG.debug("cert subject: " + subjectName);
			LOG.debug("cert: " + cert);
			if (subjectName.matches(".*OCSP.*")) {
				boolean verifyResult;
				try {
					verifyResult = basicOCSPResp.verify(cert.getPublicKey(),
							BouncyCastleProvider.PROVIDER_NAME);
				} catch (NoSuchProviderException e) {
					throw new RuntimeException("no such provider error: "
							+ e.getMessage(), e);
				} catch (OCSPException e) {
					throw new RuntimeException("OCSP error: " + e.getMessage(),
							e);
				}
				LOG.debug("verify result: " + verifyResult);
				if (!verifyResult) {
					LOG.debug("verify result was false");
					return false;
				}
			}
		}
		SingleResp[] singleResps = basicOCSPResp.getResponses();
		LOG.debug("single responses: " + singleResps.length);
		for (SingleResp singleResp : singleResps) {
			LOG.debug("this update: " + singleResp.getThisUpdate());
			LOG.debug("next update: " + singleResp.getNextUpdate());
			LOG.debug("cert serial nr: "
					+ singleResp.getCertID().getSerialNumber());
			if (!certificate.getSerialNumber().equals(
					singleResp.getCertID().getSerialNumber())) {
				LOG.debug("certificate serial number does not correspond");
				return false;
			}
			LOG.debug("cert status: " + singleResp.getCertStatus());
			CertificateStatus status = (CertificateStatus) singleResp
					.getCertStatus();
			if (null == status) {
				return true;
			}
		}
		return false;
	}

	private byte[] getOcsp(URI ocspUri, X509Certificate certificate,
			X509Certificate issuerCertificate) {
		URL ocspUrl;
		try {
			ocspUrl = ocspUri.toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("OCSP URI error: " + e.getMessage(), e);
		}
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) ocspUrl.openConnection();
		} catch (IOException e) {
			throw new RuntimeException("OCSP URL connection error: "
					+ e.getMessage(), e);
		}
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		HttpURLConnection.setFollowRedirects(false);
		try {
			connection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			throw new RuntimeException("protocol error: " + e.getMessage(), e);
		}
		OCSPReqGenerator gen = new OCSPReqGenerator();
		CertificateID certId;
		try {
			certId = new CertificateID(CertificateID.HASH_SHA1,
					issuerCertificate, certificate.getSerialNumber());
		} catch (OCSPException e) {
			throw new RuntimeException("OCSP error: " + e.getMessage(), e);
		}
		gen.addRequest(certId);
		OCSPReq req;
		try {
			req = gen.generate();
		} catch (OCSPException e) {
			throw new RuntimeException("OCSP error: " + e.getMessage(), e);
		}
		byte[] ocspData;
		try {
			ocspData = req.getEncoded();
		} catch (IOException e) {
			throw new RuntimeException("I/O error: " + e.getMessage(), e);
		}
		connection.setRequestProperty("Content-Length", Integer
				.toString(ocspData.length));
		connection.setRequestProperty("Content-Type",
				"application/ocsp-request");
		OutputStream output;
		int responseCode;
		try {
			output = connection.getOutputStream();
			output.write(ocspData);
			output.flush();
			responseCode = connection.getResponseCode();
		} catch (ConnectException e) {
			LOG.debug("OCSP Responder is down");
			return null;
		} catch (UnknownHostException e) {
			LOG.debug("unknown OCSP responder");
			return null;
		} catch (IOException e) {
			LOG.debug("IO exception type: " + e.getClass().getName());
			throw new RuntimeException("I/O error: " + e.getMessage(), e);
		}

		LOG.debug("OCSP response code: " + responseCode);
		LOG
				.debug("response OK: "
						+ (HttpURLConnection.HTTP_OK == responseCode));
		if (HttpURLConnection.HTTP_OK != responseCode) {
			LOG.error("HTTP response code: " + responseCode);
			return null;
		}
		String resultContentType = connection.getContentType();
		LOG.debug("result content type: " + resultContentType);
		if (!"application/ocsp-response".equals(resultContentType)) {
			LOG.warn("result content type not application/ocsp-response");
		}
		int resultContentLength = connection.getContentLength();
		LOG.debug("result content length: " + resultContentLength);
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException("I/O error: " + e.getMessage(), e);
		}
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream(
				resultContentLength);
		try {
			IOUtils.copy(inputStream, arrayOutputStream);
		} catch (IOException e) {
			throw new RuntimeException("I/O error: " + e.getMessage(), e);
		}
		connection.disconnect();
		byte[] resultOcspData = arrayOutputStream.toByteArray();
		return resultOcspData;
	}

	private boolean verifySignature(X509Certificate certificate,
			PublicKey issuerPublicKey) {
		try {
			certificate.verify(issuerPublicKey);
		} catch (InvalidKeyException e) {
			LOG.debug("invalid key");
			/*
			 * This can occur if a root certificate was not self-signed.
			 */
			return false;
		} catch (CertificateException e) {
			LOG.debug("cert error: " + e.getMessage());
			return false;
		} catch (NoSuchAlgorithmException e) {
			LOG.debug("algo error");
			return false;
		} catch (NoSuchProviderException e) {
			LOG.debug("provider error");
			return false;
		} catch (SignatureException e) {
			LOG.debug("sign error");
			return false;
		}
		return true;
	}

	public URI getOcspUri(X509Certificate certificate) {
		URI ocspURI = getAccessLocation(certificate,
				X509ObjectIdentifiers.ocspAccessMethod);
		return ocspURI;
	}

	private URI getAccessLocation(X509Certificate certificate,
			DERObjectIdentifier accessMethod) {
		byte[] authInfoAccessExtensionValue = certificate
				.getExtensionValue(X509Extensions.AuthorityInfoAccess.getId());
		if (null == authInfoAccessExtensionValue) {
			return null;
		}
		DEROctetString oct;
		try {
			oct = (DEROctetString) (new ASN1InputStream(
					new ByteArrayInputStream(authInfoAccessExtensionValue))
					.readObject());
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage(), e);
		}
		AuthorityInformationAccess authorityInformationAccess;
		try {
			authorityInformationAccess = new AuthorityInformationAccess(
					(ASN1Sequence) new ASN1InputStream(oct.getOctets())
							.readObject());
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage(), e);
		}
		AccessDescription[] accessDescriptions = authorityInformationAccess
				.getAccessDescriptions();
		for (AccessDescription accessDescription : accessDescriptions) {
			LOG.debug("access method: " + accessDescription.getAccessMethod());
			boolean correctAccessMethod = accessDescription.getAccessMethod()
					.equals(accessMethod);
			if (!correctAccessMethod) {
				continue;
			}
			GeneralName gn = accessDescription.getAccessLocation();
			if (gn.getTagNo() != GeneralName.uniformResourceIdentifier) {
				LOG.debug("not a uniform resource identifier");
				continue;
			}
			DERIA5String str = DERIA5String.getInstance(gn.getDERObject());
			String accessLocation = str.getString();
			LOG.debug("access location: " + accessLocation);
			URI uri = toURI(accessLocation);
			LOG.debug("access location URI: " + uri);
			return uri;
		}
		return null;
	}

	private URI toURI(String str) {
		try {
			URI uri = new URI(str);
			return uri;
		} catch (URISyntaxException e) {
			throw new RuntimeException("URI syntax error: " + e.getMessage(), e);
		}
	}
}
