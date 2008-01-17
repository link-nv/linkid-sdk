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

namespace safe_online_sdk_dotnet
{
	public class IdMappingClientImpl : IdMappingClient
	{
		private NameIdentifierMappingPortClient client;
		
		public IdMappingClientImpl(string location, string pfxFilename, string password)
		{
			/*
			BasicHttpBinding basicBinding = new BasicHttpBinding(BasicHttpSecurityMode.TransportWithMessageCredential);
			BasicHttpSecurity security = basicBinding.Security;
			security.Mode = BasicHttpSecurityMode.TransportWithMessageCredential;
			
			BasicHttpMessageSecurity messageSecurity = security.Message;
			messageSecurity.ClientCredentialType = BasicHttpMessageCredentialType.Certificate;
			messageSecurity.AlgorithmSuite = SecurityAlgorithmSuite.Default;
			
			HttpTransportSecurity transportSecurity = security.Transport;
			transportSecurity.ClientCredentialType = HttpClientCredentialType.None;
			transportSecurity.ProxyCredentialType = HttpProxyCredentialType.None;
			transportSecurity.Realm = "";

			BindingElementCollection bec = basicBinding.CreateBindingElements();
			TransportSecurityBindingElement tsp = bec.Find<TransportSecurityBindingElement>();
			HttpsTransportBindingElement httpsBinding = bec.Find<HttpsTransportBindingElement>();
			TextMessageEncodingBindingElement encoding = bec.Find<TextMessageEncodingBindingElement>();
			SecurityBindingElement securityBinding = bec.Find<SecurityBindingElement>();			
			CustomBinding binding = new CustomBinding(tsp, encoding, httpsBinding);
			*/
			/*
			CustomBinding binding = new CustomBinding();
			HttpsTransportBindingElement httpsTransport = new HttpsTransportBindingElement();
			TextMessageEncodingBindingElement encoding = new TextMessageEncodingBindingElement();
			encoding.MessageVersion = MessageVersion.Soap11;
			*/
			//SecurityBindingElement securityBinding =
			//	SecurityBindingElement.CreateCertificateOverTransportBindingElement(MessageSecurityVersion.WSSecurity10WSTrustFebruary2005WSSecureConversationFebruary2005WSSecurityPolicy11BasicSecurityProfile10);
			//SecurityBindingElement securityBinding = SecurityBindingElement.CreateSslNegotiationBindingElement(false);
			/*
			AsymmetricSecurityBindingElement securityBinding =
        		(AsymmetricSecurityBindingElement)SecurityBindingElement.
        		CreateMutualCertificateBindingElement(
				MessageSecurityVersion.WSSecurity10WSTrustFebruary2005WSSecureConversationFebruary2005WSSecurityPolicy11BasicSecurityProfile10, true);
			securityBinding.DefaultAlgorithmSuite = SecurityAlgorithmSuite.Default;
			securityBinding.SetKeyDerivation(false);
			securityBinding.SecurityHeaderLayout = SecurityHeaderLayout.Lax;
			*/
			//SslStreamSecurityBindingElement sslStreamSecurity = new SslStreamSecurityBindingElement();
			//binding.Elements.Add(securityBinding);
			
			/*
			TransportSecurityBindingElement securityBinding = new TransportSecurityBindingElement();
			securityBinding.MessageSecurityVersion = MessageSecurityVersion.WSSecurity10WSTrustFebruary2005WSSecureConversationFebruary2005WSSecurityPolicy11BasicSecurityProfile10;
			securityBinding.DefaultAlgorithmSuite = SecurityAlgorithmSuite.Default;
			securityBinding.SetKeyDerivation(false);
			X509SecurityTokenParameters certToken = new X509SecurityTokenParameters();
			certToken.InclusionMode = SecurityTokenInclusionMode.AlwaysToRecipient;
			certToken.ReferenceStyle = SecurityTokenReferenceStyle.Internal;
			certToken.RequireDerivedKeys = false;
			certToken.X509ReferenceStyle = X509KeyIdentifierClauseType.Any;
			securityBinding.EndpointSupportingTokenParameters.SignedEndorsing.Add(certToken);
			securityBinding.LocalClientSettings.DetectReplays = false;
			
			binding.Elements.Add(securityBinding);
			binding.Elements.Add(encoding);
			binding.Elements.Add(sslStreamSecurity);
			
			binding.Elements.Add(httpsTransport);
			*/
			/*
			WSHttpBinding binding = new WSHttpBinding(SecurityMode.TransportWithMessageCredential);
			WSHttpSecurity security = binding.Security;
			
			HttpTransportSecurity transportSecurity = security.Transport;
			transportSecurity.ClientCredentialType = HttpClientCredentialType.None;
			transportSecurity.ProxyCredentialType = HttpProxyCredentialType.None;
			transportSecurity.Realm = "";
			
			NonDualMessageSecurityOverHttp messageSecurity = security.Message;
			messageSecurity.ClientCredentialType = MessageCredentialType.Certificate;
			messageSecurity.NegotiateServiceCredential = false;
			messageSecurity.AlgorithmSuite = SecurityAlgorithmSuite.Default;
			*/
			
			ServicePointManager.ServerCertificateValidationCallback = new RemoteCertificateValidationCallback(SafeOnlineCertificateValidationCallback);
			
			string address = "https://" + location + "/safe-online-ws/idmapping";
			EndpointAddress remoteAddress = new EndpointAddress(address);
			
			//Binding safeOnlineBinding = new SafeOnlineBinding();
			
			//this.client = new NameIdentifierMappingPortClient(safeOnlineBinding, remoteAddress);
			
			//X509Certificate2 certificate = new X509Certificate2("C:\\work\\test.pfx", "secret");
			/*
			this.client.ClientCredentials.ClientCertificate.SetCertificate(StoreLocation.CurrentUser, 
			                                                               StoreName.My, 
			                                                               X509FindType.FindBySubjectName, 
			                                                               "Test");
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			*/
			//this.client.Endpoint.Contract.Behaviors.Add(new SignBodyBehavior());
			//this.client.Endpoint.Behaviors.Add(new SafeOnlineMessageInspectorBehavior());
			/*
			X509Store store = new X509Store(StoreName.My, StoreLocation.CurrentUser);
			store.Open(OpenFlags.ReadOnly);
			X509Certificate2 cert = store.Certificates.Find(X509FindType.FindBySubjectName, "Test", false)[0];
			this.client.ClientCredentials.ClientCertificate.Certificate = cert;
			*/
			//Console.WriteLine("cert: " + this.client.ClientCredentials.ClientCertificate.Certificate);
			
			/*
			ChannelFactory<NameIdentifierMappingPort> channelFactory =
				new ChannelFactory<NameIdentifierMappingPort>(safeOnlineBinding, remoteAddress);
			channelFactory.Credentials.ClientCertificate.SetCertificate(StoreLocation.CurrentUser, 
			                                                               StoreName.My, 
			                                                               X509FindType.FindBySubjectName, 
			                                                              "Test");
			*/
			//channelFactory.Credentials.ClientCertificate.Certificate =
			//channelFactory.Endpoint.Behaviors.Add(new SafeOnlineMessageInspectorBehavior());
			//channelFactory.Endpoint.Contract.Behaviors.Add(new SignBodyBehavior());
			/*
			 * Next does not work at all.
			foreach (OperationDescription operation in channelFactory.Endpoint.Contract.Operations) {
				operation.ProtectionLevel = ProtectionLevel.Sign
				Console.WriteLine("operation: " + operation.Name);
			}
			*/
			//channelFactory.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			//this.client = channelFactory.CreateChannel();
			
			BasicHttpBinding binding = new BasicHttpBinding(BasicHttpSecurityMode.TransportWithMessageCredential);
			BasicHttpSecurity security = binding.Security;
			
			BasicHttpMessageSecurity messageSecurity = security.Message;
			messageSecurity.ClientCredentialType = BasicHttpMessageCredentialType.Certificate;
			messageSecurity.AlgorithmSuite = SecurityAlgorithmSuite.Default;
			
			HttpTransportSecurity transportSecurity = security.Transport;
			transportSecurity.ClientCredentialType = HttpClientCredentialType.None;
			transportSecurity.ProxyCredentialType = HttpProxyCredentialType.None;
			transportSecurity.Realm = "";
			
			this.client = new NameIdentifierMappingPortClient(binding, remoteAddress);
			
			X509Certificate2 certificate = new X509Certificate2(pfxFilename, password);
			this.client.ClientCredentials.ClientCertificate.Certificate = certificate;
		}
		
