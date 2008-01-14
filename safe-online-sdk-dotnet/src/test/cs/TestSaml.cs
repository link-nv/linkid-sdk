using System;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;

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
        	
        	XmlTextWriter xmltw = new XmlTextWriter("C:\\work\\test.xml", new UTF8Encoding(false));
        	document.WriteTo(xmltw);
        	xmltw.Close();
		}
	}
}
