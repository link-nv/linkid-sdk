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

namespace safe_online_sdk_dotnet
{
	public class STSClientImpl : STSClient
	{
		private SecurityTokenServicePortClient client;
		
		public STSClientImpl(string location, string pfxFilename, string password)
		{
			ServicePointManager.ServerCertificateValidationCallback = 
				new RemoteCertificateValidationCallback(SafeOnlineCertificateValidationCallback);
			string address = "https://" + location + "/safe-online-ws/sts";
			EndpointAddress remoteAddress = new EndpointAddress(address);
			
			BasicHttpBinding binding = new BasicHttpBinding(BasicHttpSecurityMode.TransportWithMessageCredential);
			BasicHttpSecurity security = binding.Security;
			
			BasicHttpMessageSecurity messageSecurity = security.Message;
			messageSecurity.ClientCredentialType = BasicHttpMessageCredentialType.Certificate;
			messageSecurity.AlgorithmSuite = SecurityAlgorithmSuite.Default;
			
			HttpTransportSecurity transportSecurity = security.Transport;
			transportSecurity.ClientCredentialType = HttpClientCredentialType.None;
			transportSecurity.ProxyCredentialType = HttpProxyCredentialType.None;
			transportSecurity.Realm = "";
			
			this.client = new SecurityTokenServicePortClient(binding, remoteAddress);
			
			X509Certificate2 certificate = new X509Certificate2(pfxFilename, password);
			this.client.ClientCredentials.ClientCertificate.Certificate = certificate;
		}
		
		public bool validateAuthnResponse(string authnResponse) {
			RequestSecurityTokenType requestSecurityToken = new RequestSecurityTokenType();
			XmlDocument authnResponseDocument = new XmlDocument();
			authnResponseDocument.LoadXml(authnResponse);
			XmlElement requestTypeElement = authnResponseDocument.CreateElement("wst", "RequestType", "http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
			requestTypeElement.AppendChild(authnResponseDocument.CreateTextNode("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate"));
			XmlElement validateTargetElement = authnResponseDocument.CreateElement("wst", "ValidateTarget", "http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
			validateTargetElement.AppendChild(authnResponseDocument.DocumentElement);
			requestSecurityToken.Any = new XmlElement[] { requestTypeElement, validateTargetElement };
			RequestSecurityTokenResponseType response =  this.client.RequestSecurityToken(requestSecurityToken);
			XmlElement[] results = response.Any;
			foreach (XmlElement result in results) {
				Console.WriteLine("result: {0}", result.InnerXml);
			}
			return false;
		}
		
		public static bool SafeOnlineCertificateValidationCallback(Object sender,
      		X509Certificate certificate,
      		X509Chain chain,
      		SslPolicyErrors sslPolicyErrors) {
			Console.WriteLine("SafeOnline Certificate Validation Callback");
        	return true;
		}
	}
}
