/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using NUnit.Framework;
using System.IdentityModel.Tokens;
using System.Collections.Specialized;
using System.Collections;
using System.Xml.Serialization;
using System.IO;
using System.Xml;
using System.Text;
using System.Security.Cryptography.Xml;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.X509;
using System.Security.Cryptography;
using System.Collections;
using System.Collections.Generic;
using IdMappingWSNamespace;

using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestSaml
	{
		[Test]
		public void TestMethod()
		{
			string[] attributeValues = {"test-value"};
			SamlAttribute samlAttribute = new SamlAttribute("urn:namespace", "attributeName", attributeValues);
			Console.WriteLine("saml attribute: " + samlAttribute);
			
			SamlAuthenticationStatement samlStatement = new SamlAuthenticationStatement();
			
			SamlAssertion samlAssertion = new SamlAssertion();
			samlAssertion.Statements.Add(samlStatement);
		}
		
		[Test]
		public void TestAuthnRequest() {
			AuthnRequestType authnRequest = new AuthnRequestType();
			authnRequest.ID = "test-id";
			authnRequest.AssertionConsumerServiceURL = "http://test.assertion.consumer";
			authnRequest.Destination = "http://destination";
			authnRequest.ForceAuthn = true;
			authnRequest.ProtocolBinding = "urn:test:protocol:binding";
			authnRequest.Version = "2.0";
			authnRequest.IssueInstant = DateTime.Now.ToUniversalTime();
			
			NameIDType issuer = new NameIDType();
			issuer.Value = "test-issuer";
			authnRequest.Issuer = issuer;
			
			NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
			nameIdPolicy.AllowCreate = true;
			nameIdPolicy.AllowCreateSpecified = true;
			authnRequest.NameIDPolicy = nameIdPolicy;
			
			XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
      		ns.Add("samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
      		ns.Add("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
      		//ns.Add("ds", "http://www.w3.org/2000/09/xmldsig#");
			
			XmlRootAttribute xRoot = new XmlRootAttribute();
    		xRoot.ElementName = "AuthnRequest";
    		xRoot.Namespace = "urn:oasis:names:tc:SAML:2.0:protocol";
			XmlSerializer serializer = new XmlSerializer(typeof(AuthnRequestType), xRoot);
			MemoryStream memoryStream = new MemoryStream();
			XmlTextWriter xmlTextWriter = new XmlTextWriter(memoryStream, Encoding.UTF8);
      		serializer.Serialize(xmlTextWriter, authnRequest, ns);
     		memoryStream = (MemoryStream)xmlTextWriter.BaseStream;
     		string result = new UTF8Encoding().GetString(memoryStream.ToArray());
      		Console.WriteLine("result: " + result);
      		
      		XmlDocument document = new XmlDocument();
      		memoryStream.Seek(0, SeekOrigin.Begin);
      		document.Load(memoryStream);
      		String xmlString = document.OuterXml;
      		Console.WriteLine("DOM result: " + xmlString);
      		
      		RSACryptoServiceProvider Key = new RSACryptoServiceProvider();
      		
      		SignedXml signedXml = new SignedXml(document);
      		signedXml.SigningKey = Key;
      		Signature signature = signedXml.Signature;
      		signature.SignedInfo.CanonicalizationMethod = SignedXml.XmlDsigExcC14NTransformUrl;
      		Reference reference = new Reference("#" + authnRequest.ID);
      		XmlDsigEnvelopedSignatureTransform env = new XmlDsigEnvelopedSignatureTransform();
        	reference.AddTransform(env);
        	XmlDsigExcC14NTransform excC14NTransform = new XmlDsigExcC14NTransform("ds saml samlp");
        	reference.AddTransform(excC14NTransform);
        	signature.SignedInfo.AddReference(reference);
        	
        	signedXml.ComputeSignature();

        	XmlElement xmlDigitalSignature = signedXml.GetXml();
        	document.DocumentElement.AppendChild(document.ImportNode(xmlDigitalSignature, true));

        	result = document.OuterXml;
        	Console.WriteLine("result: " + result);
        	
        	XmlTextWriter xmltw = new XmlTextWriter(TestConstants.workDir + "\\test.xml", new UTF8Encoding(false));
        	document.WriteTo(xmltw);
        	xmltw.Close();
		}
		
		[Test]
		public void TestSaml2AuthUtil() {
			RSACryptoServiceProvider key = new RSACryptoServiceProvider();
			
			Saml2AuthUtil testedInstance = new Saml2AuthUtil(key);
			
			string spUrl = "http://service.provider.com";
			string idpUrl = "http://identity.provider.com";
			string applicationId = "urn:application:id";
			string[] devices = new string[] {"test-device-1", "test-device-2"};

            // device context
            Dictionary<string, string> deviceContextMap = new Dictionary<string, string>();
            deviceContextMap.Add(RequestConstants.DEVICE_CONTEXT_TITLE, "Test device context");

            // attribute suggestions
            Dictionary<string, List<Object>> attributeSuggestions = new Dictionary<string, List<object>>();
            attributeSuggestions.Add("test.attribute.string", new List<Object>{"test"});
            attributeSuggestions.Add("test.attribute.date", new List<Object> { new DateTime() });
            attributeSuggestions.Add("test.attribute.boolean", new List<Object> { true });
            attributeSuggestions.Add("test.attribute.integer", new List<Object> { 69 });
            attributeSuggestions.Add("test.attribute.double", new List<Object> { 3.14159 });

            string result = testedInstance.generateAuthnRequest(applicationId, null, null, spUrl, idpUrl, null, false, 
                deviceContextMap, attributeSuggestions);			
			Console.WriteLine("result document: " + result);
			
			XmlDocument xmlDocument = new XmlDocument();
			xmlDocument.LoadXml(result);
			
			SignedXml signedXml = new SignedXml(xmlDocument);
			XmlNodeList nodeList = xmlDocument.GetElementsByTagName("Signature");
			signedXml.LoadXml((XmlElement)nodeList[0]);
			
			bool verificationResult = signedXml.CheckSignature(key);
			Console.WriteLine("verification result: " + verificationResult);
			Assert.IsTrue(verificationResult);
		}

		[Test]
		public void TestSaml2LogoutUtil() {
			RSACryptoServiceProvider key = new RSACryptoServiceProvider();
			
			Saml2LogoutUtil testedInstance = new Saml2LogoutUtil(key);
			
			string idpUrl = "http://identity.provider.com";
			string applicationId = "urn:application:id";
			string subjectName = "test-subject";
			
			string result = testedInstance.generateLogoutRequest(subjectName, applicationId, idpUrl);
			Console.WriteLine("result document: " + result);
			
			XmlDocument xmlDocument = new XmlDocument();
			xmlDocument.LoadXml(result);
			
			SignedXml signedXml = new SignedXml(xmlDocument);
			XmlNodeList nodeList = xmlDocument.GetElementsByTagName("Signature");
			signedXml.LoadXml((XmlElement)nodeList[0]);
			
			bool verificationResult = signedXml.CheckSignature(key);
			Console.WriteLine("verification result: " + verificationResult);
			Assert.IsTrue(verificationResult);
		}
	}
}
