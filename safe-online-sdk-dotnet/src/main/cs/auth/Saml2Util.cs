/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Security.Cryptography;
using System.Security.Cryptography.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Xml;
using System.Text;
using System.Collections.Generic;

using IdMappingWSNamespace;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Saml2Util
	/// 
	/// This utility class generates a SAML v2.0 AuthenticationRequest with HTTP Browser Post binding
	/// and validates the returned SAML v2.0 Response.
	/// </summary>
	public class Saml2Util
	{
		private readonly RSACryptoServiceProvider key;
		
		private string expectedChallenge;
		
		private string expectedAudience;
		
		public Saml2Util(RSACryptoServiceProvider key)
		{
			this.key = key;
		}
		
		public string getChallenge() {
			return this.expectedChallenge;
		}
		
		public String getAudience() {
			return this.expectedAudience;
		}
		
		/// <summary>
		/// Generates a SAML v2.0 Authentication Request with HTTP Browser Post Binding. The return string containing the request
		/// is already Base64 encoded.
		/// </summary>
		/// <param name="issuerName"></param>
		/// <param name="applicationName"></param>
		/// <param name="applicationFriendlyName"></param>
		/// <param name="serviceProviderUrl"></param>
		/// <param name="identityProviderUrl"></param>
		/// <param name="devices"></param>
		/// <param name="ssoEnabled"></param>
		/// <returns></returns>
		public string generateEncodedSamlRequest(string issuerName, string applicationName, string applicationFriendlyName,
		                                  string serviceProviderUrl, string identityProviderUrl, List<string> devices,
		                                  bool ssoEnabled) {
			string samlRequest = generateSamlRequest(issuerName, applicationName, applicationFriendlyName, serviceProviderUrl,
			                                        identityProviderUrl, devices, ssoEnabled);
			return Convert.ToBase64String(Encoding.ASCII.GetBytes(samlRequest));		
		}
		
		/// <summary>
		/// Generates a SAML v2.0 Authentication Request with HTTP Browser Post Binding. The return string containing the request
		/// is NOT Base64 encoded.
		/// </summary>
		/// <param name="issuerName"></param>
		/// <param name="applicationName"></param>
		/// <param name="applicationFriendlyName"></param>
		/// <param name="serviceProviderUrl"></param>
		/// <param name="identityProviderUrl"></param>
		/// <param name="devices"></param>
		/// <param name="ssoEnabled"></param>
		/// <returns></returns>
		public string generateSamlRequest(string issuerName, string applicationName, string applicationFriendlyName,
		                                  string serviceProviderUrl, string identityProviderUrl, List<string> devices,
		                                  bool ssoEnabled) {
			this.expectedChallenge = Guid.NewGuid().ToString();
			this.expectedAudience = applicationName;
			
			AuthnRequestType authnRequest = new AuthnRequestType();
			authnRequest.ForceAuthn = !ssoEnabled;
			authnRequest.ID = this.expectedChallenge;
			authnRequest.Version = "2.0";
			authnRequest.IssueInstant = DateTime.Now.ToUniversalTime();

			NameIDType issuer = new NameIDType();
			issuer.Value = issuerName;
			authnRequest.Issuer = issuer;

			authnRequest.AssertionConsumerServiceURL = serviceProviderUrl;
			authnRequest.ProtocolBinding = Saml2Constants.SAML2_BINDING_HTTP_POST;

			authnRequest.Destination = identityProviderUrl;
			
			if ( null != applicationFriendlyName ) {
				authnRequest.ProviderName = applicationFriendlyName;
			} else {
				authnRequest.ProviderName = applicationName;
			}
			
			NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
			nameIdPolicy.AllowCreate = true;
			nameIdPolicy.AllowCreateSpecified = true;
			authnRequest.NameIDPolicy = nameIdPolicy;
			
			if ( null != devices) {
				RequestedAuthnContextType requestedAuthnContext = new RequestedAuthnContextType();
				requestedAuthnContext.Items = devices.ToArray();
				authnRequest.RequestedAuthnContext = requestedAuthnContext;
			}
			
			ConditionsType conditions = new ConditionsType();
			AudienceRestrictionType audienceRestriction = new AudienceRestrictionType();
			audienceRestriction.Audience = new string[] {applicationName};
			conditions.Items = new ConditionAbstractType[] {audienceRestriction};
			authnRequest.Conditions = conditions;
			
			XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
      		ns.Add("samlp", Saml2Constants.SAML2_PROTOCOL_NAMESPACE);
      		ns.Add("saml", Saml2Constants.SAML2_ASSERTION_NAMESPACE);
			
			XmlRootAttribute xRoot = new XmlRootAttribute();
    		xRoot.ElementName = "AuthnRequest";
    		xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;
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
      		xmlTextWriter.Close();
        	return result;
		}
		
		/// <summary>
		/// Validates a Base64 encoded SAML v2.0 Response.
		/// </summary>
		/// <param name="encodedSamlResponse"></param>
		/// <param name="wsLocation"></param>
		/// <param name="appPfxPath"></param>
		/// <param name="appPfxPassword"></param>
		/// <param name="olasCertPath"></param>
		/// <returns>AuthenticationProtocolContext containing OLAS userId and authenticated device</returns>
		public AuthenticationProtocolContext validateEncodedSamlResponse(string encodedSamlResponse, string wsLocation,
		                                                          		 string appPfxPath, string appPfxPassword, 
		                                                          		 string olasCertPath) {
			;
			byte[] samlResponseData = Convert.FromBase64String(encodedSamlResponse);
			string samlResponse = Encoding.UTF8.GetString(samlResponseData);
			return validateSamlResponse(samlResponse, wsLocation, appPfxPath, appPfxPassword, olasCertPath);
		}
		
		/// <summary>
		/// Validates a base64 decoded SAML v2.0 Response.
		/// </summary>
		/// <param name="encodedSamlResponse"></param>
		/// <param name="wsLocation"></param>
		/// <param name="appPfxPath"></param>
		/// <param name="appPfxPassword"></param>
		/// <param name="olasCertPath"></param>
		/// <returns>AuthenticationProtocolContext containing OLAS userId and authenticated device</returns>
		public AuthenticationProtocolContext validateSamlResponse(string samlResponse, string wsLocation,
		                                                          string appPfxPath, string appPfxPassword, 
		                                                          string olasCertPath) {
			DateTime now = DateTime.UtcNow;
			
			STSClient stsClient = new STSClientImpl(wsLocation, appPfxPath, appPfxPassword, olasCertPath);
			bool result = stsClient.validateAuthnResponse(samlResponse, TrustDomainType.NODE);
			if ( false == result ) {
				return null;
			}
			
			XmlRootAttribute xRoot = new XmlRootAttribute();
    		xRoot.ElementName = "Response";
    		xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;

			TextReader reader = new StringReader(samlResponse);
			XmlSerializer serializer = new XmlSerializer(typeof(ResponseType),xRoot);
			ResponseType response = (ResponseType)serializer.Deserialize(reader);
			reader.Close();
			
			if ( ! response.InResponseTo.Equals(this.expectedChallenge) ) {
				throw new AuthenticationException("SAML response is not a response belonging to the original request.");
			}

			if ( response.Status.StatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_AUTHN_FAILED) || 
			    response.Status.StatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_UNKNOWN_PRINCIPAL)) {
				return null;
			}
			
			String subjectName;
			String authenticatedDevice;
			foreach(object item in response.Items) {
				AssertionType assertion = (AssertionType) item;
				DateTime notBefore = assertion.Conditions.NotBefore;
				DateTime notOnOrAfter = assertion.Conditions.NotOnOrAfter;
				
				Console.WriteLine("now: {0}", now.ToString());
				Console.WriteLine("notBefore: {0}", notBefore.ToString());
				Console.WriteLine("notOnOrAfter: {0}", notOnOrAfter.ToString());
				
				if (now.CompareTo(notBefore) < 0 || now.CompareTo(notOnOrAfter) > 0) {
					throw new AuthenticationException("Invalid SAML message timeframe");
				}
				
				SubjectType subject = assertion.Subject;
				if ( null == subject) {
					throw new AuthenticationException("Missing Assertion Subject");
				}
				NameIDType nameId = (NameIDType) subject.Items[0];
				subjectName = nameId.Value;
				
				AudienceRestrictionType audienceRestriction = (AudienceRestrictionType)assertion.Conditions.Items[0];
				if ( null == audienceRestriction.Audience ) {
					throw new AuthenticationException("No Audiences found in AudienceRestriction");
				}
				
				if ( ! audienceRestriction.Audience[0].Equals(this.expectedAudience)) {
					throw new AuthenticationException("Audience name not correct, expected: " + this.expectedAudience);
				}
				
				AuthnStatementType authnStatement = (AuthnStatementType)assertion.Items[0];
				authenticatedDevice = (string) authnStatement.AuthnContext.Items[0];

				return new AuthenticationProtocolContext(subjectName, authenticatedDevice);
			}
			
			return null;
		}
	}
}
