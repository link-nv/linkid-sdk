/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Collections.Generic;
using System.Security.Cryptography.X509Certificates;
using NUnit.Framework;
using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet.test.cs
{
    [TestFixture]
    public class TestDataClient
    {
        [SetUp]
        public void Init()
        {
            TestConstants.initForDevelopment();
        }

        [Test]
        public void TestDataWS()
        {
            String attributeName = "guido.data.test.attribute";

            X509Certificate2 appCertificate = KeyStoreUtil.loadCertificate(TestConstants.testPfxPath, TestConstants.testPfxPassword, false);
            X509Certificate2 linkidCertificate = new X509Certificate2(TestConstants.linkidCertPath);

            // first fetch userId
            IdMappingClient idMappingClient =
                new IdMappingClientImpl(TestConstants.wsLocation, appCertificate, linkidCertificate);
            String userId = idMappingClient.getUserId(TestConstants.loginAttribute, TestConstants.testLogin);
            Console.WriteLine("admin userId: " + userId);

            DataClient dataClient = new DataClientImpl(TestConstants.wsLocation, appCertificate, linkidCertificate);

            // Remove old attribute if any
            dataClient.removeAttributes(userId, attributeName); 

            // Create
            AttributeSDK attributeSDK = new AttributeSDK(null, attributeName, true);
            dataClient.createAttribute(userId, attributeSDK);

            // Get
            List<AttributeSDK> attributes = dataClient.getAttributes(userId, attributeName);
            Assert.AreEqual(1, attributes.Count);
            Assert.AreEqual(true, (Boolean)attributes[0].getValue());

            // Set
            attributeSDK.setValue(false);
            dataClient.setAttributeValue(userId, attributeSDK);

            // Get
            attributes = dataClient.getAttributes(userId, attributeName);
            Assert.AreEqual(1, attributes.Count);
            Assert.AreEqual(false, (Boolean)attributes[0].getValue());

            // Delete
            dataClient.removeAttributes(userId, attributeName);

            // Get
            attributes = dataClient.getAttributes(userId, attributeName);
            Assert.AreEqual(1, attributes.Count);
        }
    }
}
