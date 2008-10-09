/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
 
using System;
using NUnit.Framework;
using System.Security.Cryptography;
using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestAttributeClient
	{
		[Test]
		public void TestGetStringAttribute()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(Constants.wsLocation, Constants.testPfxPath, Constants.testPfxPassword, Constants.olasCertPath);
			String userId = idMappingClient.getUserId("admin");
			Console.WriteLine("admin userId: " + userId);

			AttributeClient attributeClient = 
				new AttributeClientImpl(Constants.wsLocation, Constants.testPfxPath, Constants.testPfxPassword, Constants.olasCertPath);
			String loginName = attributeClient.getStringAttributeValue(userId, Constants.loginAttribute);
			Console.WriteLine("admin loginName: " + loginName);
			Assert.AreEqual("admin", loginName);
		}
	}
}
