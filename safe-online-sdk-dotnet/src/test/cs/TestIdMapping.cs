using System;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;
using System.Security.Cryptography;
using Org.BouncyCastle.Pkcs;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Math;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.Crypto.Generators;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestIdMapping
	{
		[Test]
		public void TestMethod()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl("192.168.5.102:8443", "C:\\work\\test.pfx", "secret");
			String userId = idMappingClient.getUserId("admin");
			Console.WriteLine("admin userId: " + userId);
		}
	}
}
