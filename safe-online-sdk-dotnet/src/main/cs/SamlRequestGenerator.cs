using System;

using System.Security.Cryptography;
using System.Security.Cryptography.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Xml;
using System.Text;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of SamlRequestGenerator.
	/// </summary>
	public class SamlRequestGenerator
	{
		private readonly RSACryptoServiceProvider key;
		
		public SamlRequestGenerator(RSACryptoServiceProvider key)
		{
			this.key = key;
		}
		
		public string generateSamlRequest(string tokenId, string spUrl,
		                                  string idpUrl, string applicationId) {
			AuthnRequestType authnRequest = new AuthnRequestType();
			authnRequest.ID = tokenId;
			authnRequest.AssertionConsumerServiceURL = spUrl;
			authnRequest.Destination = idpUrl;
			authnRequest.ForceAuthn = true;
			authnRequest.ProtocolBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
			authnRequest.Version = "2.0";
			authnRequest.IssueInstant = DateTime.Now.ToUniversalTime();
			
			NameIDType issuer = new NameIDType();
			issuer.Value = applicationId;
			authnRequest.Issuer = issuer;
			
			NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
			nameIdPolicy.AllowCreate = true;
			nameIdPolicy.AllowCreateSpecified = true;
			authnRequest.NameIDPolicy = nameIdPolicy;
			
			XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
      		ns.Add("samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
      		ns.Add("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
			
			XmlRootAttribute xRoot = new XmlRootAttribute();
    		xRoot.ElementName = "AuthnRequest";
    		xRoot.Namespace = "urn:oasis:names:tc:SAML:2.0:protocol";
			XmlSerializer serializer = new XmlSerializer(typeof(AuthnRequestType), xRoot);
			MemoryStream memoryStream = new MemoryStream();
			XmlTextWriter xmlTextWriter = new XmlTextWriter(memoryStream, Encoding.UTF8);
      		serializer.Serialize(xmlTextWriter, authnRequest, ns);
      		
      		XmlDocument document = new XmlDocument();
      		memoryStream.Seek(0, SeekOrigin.Begin);
      		document.Load(memoryStream);
      		
      		SignedXml signedXml = new SignedXml(document);
      		signedXml.SigningKey = this.key;
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

        	string result = document.OuterXml;
        	return result;
		}
	}
}
