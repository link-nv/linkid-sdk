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
			
			NotificationProducerClient producerClient = 
				new NotificationProducerClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, 
				                                   TestConstants.testPfxPassword, TestConstants.olasCertPath);
			try {
				producerClient.subscribe(TestConstants.olasTopicRemoveUser, testAddress);
				producerClient.subscribe(TestConstants.olasTopicUnsubscribeUser, testAddress);
			} catch(SubscriptionFailedException e) {
				Console.WriteLine("Subscription failed: " + e.Message);
				Assert.Fail();
			}
			
			NotificationSubscriptionManagerClient subscriptionManagerClient =
				new NotificationSubscriptionManagerClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath,
				                                              TestConstants.testPfxPassword, TestConstants.olasCertPath);
			subscriptionManagerClient.unsubscribe(TestConstants.olasTopicRemoveUser,testAddress);
			subscriptionManagerClient.unsubscribe(TestConstants.olasTopicUnsubscribeUser,testAddress);
		}
	}
}
