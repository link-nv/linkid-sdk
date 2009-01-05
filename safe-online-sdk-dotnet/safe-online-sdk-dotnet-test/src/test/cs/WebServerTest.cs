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
	/// The server will then extract this response and validate it.
	/// This means sending it to the OLAS STS Web Service for validation of the signature and basic validation.
	/// Further validation is done by the utility class Saml2Util.
	/// </summary>
	
	[TestFixture]
	public class WebServerTest
	{
		private TcpListener listener;
		
		private RSACryptoServiceProvider key;
		
		private Saml2Util saml2Util;
		
		[Test]
		public void StartWebServer()
		{
			IPAddress localAddr = IPAddress.Parse(TestConstants.localhost);
			this.listener = new TcpListener(localAddr, 8080);
			this.listener.Start();
			
			System.Security.Cryptography.X509Certificates.X509Certificate2 certificate = 
				new System.Security.Cryptography.X509Certificates.X509Certificate2(TestConstants.testPfxPath, TestConstants.testPfxPassword);
			this.key = (RSACryptoServiceProvider) certificate.PrivateKey;
			
			this.saml2Util = new Saml2Util(this.key);
			
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
					string encodedSamlRequest = this.saml2Util.generateEncodedSamlRequest(TestConstants.testCrtIssuerName,
						TestConstants.testApplicationName, null, "http://" + TestConstants.localhost + ":8080", 
						TestConstants.olasAuthEntry, null, false);
					byte[] msg = Encoding.ASCII.GetBytes("HTTP/1.0 200 OK" + "\r\n" +
						"Content-type: text/html" + "\r\n" +
						"Pragma: no-Cache" + "\r\n" +
						"Cache-Control: no-cache" + "\r\n" +
						"\r\n" +
						"<html>" + "\r\n" + 
						"Hello World" + "\r\n" +
						"<form action=\"" + TestConstants.olasAuthEntry + "\" method=\"POST\">" 
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
						encodedSamlResponse = HttpUtility.UrlDecode(encodedSamlResponse);
						AuthenticationProtocolContext context = 
							this.saml2Util.validateEncodedSamlResponse(encodedSamlResponse, TestConstants.wsLocation,
							                                           TestConstants.testPfxPath, TestConstants.testPfxPassword,
							                                           TestConstants.olasCertPath);
						Console.WriteLine("userId is: \"{0}\"", context.getUserId());
						Console.WriteLine("authentication device is: \"{0}\"", context.getAuthenticatedDevice());
						
						msg = "Successfully authenticated user \"" + context.getUserId().ToString() + "\" using device \"" + context.getAuthenticatedDevice() + "\"";
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
