using System;
using System.ServiceModel;

namespace safe_online_sdk_dotnet
{
	public class IdMappingClientImpl : IdMappingClient
	{
		private NameIdentifierMappingPortClient client;
		
		public IdMappingClientImpl(string location)
		{
			BasicHttpBinding binding = new BasicHttpBinding();
			EndpointAddress remoteAddress = 
               new EndpointAddress("http://" + location + "/safe-online-ws/idmapping");
			this.client = new NameIdentifierMappingPortClient(
				binding, remoteAddress);
		}
		
		public string getUsername(String userId) {
			NameIDMappingRequestType request = new NameIDMappingRequestType();
			NameIDMappingResponseType response = this.client.NameIdentifierMappingQuery(request);
			return null;
		}
	}
}
