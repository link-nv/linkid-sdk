using System;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.X509;
using System.IO;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class KeyStoreUtilTest
	{
		[Test]
		public void TestMethod()
		{
			AsymmetricCipherKeyPair keyPair = KeyStoreUtil.GenerateKeyPair();
			RsaPrivateCrtKeyParameters RSAprivKey = (RsaPrivateCrtKeyParameters) keyPair.Private;
			RsaKeyParameters RSApubKey = (RsaKeyParameters) keyPair.Public;
			
			X509Certificate cert = KeyStoreUtil.CreateCert(RSApubKey, RSAprivKey);
			Console.WriteLine(cert.ToString());
			
			FileStream fs = new FileStream("C:\\work\\test.pfx", FileMode.CreateNew);
			KeyStoreUtil.WritePkcs12(RSAprivKey, cert, "secret", fs);
			fs.Close();
			
			FileStream certFileStream = new FileStream("C:\\work\\test.crt", FileMode.CreateNew);
			byte[] encodedCert = cert.GetEncoded();
			certFileStream.Write(encodedCert, 0, encodedCert.Length);
			certFileStream.Close();
		}
	}
}
