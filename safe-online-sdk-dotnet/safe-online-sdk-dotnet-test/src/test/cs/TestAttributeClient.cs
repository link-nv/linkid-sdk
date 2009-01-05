/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
 
using System;
using NUnit.Framework;
using System.Security.Cryptography;
using System.Collections;
using System.Collections.Generic;

using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet.test.cs
{
		/// <summary>
	/// Test class for the OLAS Attribute Web Service.
	/// 
	/// Make sure you have created the test application in OLAS before executing these tests.
	/// Don't forget to add the test application's certificate to the OLAS PKI.
	/// 
	/// Following attributes need to be available in the targetted OLAS.
	/// <list type="bullet">
	/// <item>urn:test:multi:string</item>
	/// <item>urn:test:multi:date</item>
	/// <item>urn:test:compound : containing the 2 previous multivalued atributes</item>
	/// <item>urn:net:lin-k:safe-online:attribute:login</item>
	/// </list>
	/// 
	/// These attributes should be the application identity of the test application used in these tests.
	/// 
	/// You must login to the test application once as TestConstants.testLogin using the WebServerTest.
	/// </summary>
	[TestFixture]
	public class TestAttributeClient
	{
		[Test]
		public void TestGetStringAttribute()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			AttributeClient attributeClient = 
				new AttributeClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			string loginName = attributeClient.getAttributeValue<string>(userId, TestConstants.loginAttribute);
			Console.WriteLine("admin loginName: " + loginName);
			Assert.AreEqual(TestConstants.testLogin, loginName);
		}
		
		[Test]
		public void TestGetMultivaluedStringAttribute()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			AttributeClient attributeClient =
				new AttributeClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			string[] testStrings = attributeClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
			foreach(string testString in testStrings) {
				Console.WriteLine("testString: " + testString);
			}
		}

		[Test]
		public void TestGetMultivaluedDateAttribute()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			AttributeClient attributeClient =
				new AttributeClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword,
				                        TestConstants.olasCertPath);
			DateTime[] testDates = attributeClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
			foreach(DateTime testDate in testDates) {
				Console.WriteLine("testDate: " + testDate.ToString());
			}
		}

		[Test]
		public void TestGetCompoundAttribute()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			AttributeClient attributeClient =
				new AttributeClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			Dictionary<string, object>[] testCompounds = 
				attributeClient.getAttributeValue<Dictionary<string,object>[]>(userId, TestConstants.testCompoundAttribute);
			foreach(Dictionary<string,object> testCompound in testCompounds) {
				Console.WriteLine("Compound attribute");
				foreach(object key in testCompound.Keys) {
					Console.WriteLine("compound member " + key + " value=" + testCompound[key.ToString()]);
				}
			}
		}
		
		[Test]
		public void TestGetAttributesFromDictionary()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			AttributeClient attributeClient =
				new AttributeClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);

			Dictionary<string, object> attributeMap = new Dictionary<string, object>();
			attributeMap.Add(TestConstants.testMultiStringAttribute, null);
			attributeMap.Add(TestConstants.testMultiDateAttribute, null);
			
			attributeClient.getAttributeValues(userId, attributeMap);
			
			foreach(string key in attributeMap.Keys) {
				if (key.Equals(TestConstants.testMultiStringAttribute)) {
					string[] values = (string[]) attributeMap[key];
					foreach(string value in values) {
						Console.WriteLine("key: " + key + " value=" + value);
					}
				} else {
					DateTime[] values = (DateTime[]) attributeMap[key];
					foreach(DateTime date in values) {
						Console.WriteLine("key: " + key + " value=" + date.ToString());
					}
				}
			}
		}

		[Test]
		public void TestGetAttributesFromUserId()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			AttributeClient attributeClient =
				new AttributeClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);

			Dictionary<string, object> attributeMap = attributeClient.getAttributeValues(userId);
			
			foreach(string key in attributeMap.Keys) {
				if (key.Equals(TestConstants.testMultiStringAttribute)) {
					string[] values = (string[]) attributeMap[key];
					foreach(string value in values) {
						Console.WriteLine("-key: " + key + " value=" + value);
					}
				} else if (key.Equals(TestConstants.testMultiDateAttribute) ){
					DateTime[] values = (DateTime[]) attributeMap[key];
					foreach(DateTime date in values) {
						Console.WriteLine("-key: " + key + " value=" + date.ToString());
					}
				} else if (key.Equals(TestConstants.testCompoundAttribute)) {
					Dictionary<string, object>[] values = (Dictionary<string, object>[]) attributeMap[key];
					foreach(Dictionary<string, object> value in values) {
						Console.WriteLine("Compound attribute: " + key);
						foreach(string valueKey in value.Keys) {
							if (valueKey.Equals(TestConstants.testMultiStringAttribute)) {
								Console.WriteLine("key: " + valueKey + " value=" + (string)value[valueKey]);
							} else if (valueKey.Equals(TestConstants.testMultiDateAttribute) ){
								Console.WriteLine("key: " + valueKey + " value=" + ((DateTime)value[valueKey]).ToString());
							}
						}
					}
				}
			}
		}

	}
}