		public string getUserId(String username) {
			/*
			NameIdentifierMappingQueryRequest request = new NameIdentifierMappingQueryRequest();
			NameIDType nameId = new NameIDType();
			nameId.Value = userId;
			request.NameIDMappingRequest = new NameIDMappingRequestType();
			request.NameIDMappingRequest.Item = nameId;
			this.client.NameIdentifierMappingQuery(request);
			*/
			NameIDMappingRequestType request = new NameIDMappingRequestType();
			NameIDPolicyType nameIDPolicy = new NameIDPolicyType();
			nameIDPolicy.Format = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
			request.NameIDPolicy = nameIDPolicy;
			NameIDType nameId = new NameIDType();
			nameId.Value = username;
			request.Item = nameId;
			NameIDMappingResponseType response = this.client.NameIdentifierMappingQuery(request);
			string statusCode = response.Status.StatusCode.Value;
			if (!"urn:oasis:names:tc:SAML:2.0:status:Success".Equals(statusCode)) {
			    return null;	
			}
			NameIDType responseNameId = (NameIDType) response.Item;
			String userId = responseNameId.Value;
			return userId;
		}
		
		
		public static bool SafeOnlineCertificateValidationCallback(Object sender,
      		X509Certificate certificate,
      		X509Chain chain,
      		SslPolicyErrors sslPolicyErrors) {
			Console.WriteLine("SafeOnline Certificate Validation Callback");
        	return true;
		}
	}
	
