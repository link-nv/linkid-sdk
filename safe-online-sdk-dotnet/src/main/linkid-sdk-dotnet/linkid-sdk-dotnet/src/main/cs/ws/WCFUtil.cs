/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Security;
using System.ServiceModel.Security.Tokens;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// WCFUtil.
	/// 
	/// Utility class used by the WCF clients.
	/// </summary>
	public class WCFUtil
	{
		/// <summary>
		/// SOAP header required by the Liberty ID-WSF Data Service to identity the resource.
		/// For linkID this resource is the subject's UUID, which can be acquired using the ID Mapping Service.
		/// </summary>
		public static readonly string TARGET_IDENTITY_HEADER_NAME = "TargetIdentity";
		
		public static readonly string TARGET_IDENTITY_HEADER_NAMESPACE = "urn:liberty:sb:2005-11";

		/// <summary>
		/// Certificate validation callback to accepts any SSL certificate.
		/// </summary>
		public static bool AnyCertificateValidationCallback(Object sender,
      		X509Certificate certificate,
      		X509Chain chain,
      		SslPolicyErrors sslPolicyErrors) {
			Console.WriteLine("Certificate Validation Callback");
        	return true;
		}
		
		/// <summary>
		/// BasicHttpBinding with SSL Transport Security. Provides NO message integrity.
		/// This binding can also be used, but the flag has to be set in the operator webapp.
		/// </summary>
		/// <returns></returns>
		public static BasicHttpBinding BasicHttpOverSSLBinding() {
			BasicHttpBinding binding = new BasicHttpBinding(BasicHttpSecurityMode.TransportWithMessageCredential);
			BasicHttpSecurity security = binding.Security;
			
			BasicHttpMessageSecurity messageSecurity = security.Message;
			messageSecurity.ClientCredentialType = BasicHttpMessageCredentialType.Certificate;
			messageSecurity.AlgorithmSuite = SecurityAlgorithmSuite.Default;
			
			HttpTransportSecurity transportSecurity = security.Transport;
			transportSecurity.ClientCredentialType = HttpClientCredentialType.None;
			transportSecurity.ProxyCredentialType = HttpProxyCredentialType.None;
			transportSecurity.Realm = "";
			return binding;
		}

	}
}
