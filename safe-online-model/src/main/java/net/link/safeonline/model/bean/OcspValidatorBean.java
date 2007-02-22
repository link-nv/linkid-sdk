/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
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
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;

import javax.ejb.Stateless;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
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
import org.bouncycastle.ocsp.RevokedStatus;
import org.bouncycastle.ocsp.UnknownStatus;
import org.bouncycastle.ocsp.SingleResp;

import net.link.safeonline.model.OcspValidator;

@Stateless
public class OcspValidatorBean implements OcspValidator {

	private static final Log LOG = LogFactory.getLog(PkiValidatorBean.class);

	public boolean performOcspCheck(X509Certificate certificate,
			X509Certificate issuerCertificate) {
		URI ocspUri = getOcspUri(certificate);
		if (null == ocspUri) {
			/*
			 * Nothing to do.
			 */
			return true;
		}
		OcspResult result = verifyOcspStatus(ocspUri, certificate,
				issuerCertificate);
		if (result == OcspResult.GOOD) {
			return true;
		} else {
			return false;
		}
	}

	public OcspResult verifyOcspStatus(URI ocspUri,
			X509Certificate certificate, X509Certificate issuerCertificate) {
		byte[] resultOcspData = getOcsp(ocspUri, certificate, issuerCertificate);
		if (null == resultOcspData) {
			return OcspResult.FAILED;
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
			return OcspResult.FAILED;
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
			return OcspResult.FAILED;
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
					return OcspResult.FAILED;
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
				return OcspResult.FAILED;
			}
			LOG.debug("cert status: " + singleResp.getCertStatus());
			CertificateStatus status = (CertificateStatus) singleResp
					.getCertStatus();
			if (null == status) {
				return OcspResult.GOOD;
			} else if (status instanceof RevokedStatus) {
				return OcspResult.REVOKED;
			} else if (status instanceof UnknownStatus) {
				return OcspResult.SUSPENDED;
			}
		}
		return OcspResult.FAILED;
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
