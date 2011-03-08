/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 31/12/2008
 * Time: 8:50
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using safe_online_sdk_dotnet;
using System;
using System.Security.Cryptography;
using NUnit.Framework;
using System.Collections;
using System.Collections.Generic;

using safe_online_sdk_dotnet_test.test.cs;
using System.Security.Cryptography.X509Certificates;

namespace safe_online_sdk_dotnet.test.cs
{
	/// <summary>
	/// Test class for the WS-Notification clients.
	/// </summary>
	[TestFixture]
	public class TestNotificationClient
	{
		[Test]
		public void testSubscribeUnsubscribe() {
			string testAddress = "test-address";

            X509Certificate2 appCertificate = KeyStoreUtil.loadCertificate(TestConstants.testPfxPath, TestConstants.testPfxPassword, false);
            X509Certificate2 linkidCertificate = new X509Certificate2(TestConstants.linkidCertPath);

			NotificationProducerClient producerClient = 
				new NotificationProducerClientImpl(TestConstants.wsLocation, appCertificate, linkidCertificate);

			try {
				producerClient.subscribe(TestConstants.linkidTopicRemoveUser, testAddress);
				producerClient.subscribe(TestConstants.linkidTopicUnsubscribeUser, testAddress);
			} catch(SubscriptionFailedException e) {
				Console.WriteLine("Subscription failed: " + e.Message);
				Assert.Fail();
			}
			
			NotificationSubscriptionManagerClient subscriptionManagerClient =
				new NotificationSubscriptionManagerClientImpl(TestConstants.wsLocation, appCertificate, linkidCertificate);
			subscriptionManagerClient.unsubscribe(TestConstants.linkidTopicRemoveUser,testAddress);
			subscriptionManagerClient.unsubscribe(TestConstants.linkidTopicUnsubscribeUser,testAddress);
		}
	}
}
