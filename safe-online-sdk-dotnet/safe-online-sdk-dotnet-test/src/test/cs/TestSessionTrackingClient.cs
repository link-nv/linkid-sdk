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

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestSessionTrackingClient
	{
		[Test]
		public void TestGetAssertions()
		{
			SessionTrackingClient sessionTrackingClient = new SessionTrackingClientImpl(
				TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword,
				                        TestConstants.olasCertPath);
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
