/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 7/04/2009
 * Time: 8:41
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Collections.Generic;
using NUnit.Framework;
using safe_online_sdk_dotnet_test.test.cs;
using System.Security.Cryptography.X509Certificates;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestSessionTrackingClient
	{
        [SetUp]
        public void Init()
        {
            TestConstants.initForDevelopment();
        }

		[Test]
		public void TestGetAssertions()
		{
            X509Certificate2 appCertificate = KeyStoreUtil.loadCertificate(TestConstants.testPfxPath, TestConstants.testPfxPassword, false);
            X509Certificate2 linkidCertificate = new X509Certificate2(TestConstants.linkidCertPath);

			SessionTrackingClient sessionTrackingClient = new SessionTrackingClientImpl(
				TestConstants.wsLocation, appCertificate, linkidCertificate);
			List<SessionAssertion> assertions = sessionTrackingClient.getAssertions("test-session", null, null);
			foreach(SessionAssertion assertion in assertions) {
				Console.WriteLine("assertion: subject=" + assertion.getSubject());
				Console.WriteLine("           pool=" + assertion.getApplicationPool());
				foreach(DateTime time in assertion.getAuthentications().Keys) {
					Console.WriteLine("          authentication: " + assertion.getAuthentications()[time] + " @ " + time.ToString());
				}
			}
		}
	}
}
