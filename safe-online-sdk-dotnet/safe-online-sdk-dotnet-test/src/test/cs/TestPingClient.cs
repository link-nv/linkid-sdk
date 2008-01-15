using System;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestPingClient
	{
		[Test]
		public void TestMethod()
		{
			PingClient pingClient = new PingClientImpl("192.168.5.102:8080");
			pingClient.ping();
		}
	}
}
