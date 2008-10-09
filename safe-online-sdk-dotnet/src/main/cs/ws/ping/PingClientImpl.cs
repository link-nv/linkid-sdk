/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Collections.Generic;
using System.ServiceModel;
using System.Diagnostics;
using PingWSNamespace;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the OLAS Ping Web Service.
	/// 
	/// This is only a proof-of-concept implementation.
	/// </summary>
	public class PingClientImpl : PingClient
	{
		private static TraceSource traceSource = new TraceSource("TraceTest");
		
		private PingPortClient pingPortClient;
		
		public PingClientImpl(string location) {
			traceSource.TraceInformation("ping client constructor");
			BasicHttpBinding binding = new BasicHttpBinding();
			EndpointAddress address = 
               new EndpointAddress("http://" + location + "/safe-online-ws/ping");
			this.pingPortClient = new PingPortClient(binding, address);
		}
		
		public void ping() {
			Request request = new Request();
			this.pingPortClient.PingOperation(request);
		}
	}
}
