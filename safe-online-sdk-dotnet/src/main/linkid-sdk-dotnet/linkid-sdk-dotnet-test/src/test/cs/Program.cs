/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;

using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet_test
{
	class Program
	{
		public static void Main(string[] args)
		{
			Console.WriteLine("starting web server...");
			new WebServerTest().StartWebServer();
		}
	}
}