	public class SafeOnlineClientMessageInspector : IClientMessageInspector {
		
		public object BeforeSendRequest(ref Message request, IClientChannel channel)
		{
			Console.WriteLine(request);
			return null;
		}
		
		public void AfterReceiveReply(ref Message reply, object correlationState)
		{
			Console.WriteLine(reply);
		}
	}
	
	public class SafeOnlineMessageInspectorBehavior : IEndpointBehavior {
		
		
		public void Validate(ServiceEndpoint endpoint)
		{
		}
		
		public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
		{
			ChannelProtectionRequirements requirements = bindingParameters.Find<ChannelProtectionRequirements>();
			MessagePartSpecification part = new MessagePartSpecification(true);
			requirements.OutgoingSignatureParts.AddParts(part);
		}
		
		public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
		{
			clientRuntime.MessageInspectors.Add(new SafeOnlineClientMessageInspector());
		}
		
		public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
		{
			throw new NotImplementedException();
		}
	}
	
	public class SignBodyBehavior : Attribute, IContractBehavior{
		
		public void Validate(ContractDescription contractDescription, 
		                     ServiceEndpoint endpoint)
		{
		}
		
		public void ApplyDispatchBehavior(ContractDescription contractDescription, 
		                                  ServiceEndpoint endpoint, 
		                                  DispatchRuntime dispatchRuntime)
		{
		}
		
		public void ApplyClientBehavior(ContractDescription contractDescription, 
		                                ServiceEndpoint endpoint, 
		                                ClientRuntime clientRuntime)
		{
			foreach (OperationDescription operation in contractDescription.Operations) {
				operation.ProtectionLevel = ProtectionLevel.Sign;
				Console.WriteLine("applyClientBehavior: operation: " + operation.Name);
			}
			ClientOperation clientOperation = clientRuntime.Operations["NameIdentifierMappingQuery"];
		}
		
		public void AddBindingParameters(ContractDescription contractDescription, 
		                                 ServiceEndpoint endpoint, 
		                                 BindingParameterCollection bindingParameters)
		{
			Console.WriteLine("add outgoing signature parts");
			ChannelProtectionRequirements requirements = bindingParameters.Find<ChannelProtectionRequirements>();
			MessagePartSpecification part = new MessagePartSpecification(true);
			requirements.OutgoingSignatureParts.AddParts(part);
		}
	}
	
	public class SafeOnlineBinding : Binding {
		private BindingElementCollection bindingElements;
		
		public SafeOnlineBinding() {
			HttpsTransportBindingElement httpsTransport = new HttpsTransportBindingElement();
			TextMessageEncodingBindingElement encoding = new TextMessageEncodingBindingElement();
			encoding.MessageVersion = MessageVersion.Soap11;
			
			TransportSecurityBindingElement securityBinding = new TransportSecurityBindingElement();
			securityBinding.MessageSecurityVersion = MessageSecurityVersion.WSSecurity10WSTrustFebruary2005WSSecureConversationFebruary2005WSSecurityPolicy11BasicSecurityProfile10;
			securityBinding.DefaultAlgorithmSuite = SecurityAlgorithmSuite.Default;
			securityBinding.SetKeyDerivation(false);
			X509SecurityTokenParameters certToken = new X509SecurityTokenParameters();
			certToken.InclusionMode = SecurityTokenInclusionMode.AlwaysToRecipient;
			certToken.ReferenceStyle = SecurityTokenReferenceStyle.Internal;
			certToken.RequireDerivedKeys = false;
			certToken.X509ReferenceStyle = X509KeyIdentifierClauseType.Any;
			securityBinding.EndpointSupportingTokenParameters.SignedEndorsing.Add(certToken);
			securityBinding.LocalClientSettings.DetectReplays = false;
			
			this.bindingElements = new BindingElementCollection();
			this.bindingElements.Add(securityBinding);
			this.bindingElements.Add(encoding);
			this.bindingElements.Add(httpsTransport);
		}
		
		public override BindingElementCollection CreateBindingElements() {
			Console.WriteLine("create binding elements");
			return this.bindingElements.Clone();
		}
		
		public override string Scheme {
			get { return "https"; }
		}
		
		public override IChannelFactory<TChannel> BuildChannelFactory<TChannel>(BindingParameterCollection parameters)
		{
			Console.WriteLine("build channel factory");
			return null;
		}
		
		public override bool CanBuildChannelFactory<TChannel>(BindingParameterCollection parameters)
		{
			Console.WriteLine("can build channel factory");
			return true;
		}
	}
}
