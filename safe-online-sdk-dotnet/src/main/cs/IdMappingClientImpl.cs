using System;
using System.ServiceModel;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel.Description;
using System.ServiceModel.Security;
using System.Net;
using System.Net.Security;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Channels;
using System.ServiceModel;

namespace safe_online_sdk_dotnet
{
	public class IdMappingClientImpl : IdMappingClient
	{
		private NameIdentifierMappingPortClient client;
		
		public IdMappingClientImpl(string location, string pfxFilename, string password)
		{
			BasicHttpBinding binding = new BasicHttpBinding(BasicHttpSecurityMode.TransportWithMessageCredential);
			BasicHttpSecurity security = binding.Security;
			security.Mode = BasicHttpSecurityMode.TransportWithMessageCredential;
			
			BasicHttpMessageSecurity messageSecurity = security.Message;
			messageSecurity.ClientCredentialType = BasicHttpMessageCredentialType.Certificate;
			messageSecurity.AlgorithmSuite = SecurityAlgorithmSuite.Default;
			
			HttpTransportSecurity transportSecurity = security.Transport;
			transportSecurity.ClientCredentialType = HttpClientCredentialType.None;
			transportSecurity.ProxyCredentialType = HttpProxyCredentialType.None;
			transportSecurity.Realm = "";

			ServicePointManager.ServerCertificateValidationCallback = new RemoteCertificateValidationCallback(SafeOnlineCertificateValidationCallback);
			
			EndpointAddress remoteAddress =
               new EndpointAddress("https://" + location + "/safe-online-ws/idmapping");
			
			this.client = new NameIdentifierMappingPortClient(binding, remoteAddress);
			this.client.ClientCredentials.ClientCertificate.SetCertificate(StoreLocation.CurrentUser, 
			                                                               StoreName.My, 
			                                                               X509FindType.FindBySubjectName, 
			                                                              "Test");
			/*
			X509Store store = new X509Store(StoreName.My, StoreLocation.CurrentUser);
			store.Open(OpenFlags.ReadOnly);
			X509Certificate2 cert = store.Certificates.Find(X509FindType.FindBySubjectName, "Test", false)[0];
			this.client.ClientCredentials.ClientCertificate.Certificate = cert;
			*/
			//Console.WriteLine("cert: " + this.client.ClientCredentials.ClientCertificate.Certificate);
			/*
			ChannelFactory<NameIdentifierMappingPort> channelFactory =
				new ChannelFactory<NameIdentifierMappingPort>(binding, remoteAddress);
			channelFactory.Credentials.ClientCertificate.SetCertificate(StoreLocation.CurrentUser, 
			                                                               StoreName.My, 
			                                                               X509FindType.FindBySubjectName, 
			                                                              "Test");
			*/
			//channelFactory.Credentials.ClientCertificate.Certificate =
			//channelFactory.Endpoint.Behaviors.Add(new SafeOnlineMessageInspectorBehavior());
			//channelFactory.Endpoint.Contract.Behaviors.Insert(0, new SignBodyBehavior());
			/*
			 * Next does not work at all.
			channelFactory.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			foreach (OperationDescription operation in channelFactory.Endpoint.Contract.Operations) {
				operation.ProtectionLevel = ProtectionLevel.Sign
				Console.WriteLine("operation: " + operation.Name);
			}
			*/
			//this.client = channelFactory.CreateChannel();
		}
		
		public string getUsername(String userId) {
			/*
			NameIdentifierMappingQueryRequest request = new NameIdentifierMappingQueryRequest();
			NameIDType nameId = new NameIDType();
			nameId.Value = userId;
			request.NameIDMappingRequest = new NameIDMappingRequestType();
			request.NameIDMappingRequest.Item = nameId;
			this.client.NameIdentifierMappingQuery(request);
			*/
			NameIDMappingRequestType request = new NameIDMappingRequestType();
			this.client.NameIdentifierMappingQuery(request);
			return null;
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
	
	public class SignBodyBehavior : IContractBehavior {
		
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
}
