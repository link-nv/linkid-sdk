/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
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
	/// Test class for the OLAS Data Web Service.
	/// 
	/// Make sure you have created the test application in OLAS before executing these tests.
	/// Don't forget to add the test application's certificate to the OLAS PKI.
	/// 
	/// Following attributes need to be available in the targetted OLAS.
	/// <list type="bullet">
	/// <item>urn:test:multi:string</item>
	/// <item>urn:test:multi:date</item>
	/// <item>urn:test:compound : containing the 2 previous multivalued atributes</item>
	/// <item>urn:test:single:string</item>
	/// <item>urn:test:single:date</item>
	/// <item>urn:net:lin-k:safe-online:attribute:login</item>
	/// </list>
	/// 
	/// These attributes should also have as attribute provider the test application used in these tests.
	/// 
	/// You must login to the test application once as TestConstants.testLogin using the WebServerTest.
	/// </summary>
	[TestFixture]
	public class TestDataClient
	{
		[Test]
		public void TestGetStringAttribute()
		{
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DataClient dataClient = 
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                   TestConstants.olasCertPath);
			String loginName = dataClient.getAttributeValue<string>(userId, TestConstants.loginAttribute);
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

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			string[] testStrings = dataClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
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

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword,
				                        TestConstants.olasCertPath);
			DateTime[] testDates = dataClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
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

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			Dictionary<string, object>[] testCompounds = 
				dataClient.getAttributeValue<Dictionary<string,object>[]>(userId, TestConstants.testCompoundAttribute);
			if ( null != testCompounds ) {
				foreach(Dictionary<string,object> testCompound in testCompounds) {
					Console.WriteLine("Compound attribute");
					foreach(object key in testCompound.Keys) {
						Console.WriteLine("compound member " + key + " value=" + testCompound[key.ToString()]);
					}
				}
			}
		}
		
		[Test]
		public void TestCreateStringAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			String testValue = "test";
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			string result = dataClient.getAttributeValue<string>(userId, TestConstants.testSingleStringAttribute);
			if ( null != result ) {
				dataClient.removeAttribute(userId, TestConstants.testSingleStringAttribute, null);
			}
			
			dataClient.createAttribute(userId, TestConstants.testSingleStringAttribute, testValue);
			
			result = dataClient.getAttributeValue<string>(userId, TestConstants.testSingleStringAttribute);
			Assert.AreEqual(testValue, result);
		}

		[Test]
		public void TestCreateDateAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			DateTime result = dataClient.getAttributeValue<DateTime>(userId, TestConstants.testSingleDateAttribute);
			if ( ! result.Equals(DateTime.MinValue) ) {
				dataClient.removeAttribute(userId, TestConstants.testSingleDateAttribute, null);
			}
			
			dataClient.createAttribute(userId, TestConstants.testSingleDateAttribute, DateTime.UtcNow);
			
			result = dataClient.getAttributeValue<DateTime>(userId, TestConstants.testSingleDateAttribute);
			Assert.AreNotEqual(DateTime.MinValue, result);
			Console.WriteLine("result: " + result);
		}

		[Test]
		public void TestCreateMultivaluedStringAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			string[] testValues = new string[]{"test 1", "test 2"};
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			string[] result = dataClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
			if ( null != result ) {
				dataClient.removeAttribute(userId, TestConstants.testMultiStringAttribute, null);
			}
			dataClient.createAttribute(userId, TestConstants.testMultiStringAttribute, testValues);
			
			result = dataClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
			Assert.IsNotNull(result);
			Assert.AreEqual(testValues, result);
		}

		[Test]
		public void TestCreateMultivaluedDateAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DateTime[] testValues = new DateTime[]{DateTime.UtcNow, DateTime.UtcNow.AddDays(1)};
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			DateTime[] result = dataClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
			if ( null != result ) {
				dataClient.removeAttribute(userId, TestConstants.testMultiDateAttribute, null);
			}
			dataClient.createAttribute(userId, TestConstants.testMultiDateAttribute, testValues);
			
			result = dataClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
			Assert.IsNotNull(result);
			foreach(DateTime resultDate in result) {
				Console.WriteLine("result date: " + resultDate);
			}
		}

		[Test]
		public void TestCreateCompoundAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			Dictionary<string, object> testValues = new Dictionary<string, object>();
			testValues.Add(TestConstants.testMultiStringAttribute, "test 1");
			testValues.Add(TestConstants.testMultiDateAttribute, DateTime.UtcNow);
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);

			dataClient.createAttribute(userId, TestConstants.testCompoundAttribute, testValues);
			
			Dictionary<string, object>[] result = dataClient.getAttributeValue<Dictionary<string, object>[]>(userId, TestConstants.testCompoundAttribute);
			Assert.IsNotNull(result);
			foreach(Dictionary<string,object> resultCompound in result) {
				Console.WriteLine("Compound attribute");
				foreach(object key in resultCompound.Keys) {
					Console.WriteLine("compound member " + key + " value=" + resultCompound[key.ToString()]);
				}
			}
		}

		[Test]
		public void TestRemoveStringAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			string result = dataClient.getAttributeValue<string>(userId, TestConstants.testSingleStringAttribute);
			if ( null == result ) {
				dataClient.createAttribute(userId, TestConstants.testSingleStringAttribute, "test");
			}
			
			dataClient.removeAttribute(userId, TestConstants.testSingleStringAttribute, null);
			result = dataClient.getAttributeValue<string>(userId, TestConstants.testSingleStringAttribute);
			Assert.IsNull(result);
		}

		[Test]
		public void TestRemoveDateAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			DateTime result = dataClient.getAttributeValue<DateTime>(userId, TestConstants.testSingleDateAttribute);
			if ( result.Equals(DateTime.MinValue) ) {
				dataClient.createAttribute(userId, TestConstants.testSingleDateAttribute, DateTime.UtcNow);
			}
			
			dataClient.removeAttribute(userId, TestConstants.testSingleDateAttribute, null);
			result = dataClient.getAttributeValue<DateTime>(userId, TestConstants.testSingleDateAttribute);
			Assert.AreEqual(DateTime.MinValue, result);
		}

		[Test]
		public void TestRemoveMultivaluedStringAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			string[] result = dataClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
			if ( null == result) {
				dataClient.createAttribute(userId, TestConstants.testMultiStringAttribute, new string[] {"test 1", "test 2"});
			}
			
			dataClient.removeAttribute(userId, TestConstants.testMultiStringAttribute, null);
			result = dataClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
			Assert.IsNull(result);
		}

		[Test]
		public void TestRemoveMultivaluedDateAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			DateTime[] result = dataClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
			if ( null == result) {
				dataClient.createAttribute(userId, TestConstants.testMultiDateAttribute, new DateTime[] {DateTime.UtcNow, DateTime.UtcNow.AddDays(1)});
			}
			
			dataClient.removeAttribute(userId, TestConstants.testMultiDateAttribute, null);
			result = dataClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
			Assert.IsNull(result);
		}

		[Test]
		public void TestRemoveCompoundAttributes() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			Dictionary<string, object>[] result = 
				dataClient.getAttributeValue<Dictionary<string, object>[]>(userId, TestConstants.testCompoundAttribute);
			if ( null == result ) {
				Dictionary<string, object> testValues = new Dictionary<string, object>();
				testValues.Add(TestConstants.testMultiStringAttribute, "test 1");
				testValues.Add(TestConstants.testMultiDateAttribute, DateTime.UtcNow);
				dataClient.createAttribute(userId, TestConstants.testCompoundAttribute, testValues);
				result = 
					dataClient.getAttributeValue<Dictionary<string, object>[]>(userId, TestConstants.testCompoundAttribute);
			}
			
			foreach(Dictionary<string, object> resultCompound in result ) {
				string compoundId = (string) resultCompound[WebServiceConstants.ATTRIBUTE_ID_KEY];
				Console.WriteLine("remove compound attribute: @Id=" + compoundId);
				dataClient.removeAttribute(userId, TestConstants.testCompoundAttribute, compoundId);
			}
			
			result = dataClient.getAttributeValue<Dictionary<string, object>[]>(userId, TestConstants.testCompoundAttribute);
			Assert.IsNull(result);
		}

		[Test]
		public void TestModifyStringAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			string testValue = "test";
			string modifiedTestValue = "modified-test";
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			string result = dataClient.getAttributeValue<string>(userId, TestConstants.testSingleStringAttribute);
			if ( null != result ) {
				dataClient.removeAttribute(userId, TestConstants.testSingleStringAttribute, null);
			} 
			dataClient.createAttribute(userId, TestConstants.testSingleStringAttribute, testValue);
			
			dataClient.setAttributeValue(userId, TestConstants.testSingleStringAttribute, modifiedTestValue);

			result = dataClient.getAttributeValue<string>(userId, TestConstants.testSingleStringAttribute);
			Assert.AreEqual(modifiedTestValue, result);
		}

		[Test]
		public void TestModifyDateAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DateTime testValue = DateTime.UtcNow;
			DateTime modifiedTestValue = DateTime.UtcNow.AddDays(1);
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			DateTime result = dataClient.getAttributeValue<DateTime>(userId, TestConstants.testSingleDateAttribute);
			if ( ! result.Equals(DateTime.MinValue) ) {
				dataClient.removeAttribute(userId, TestConstants.testSingleDateAttribute, null);
			} 
			dataClient.createAttribute(userId, TestConstants.testSingleDateAttribute, testValue);
			
			dataClient.setAttributeValue(userId, TestConstants.testSingleDateAttribute, modifiedTestValue);

			result = dataClient.getAttributeValue<DateTime>(userId, TestConstants.testSingleDateAttribute);
			Assert.AreNotEqual(DateTime.MinValue, result);
			Console.WriteLine("result: " + result);
		}

		[Test]
		public void TestModifyMultivaluedStringAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			string[] testValue = new string[]{"test 1", "test 2"};
			string[] modifiedTestValue = new string[]{"modified test 1", "modified test 2"};
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			string[] result = dataClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
			if ( null != result ) {
				dataClient.removeAttribute(userId, TestConstants.testMultiStringAttribute, null);
			} 
			dataClient.createAttribute(userId, TestConstants.testMultiStringAttribute, testValue);
			
			dataClient.setAttributeValue(userId, TestConstants.testMultiStringAttribute, modifiedTestValue);

			result = dataClient.getAttributeValue<string[]>(userId, TestConstants.testMultiStringAttribute);
			Assert.IsNotNull(result);
			Assert.AreEqual(modifiedTestValue, result);
		}

		[Test]
		public void TestModifyMultivaluedDateAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			DateTime[] testValue = new DateTime[]{DateTime.UtcNow, DateTime.UtcNow.AddDays(1)};
			DateTime[] modifiedTestValue = new DateTime[]{DateTime.UtcNow.AddDays(2), DateTime.UtcNow.AddDays(3)};
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			DateTime[] result = dataClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
			if ( null != result ) {
				dataClient.removeAttribute(userId, TestConstants.testMultiDateAttribute, null);
			} 
			dataClient.createAttribute(userId, TestConstants.testMultiDateAttribute, testValue);
			
			dataClient.setAttributeValue(userId, TestConstants.testMultiDateAttribute, modifiedTestValue);

			result = dataClient.getAttributeValue<DateTime[]>(userId, TestConstants.testMultiDateAttribute);
			Assert.IsNotNull(result);
			foreach(DateTime resultDate in result) {
				Console.WriteLine("modified result date: " + resultDate);
			}
		}

		[Test]
		public void TestModifyCompoundAttribute() {
			IdMappingClient idMappingClient = 
				new IdMappingClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			String userId = idMappingClient.getUserId(TestConstants.testLogin);
			Console.WriteLine("admin userId: " + userId);

			Dictionary<string, object> testValue = new Dictionary<string, object>();
			testValue.Add(TestConstants.testMultiStringAttribute, "test 1");
			testValue.Add(TestConstants.testMultiDateAttribute, DateTime.UtcNow);
			Dictionary<string, object> modifiedTestValue = new Dictionary<string, object>();
			modifiedTestValue.Add(TestConstants.testMultiStringAttribute, "modified test 1");
			modifiedTestValue.Add(TestConstants.testMultiDateAttribute, DateTime.UtcNow.AddDays(1));
			DataClient dataClient =
				new DataClientImpl(TestConstants.wsLocation, TestConstants.testPfxPath, TestConstants.testPfxPassword, 
				                        TestConstants.olasCertPath);
			
			Dictionary<string, object>[] result = 
				dataClient.getAttributeValue<Dictionary<string, object>[]>(userId, TestConstants.testCompoundAttribute);
			if ( null != result ) {
				foreach(Dictionary<string, object> resultCompound in result) {
					string compoundId = (string) resultCompound[WebServiceConstants.ATTRIBUTE_ID_KEY];
					dataClient.removeAttribute(userId, TestConstants.testCompoundAttribute, compoundId);
				}
			} 
			dataClient.createAttribute(userId, TestConstants.testCompoundAttribute, testValue);
			result = dataClient.getAttributeValue<Dictionary<string, object>[]>(userId, TestConstants.testCompoundAttribute);
			// set compound Id attribute so OLAS knows which compound to modify ...
			string id = (string) result[0][WebServiceConstants.ATTRIBUTE_ID_KEY];
			modifiedTestValue.Add(WebServiceConstants.ATTRIBUTE_ID_KEY, id);
			dataClient.setAttributeValue(userId, TestConstants.testCompoundAttribute, modifiedTestValue);

			result = dataClient.getAttributeValue<Dictionary<string, object>[]>(userId, TestConstants.testCompoundAttribute);
			Assert.IsNotNull(result);
			foreach(Dictionary<string,object> resultCompound in result) {
				Console.WriteLine("Compound attribute");
				foreach(object key in resultCompound.Keys) {
					Console.WriteLine("compound member " + key + " value=" + resultCompound[key.ToString()]);
				}
			}
		}

	}
}
