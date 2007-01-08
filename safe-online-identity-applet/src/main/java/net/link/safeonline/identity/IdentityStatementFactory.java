/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.lin_k.safe_online.identity_statement._1.IdentityDataType;
import net.lin_k.safe_online.identity_statement._1.IdentityStatementType;
import net.lin_k.safe_online.identity_statement._1.ObjectFactory;
import net.link.safeonline.p11sc.SmartCard;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class IdentityStatementFactory {

	public String createIdentityStatement(SmartCard smartCard) {
		String id = "idid-" + UUID.randomUUID().toString();
		Document document = createIdentityStatementDocument(id, smartCard);

		signIdentityStatementDocument(document, id, smartCard);

		String result = outputDom(document);
		return result;
	}

	private void signIdentityStatementDocument(Document document, String id,
			SmartCard smartCard) {
		X509Certificate authenticationCertificate = smartCard
				.getAuthenticationCertificate();
		PrivateKey authenticationPrivateKey = smartCard
				.getAuthenticationPrivateKey();
		if (null == authenticationCertificate) {
			throw new IllegalArgumentException(
					"authentication certiticate is null");
		}
		if (null == authenticationPrivateKey) {
			throw new IllegalArgumentException(
					"authentication private key is null");
		}
		XMLSignatureFactory signatureFactory = XMLSignatureFactory
				.getInstance("DOM");

		XMLSignContext signContext = new DOMSignContext(
				authenticationPrivateKey, document.getDocumentElement());
		signContext.putNamespacePrefix(
				javax.xml.crypto.dsig.XMLSignature.XMLNS, "ds");

		DigestMethod digestMethod;
		SignatureMethod signatureMethod;
		CanonicalizationMethod canonicalizationMethod;
		try {
			digestMethod = signatureFactory.newDigestMethod(
					DigestMethod.SHA512, null);
			signatureMethod = signatureFactory.newSignatureMethod(
					SignatureMethod.RSA_SHA1, null);
			canonicalizationMethod = signatureFactory
					.newCanonicalizationMethod(
							CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
							(C14NMethodParameterSpec) null);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("no such algo: " + e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException("invalid algo param: " + e.getMessage(),
					e);
		}
		Reference reference = signatureFactory.newReference("#" + id,
				digestMethod);
		SignedInfo signedInfo = signatureFactory.newSignedInfo(
				canonicalizationMethod, signatureMethod, Collections
						.singletonList(reference));

		KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
		List<X509Certificate> content = new ArrayList<X509Certificate>();
		content.add(authenticationCertificate);
		X509Data x509Data = keyInfoFactory.newX509Data(content);
		KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections
				.singletonList(x509Data));

		javax.xml.crypto.dsig.XMLSignature signature = signatureFactory
				.newXMLSignature(signedInfo, keyInfo);
		try {
			signature.sign(signContext);
		} catch (MarshalException e) {
			throw new RuntimeException("marshal error: " + e.getMessage());
		} catch (XMLSignatureException e) {
			throw new RuntimeException("xml sign error: " + e.getMessage());
		}
	}

	private Document createIdentityStatementDocument(String id,
			SmartCard smartCard) {
		ObjectFactory objectFactory = new ObjectFactory();
		IdentityStatementType identityStatement = objectFactory
				.createIdentityStatementType();

		IdentityDataType identityData = objectFactory.createIdentityDataType();

		identityData.setId(id);
		identityStatement.setIdentityData(identityData);

		String givenName = smartCard.getGivenName();
		identityData.setGivenName(givenName);
		String surname = smartCard.getSurname();
		identityData.setSurname(surname);
		String street = smartCard.getStreet();
		identityData.setStreet(street);
		String postalCode = smartCard.getPostalCode();
		identityData.setPostalCode(postalCode);
		String city = smartCard.getCity();
		identityData.setCity(city);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("parser config error: " + e.getMessage());
		}
		Document document = documentBuilder.newDocument();

		try {
			JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			JAXBElement<IdentityStatementType> identityStatementElement = objectFactory
					.createIdentityStatement(identityStatement);
			marshaller.marshal(identityStatementElement, document);
		} catch (JAXBException e) {
			throw new RuntimeException("JAXB error: " + e.getMessage());
		}
		return document;
	}

	public String outputDom(Node dom) {
		Source source = new DOMSource(dom);
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		Transformer transformer;
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformer = transformerFactory.newTransformer();
			/*
			 * We have to omit the ?xml declaration if we want to embed the
			 * document.
			 */
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("transformer config error: "
					+ e.getMessage(), e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new RuntimeException("transformer factory config error: "
					+ e.getMessage(), e);
		}
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new RuntimeException("transformer error: " + e.getMessage(),
					e);
		}
		return stringWriter.getBuffer().toString();
	}
}
