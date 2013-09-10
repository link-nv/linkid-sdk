/*
 * Created by SharpDevelop.
 * User: Tester
 * Date: 21-10-2009
 * Time: 10:49
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Security.Cryptography;
using System.Security.Cryptography.Xml;
using System.Xml;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Utility class for SAML 2.
	/// </summary>
	public class Saml2Util
	{
		/// <summary>
		/// Returns specified paramName in the specified query string.
		/// </summary>
		/// <param name="stringBuffer"></param>
		/// <param name="paramName"></param>
		/// <returns></returns>
		public static string getParameter(string stringBuffer, string paramName) {
		
			if ( -1 == stringBuffer.IndexOf(paramName) ) {
				return null;
			}
			
			int beginIdx = stringBuffer.IndexOf(paramName) + paramName.Length + 1;
			int endIdx = stringBuffer.IndexOfAny(new char[] {'\r', '\n', '&'}, beginIdx);
			if ( -1 == endIdx ) return stringBuffer.Substring(beginIdx);
			else				return stringBuffer.Substring(beginIdx, endIdx - beginIdx);
		}

		public static string signDocument(XmlDocument document, RSACryptoServiceProvider key, String id) {
			SignedXml signedXml = new SignedXml(document);
      		signedXml.SigningKey = key;
      		Signature signature = signedXml.Signature;
      		signature.SignedInfo.CanonicalizationMethod = SignedXml.XmlDsigExcC14NTransformUrl;
      		Reference reference = new Reference("#" + id);
      		XmlDsigEnvelopedSignatureTransform env = new XmlDsigEnvelopedSignatureTransform();
        	reference.AddTransform(env);
        	XmlDsigExcC14NTransform excC14NTransform = new XmlDsigExcC14NTransform("ds saml samlp");
        	reference.AddTransform(excC14NTransform);
        	signature.SignedInfo.AddReference(reference);
        	
        	signedXml.ComputeSignature();

        	XmlElement xmlDigitalSignature = signedXml.GetXml();
        	document.DocumentElement.AppendChild(document.ImportNode(xmlDigitalSignature, true));

        	string result = document.OuterXml;
        	return result;
		}
		
		private Saml2Util()
		{
		}
	}
}
