/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using NUnit.Framework;
using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestPingClient
	{
		[Test]
		public void TestPing()
		{
			PingClient pingClient = new PingClientImpl(TestConstants.linkidHost + ":8080");
			pingClient.ping();
		}
	}
}
