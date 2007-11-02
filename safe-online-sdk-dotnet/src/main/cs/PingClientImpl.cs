using System;
using System.Collections.Generic;
using System.ServiceModel;
using System.Diagnostics;

namespace safe_online_sdk_dotnet
{
	public class PingClientImpl : PingClient
	{
		private static TraceSource traceSource = new TraceSource("TraceTest");
		
		private PingPortClient pingPortClient;
		
		public PingClientImpl(string location) {
			traceSource.TraceInformation("ping client constructor");
			BasicHttpBinding httpBinding = new BasicHttpBinding();
			EndpointAddress address = 
               new EndpointAddress("http://" + location + "/safe-online-ws/ping");
			this.pingPortClient = new PingPortClient(httpBinding, address);
		}
		
		public void ping() {
			Request request = new Request();
			this.pingPortClient.PingOperation(request);
		}
	}
}
