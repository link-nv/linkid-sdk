/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using IdMappingWSNamespace;
using System;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Security;
using System.ServiceModel.Security.Tokens;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the OLAS ID Mapping Web Service.
	/// 
	/// This is only a proof-of-concept implementation.
	/// </summary>
	public class IdMappingClientImpl : IdMappingClient
	{
		private NameIdentifierMappingPortClient client;
		
		public IdMappingClientImpl(string location, string appPfxPath, string appPfxPassword, string olasCertPath)
		{
			X509Certificate2 appCertificate = new X509Certificate2(appPfxPath, appPfxPassword);
			X509Certificate2 olasCertificate = new X509Certificate2(olasCertPath);
			
			ServicePointManager.ServerCertificateValidationCallback = 
				new RemoteCertificateValidationCallback(WCFUtil.AnyCertificateValidationCallback);
			
			string address = "https://" + location + "/safe-online-ws/idmapping";
			EndpointAddress remoteAddress = new EndpointAddress(address);
					
			this.client = new NameIdentifierMappingPortClient(new OlasBinding(olasCertificate), remoteAddress);
			
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = olasCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			this.client.Endpoint.Behaviors.Add(new LoggingBehavior());
		}
		
		public string getUserId(String username) {
			NameIDMappingRequestType request = new NameIDMappingRequestType();
			NameIDPolicyType nameIDPolicy = new NameIDPolicyType();
			nameIDPolicy.Format = Saml2Constants.SAML2_NAMEID_FORMAT_PERSISTENT;
			request.NameIDPolicy = nameIDPolicy;
			NameIDType nameId = new NameIDType();
			nameId.Value = username;
			request.Item = nameId;
			Console.WriteLine("get name id: " + username);
			NameIDMappingResponseType response = this.client.NameIdentifierMappingQuery(request);
			string statusCode = response.Status.StatusCode.Value;
			if (!Saml2Constants.SAML2_STATUS_SUCCESS.Equals(statusCode)) {
			    return null;	
			}
			NameIDType responseNameId = (NameIDType) response.Item;
			String userId = responseNameId.Value;
			return userId;
		}
	}
}
