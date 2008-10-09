/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using NUnit.Framework;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Text;
using System.Security.Cryptography;
using System.Web;

using safe_online_sdk_dotnet;


namespace safe_online_sdk_dotnet_test.test.cs
{
	/// <summary>
	/// Test Web Server.
	/// 
	/// This will show a basic page with a form and one button.
	/// This button will generate a SAML v2.0 authentication request and redirect to the OLAS authentication web application.
	/// When the OLAS authentication process is finished, it will send back the SAML v2.0 authentication response back.
	/// The server will then extract this response and send it to the OLAS STS Web Service for validation.
	/// </summary>
	
	[TestFixture]
	public class WebServerTest
	{
		private TcpListener listener;
		
		private RSACryptoServiceProvider key;
		
		private SamlRequestGenerator samlRequestGenerator;
		
		[Test]
		public void StartWebServer()
		{
			IPAddress localAddr = IPAddress.Parse(Constants.localhost);
			this.listener = new TcpListener(localAddr, 8080);
			this.listener.Start();
			
			System.Security.Cryptography.X509Certificates.X509Certificate2 certificate = new System.Security.Cryptography.X509Certificates.X509Certificate2(Constants.testPfxPath, Constants.testPfxPassword);
			this.key = (RSACryptoServiceProvider) certificate.PrivateKey;
			
			this.samlRequestGenerator = new SamlRequestGenerator(this.key);
			
			Thread thread = new Thread(new ThreadStart(this.StartListen));
			thread.Start();
		}
		
		void StartListen() {
			Console.WriteLine("start listen");
			while (true) {
				Socket socket = this.listener.AcceptSocket();
				Console.WriteLine("client {0}", socket.RemoteEndPoint);
				Byte[] bReceive = new Byte[1024 * 10] ;
				string sBuffer = "";
				int bytes;
				do {
					Console.WriteLine("receiving...");
					bytes = socket.Receive(bReceive, 0, bReceive.Length, 0);
					Console.WriteLine("received bytes: {0}", bytes);
					sBuffer = sBuffer + Encoding.ASCII.GetString(bReceive, 0, bytes);
					Thread.Sleep(10);
				} while (bytes > 0 && socket.Available > 0);
				Console.WriteLine("received: {0}", sBuffer);
				if (sBuffer.Substring(0,3) == "GET" ) {
					string samlRequest = this.samlRequestGenerator.generateSamlRequest("token-id", "http://" 
						+ Constants.localhost + ":8080", "https://" + Constants.wsLocation + "/olas-auth/entry", "test");
					string encodedSamlRequest = Convert.ToBase64String(Encoding.ASCII.GetBytes(samlRequest));
					byte[] msg = Encoding.ASCII.GetBytes("HTTP/1.0 200 OK" + "\r\n" +
						"Content-type: text/html" + "\r\n" +
						"Pragma: no-Cache" + "\r\n" +
						"Cache-Control: no-cache" + "\r\n" +
						"\r\n" +
						"<html>" + "\r\n" + 
						"Hello World" + "\r\n" +
						"<form action=\"http://" + Constants.olasHost + ":8080/olas-auth/entry\" method=\"POST\">" 
						+ "\r\n" + "<input type=\"submit\" value=\"submit\"/>" + "\r\n" +
						"<input type=\"hidden\" name=\"SAMLRequest\" value=\"" + encodedSamlRequest + "\"/>" + "\r\n" +
						"</form>" + "\r\n" +
						"</html>");
					int result = socket.Send(msg);
					Console.WriteLine("result: " + result);
				} else {
					int samlResponseIdx = sBuffer.IndexOf("SAMLResponse");
					string msg;
					if (-1 == samlResponseIdx) {
						msg = "SAML response not present in request data";
					} else {
						int samlResponseBeginIdx = samlResponseIdx + "SAMLResponse=".Length;
						int samlResponseEndIdx = sBuffer.IndexOfAny(new char[] {'\r', '\n'}, samlResponseBeginIdx);
						Console.WriteLine("SAML response end idx: {0}", samlResponseEndIdx);
						string encodedSamlResponse;
						if ( -1 == samlResponseEndIdx)
							encodedSamlResponse = sBuffer.Substring(samlResponseBeginIdx);
						else
							encodedSamlResponse = sBuffer.Substring(samlResponseBeginIdx, samlResponseEndIdx - samlResponseBeginIdx);
						Console.WriteLine("encoded saml response is: \"{0}\"", encodedSamlResponse);
						msg = "Authentication successful";
						encodedSamlResponse = HttpUtility.UrlDecode(encodedSamlResponse);
						byte[] samlResponseData = Convert.FromBase64String(encodedSamlResponse);
						string samlResponse = Encoding.UTF8.GetString(samlResponseData);
						Console.WriteLine("SAML Response: {0}", samlResponse);
						
						STSClient stsClient = new STSClientImpl(Constants.wsLocation, Constants.testPfxPath, Constants.testPfxPassword, Constants.olasCertPath);
						stsClient.validateAuthnResponse(samlResponse, TrustDomainType.NODE);
					}
					byte[] httpResponse = Encoding.ASCII.GetBytes("HTTP/1.0 200 OK" + "\r\n" +
						"Content-type: text/html" + "\r\n" +
						"Pragma: no-Cache" + "\r\n" +
						"Cache-Control: no-cache" + "\r\n" +
						"\r\n" +
						"<html>" + "\r\n" + 
						msg + "\r\n" +
						"</html>");
					socket.Send(httpResponse);
				}
				socket.Close();
			}
		}
	}
}
