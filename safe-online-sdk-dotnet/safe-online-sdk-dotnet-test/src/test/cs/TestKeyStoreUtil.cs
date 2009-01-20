/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using NUnit.Framework;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.X509;
using System.IO;
using System.Security.Cryptography;

using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestKeyStoreUtil
	{
		[Test]
		public void TestCreateCertificate()
		{
			AsymmetricCipherKeyPair keyPair = KeyStoreUtil.GenerateKeyPair();
			RsaPrivateCrtKeyParameters RSAprivKey = (RsaPrivateCrtKeyParameters) keyPair.Private;
			RsaKeyParameters RSApubKey = (RsaKeyParameters) keyPair.Public;
			
			X509Certificate cert = KeyStoreUtil.CreateCert(RSApubKey, RSAprivKey);
			Console.WriteLine(cert.ToString());
			
			string pfxPath = TestConstants.testPfxPath;
			if ( File.Exists(pfxPath)) {
			    pfxPath += "_test";
			    if ( File.Exists(pfxPath) ) {
			    	File.Delete(pfxPath);
			    }
			}
			FileStream fs = new FileStream(pfxPath, FileMode.CreateNew);
			KeyStoreUtil.WritePkcs12(RSAprivKey, cert, TestConstants.testPfxPassword, fs);
			fs.Close();
			
			string crtPath = TestConstants.testCrtPath;
			if ( File.Exists(crtPath)) {
			    crtPath += "_test";
			    if ( File.Exists(crtPath) ) {
			    	File.Delete(crtPath);
			    }
			}			
			FileStream certFileStream = new FileStream(crtPath, FileMode.CreateNew);
			byte[] encodedCert = cert.GetEncoded();
			certFileStream.Write(encodedCert, 0, encodedCert.Length);
			certFileStream.Close();
		}
		
		[Test]
		public void TestLoadKey()
		{
			System.Security.Cryptography.X509Certificates.X509Certificate2 certificate =
				new System.Security.Cryptography.X509Certificates.X509Certificate2(TestConstants.testPfxPath, TestConstants.testPfxPassword);
			RSACryptoServiceProvider key = (RSACryptoServiceProvider) certificate.PrivateKey;
		}
	}
}
