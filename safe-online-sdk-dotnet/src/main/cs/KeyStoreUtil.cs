using System;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.Math;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Crypto.Generators;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.X509;
using Org.BouncyCastle.Asn1.X509;
using System.Collections;
using System.IO;
using Org.BouncyCastle.Pkcs;

namespace safe_online_sdk_dotnet
{
	public class KeyStoreUtil
	{
		private KeyStoreUtil()
		{
			// empty
		}
		
		public static AsymmetricCipherKeyPair GenerateKeyPair() {
			SecureRandom sr = new SecureRandom();
			BigInteger pubExp = new BigInteger("10001", 16); 
			RsaKeyGenerationParameters RSAKeyGenPara =
               new RsaKeyGenerationParameters(pubExp, sr, 1024, 80);
			RsaKeyPairGenerator RSAKeyPairGen = new RsaKeyPairGenerator();
			RSAKeyPairGen.Init(RSAKeyGenPara);
			AsymmetricCipherKeyPair keyPair = RSAKeyPairGen.GenerateKeyPair();
			return keyPair;
		}
		
		public static X509Certificate CreateCert(
			AsymmetricKeyParameter	pubKey,
			AsymmetricKeyParameter	privKey)
		{
			Hashtable attrs = new Hashtable();
			attrs.Add(X509Name.CN, "Test");

			ArrayList ord = new ArrayList(attrs.Keys);

			X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

			certGen.SetSerialNumber(BigInteger.One);
			certGen.SetIssuerDN(new X509Name(ord, attrs));
			certGen.SetNotBefore(DateTime.UtcNow.AddDays(-30));
			certGen.SetNotAfter(DateTime.UtcNow.AddDays(30));
			certGen.SetSubjectDN(new X509Name(ord, attrs));
			certGen.SetPublicKey(pubKey);
			certGen.SetSignatureAlgorithm("SHA1WithRSAEncryption");

			X509Certificate cert = certGen.Generate(privKey);

			cert.CheckValidity(DateTime.UtcNow);

			cert.Verify(pubKey);

			return cert;
		}
		
		public static void WritePkcs12(RsaPrivateCrtKeyParameters privKey, 
		                               X509Certificate certificate, 
		                               string password, Stream stream) {
			Pkcs12Store store = new Pkcs12Store();
			X509CertificateEntry[] chain = new X509CertificateEntry[1];
			chain[0] = new X509CertificateEntry(certificate);
			store.SetKeyEntry("privateKey", new AsymmetricKeyEntry(privKey), chain);
			store.Save(stream, password.ToCharArray(), new SecureRandom());
		}
	}
}
