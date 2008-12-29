/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.ServiceModel;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel.Description;
using System.ServiceModel.Security;
using System.Net;
using System.Net.Security;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Channels;
using System.ServiceModel.Security.Tokens;
using System.Xml;
using StsWSNamespace;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the OLAS STS Web Service.
	/// 
	/// This is only a proof-of-concept implementation.
	/// </summary>
	public class STSClientImpl : STSClient
	{
		public readonly static string STATUS_VALID = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/valid";
	
		public readonly static string STATUS_INVALID = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/invalid";
		
		private SecurityTokenServicePortClient client;
		
		public STSClientImpl(string location, string appPfxPath, string appPfxPassword, string olasCertPath)
		{
			X509Certificate2 appCertificate = new X509Certificate2(appPfxPath, appPfxPassword);
			X509Certificate2 olasCertificate = new X509Certificate2(olasCertPath);

			ServicePointManager.ServerCertificateValidationCallback = 
				new RemoteCertificateValidationCallback(WCFUtil.AnyCertificateValidationCallback);
			string address = "https://" + location + "/safe-online-ws/sts";
			EndpointAddress remoteAddress = new EndpointAddress(address);

			this.client = new SecurityTokenServicePortClient(new OlasBinding(olasCertificate), remoteAddress);
			
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = olasCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
		}
		
		public bool validateAuthnResponse(string authnResponse, TrustDomainType trustDomain) {
			RequestSecurityTokenType requestSecurityToken = new RequestSecurityTokenType();
			XmlDocument authnResponseDocument = new XmlDocument();
			// by default whitespace is not preserved !!
			authnResponseDocument.PreserveWhitespace = true;
			authnResponseDocument.LoadXml(authnResponse);
			Console.WriteLine("XmlDocument response: " + authnResponseDocument.InnerXml);
			XmlElement requestTypeElement = authnResponseDocument.CreateElement("wst", "RequestType", "http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
			requestTypeElement.AppendChild(authnResponseDocument.CreateTextNode("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate#" + trustDomain));
			XmlElement validateTargetElement = authnResponseDocument.CreateElement("wst", "ValidateTarget", "http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
			validateTargetElement.AppendChild(authnResponseDocument.DocumentElement);
			requestSecurityToken.Any = new XmlElement[] { requestTypeElement, validateTargetElement };
			RequestSecurityTokenResponseType response =  this.client.RequestSecurityToken(requestSecurityToken);
			XmlElement[] results = response.Any;
			foreach (XmlElement result in results) {
				if ( result.LocalName.Equals("Status") ) {
					if ( result.FirstChild.LocalName.Equals("Code")) {
						if ( result.FirstChild.InnerText.Equals(STATUS_VALID) ) {
							return true;
						}
					}
				}
			}
			Console.WriteLine("Invalid Token");
			return false;
		}
	}
}
