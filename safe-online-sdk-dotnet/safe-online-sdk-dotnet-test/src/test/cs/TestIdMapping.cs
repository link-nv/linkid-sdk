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
	public class TestIdMapping
	{
		[Test]
		public void TestGetUserId()
		{
			IdMappingClient idMappingClient =
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId("admin");
			Console.WriteLine("admin userId: " + userId);
		}
	}
}
