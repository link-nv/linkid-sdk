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
using System.Collections;
using System.Collections.Generic;

using safe_online_sdk_dotnet;
using System.Security.Cryptography.X509Certificates;


namespace safe_online_sdk_dotnet_test.test.cs
{
    /// <summary>
    /// Test Web Server.
    /// 
    /// This will show a basic page with a form and one button.
    /// This button will generate a SAML v2.0 authentication request and redirect to the linkID authentication web application.
    /// When the linkID authentication process is finished, it will send back the SAML v2.0 authentication response back.
    /// The server will then extract this response and validate it.
    /// This means sending it to the linkID STS Web Service for validation of the signature and basic validation.
    /// Further validation is done by the utility class Saml2Util.
    /// </summary>

    [TestFixture]
    public class WebServerTest
    {
        private TcpListener listener;

        private RSACryptoServiceProvider key;

        private Saml2AuthUtil saml2AuthUtil;
        private Saml2LogoutUtil saml2LogoutUtil;

        private string userId = null;

        [Test]
        public void StartWebServer()
        {
            IPAddress localAddr = IPAddress.Parse(TestConstants.localhost);
            this.listener = new TcpListener(localAddr, 8080);
            this.listener.Start();

            this.key = KeyStoreUtil.GetPrivateKeyFromPfx(TestConstants.testPfxPath, TestConstants.testPfxPassword, true);
            //this.key = KeyStoreUtil.GetPrivateKeyFromPem(TestConstants.testKeyPath);

            this.saml2AuthUtil = new Saml2AuthUtil(this.key);
            this.saml2LogoutUtil = new Saml2LogoutUtil(this.key);

            Thread thread = new Thread(new ThreadStart(this.StartListen));
            thread.Start();
        }

        void StartListen()
        {
            Console.WriteLine("start listen");
            while (true)
            {
                Socket socket = this.listener.AcceptSocket();
                Console.WriteLine("client {0}", socket.RemoteEndPoint);
                Byte[] bReceive = new Byte[1024 * 10];
                string sBuffer = "";
                int bytes;
                do
                {
                    Console.WriteLine("receiving...");
                    bytes = socket.Receive(bReceive, 0, bReceive.Length, 0);
                    Console.WriteLine("received bytes: {0}", bytes);
                    sBuffer = sBuffer + Encoding.ASCII.GetString(bReceive, 0, bytes);
                    Thread.Sleep(10);
                } while (bytes > 0 && socket.Available > 0);
                Console.WriteLine("received: {0}", sBuffer);

                byte[] response = null;
                if (sBuffer.Substring(0, 3) == "GET")
                {
                    if (null == userId)
                        response = initiateAuthentication();
                    else
                        response = initiateLogout();
                }
                else
                {
                    if (null == userId)
                    {
                        response = finalizeAuthentication(sBuffer);
                    }
                    else
                    {
                        response = finalizeLogout(sBuffer);
                    }
                }
                int result = socket.Send(response);
                Console.WriteLine("result: " + result);
                socket.Close();
            }
        }


        public byte[] initiateAuthentication()
        {
            string encodedSamlRequest = this.saml2AuthUtil.generateEncodedAuthnRequest(TestConstants.testApplicationName,
                    null, null, "http://" + TestConstants.localhost + ":8080",
                    TestConstants.linkidAuthEntry, null, false);
            byte[] response = generateSamlRequestForm("Authenticate", TestConstants.linkidAuthEntry, encodedSamlRequest);
            Console.WriteLine("initiateAuthentication response: {0}", response);
            return response;
        }

        public byte[] initiateLogout()
        {
            string encodedLogoutRequest = this.saml2LogoutUtil.generateEncodedLogoutRequest(this.userId, TestConstants.testApplicationName,
                                                                                            TestConstants.linkidLogoutEntry);

            byte[] response = generateSamlRequestForm("Logout", TestConstants.linkidLogoutEntry, encodedLogoutRequest);
            Console.WriteLine("initiateLogout response: {0}", response);
            return response;
        }

        private byte[] generateSamlRequestForm(String message, String identityProviderUrl, String encodedRequest)
        {
            byte[] response = Encoding.ASCII.GetBytes("HTTP/1.0 200 OK" + "\r\n" +
                            "Content-type: text/html" + "\r\n" +
                            "Pragma: no-Cache" + "\r\n" +
                            "Cache-Control: no-cache" + "\r\n" +
                            "\r\n" +
                            "<html>" + "\r\n" +
                            message + "\r\n" +
                            "<form action=\"" + identityProviderUrl + "\" method=\"POST\">"
                            + "\r\n" + "<input type=\"submit\" value=\"submit\"/>" + "\r\n" +
                            "<input type=\"hidden\" name=\"" + RequestConstants.SAML2_POST_BINDING_REQUEST_PARAM + "\" value=\"" + encodedRequest + "\"/>" + "\r\n" +
                            "</form>" + "\r\n" +
                            "</html>");
            return response;
        }

        public byte[] finalizeAuthentication(string sBuffer)
        {
            string msg = null;

            // Extract encoded SAML v2.0 authentication response and validate using the STS web service
            string encodedSamlResponse = Saml2Util.getParameter(sBuffer, RequestConstants.SAML2_POST_BINDING_RESPONSE_PARAM);
            if (null == encodedSamlResponse)
            {
                msg = "SAML response not present in request data";
            }
            else
            {
                Console.WriteLine("encoded SAML response: \"{0}\"", encodedSamlResponse);
                encodedSamlResponse = HttpUtility.UrlDecode(encodedSamlResponse);

                X509Certificate2 appCertifcate = KeyStoreUtil.loadCertificate(TestConstants.testPfxPath, TestConstants.testPfxPassword, false);
                X509Certificate2 linkidCertificate = new X509Certificate2(TestConstants.linkidCertPath);
                AuthenticationProtocolContext context =
                    this.saml2AuthUtil.validateEncodedAuthnResponse(encodedSamlResponse, TestConstants.wsLocation,
                                                                     appCertifcate, linkidCertificate);
                Console.WriteLine("userId is: \"{0}\"", context.getUserId());
                String devices = "";
                foreach (String device in context.getAuthenticatedDevices())
                {
                    devices += " " + device;
                    Console.WriteLine("authentication device is: \"{0}\"", device);
                }

                msg = "Successfully authenticated user \"" + context.getUserId().ToString() + "\" using device(s) \"" + devices + "\"";

                Dictionary<String, List<AttributeSDK>> attributes = context.getAttributes();
                if (null != attributes)
                {
                    msg += "<p>Attributes:</p>";
                    foreach (String key in attributes.Keys)
                    {
                        msg += "Name : " + key + "<br/>";

                        foreach (AttributeSDK attribute in attributes[key])
                        {
                            msg += "Value: " + attribute.getValue() + "<br/>";
                        }
                    }
                }
                this.userId = context.getUserId();
            }

            return Encoding.ASCII.GetBytes("HTTP/1.0 200 OK" + "\r\n" + "Content-type: text/html" + "\r\n" + "Pragma: no-Cache" + "\r\n" + "Cache-Control: no-cache" + "\r\n" + "\r\n" +
                                            "<html>" + "\r\n" + msg + "\r\n" + "</html>");
        }

        public byte[] finalizeLogout(string sBuffer)
        {
            string msg = null;

            // Extract encoded SAML v2.0 logout response and validate using the STS web service
            string encodedLogoutResponse = Saml2Util.getParameter(sBuffer, RequestConstants.SAML2_POST_BINDING_RESPONSE_PARAM);
            if (null == encodedLogoutResponse)
            {
                msg = "SAML logout response not present in request data";
            }
            else
            {
                X509Certificate2 appCertifcate = KeyStoreUtil.loadCertificate(TestConstants.testPfxPath, TestConstants.testPfxPassword, false);
                X509Certificate2 linkidCertificate = new X509Certificate2(TestConstants.linkidCertPath);

                Console.WriteLine("encoded SAML logout response: \"{0}\"", encodedLogoutResponse);
                encodedLogoutResponse = HttpUtility.UrlDecode(encodedLogoutResponse);
                bool result = this.saml2LogoutUtil.validateEncodedLogoutResponse(encodedLogoutResponse, TestConstants.wsLocation,
                                                                                  appCertifcate, linkidCertificate);
                if (true == result)
                {
                    msg = "Logged out";
                    this.userId = null;
                }
                else
                {
                    msg = "Logout failed";
                }
            }

            return Encoding.ASCII.GetBytes("HTTP/1.0 200 OK" + "\r\n" + "Content-type: text/html" + "\r\n" + "Pragma: no-Cache" + "\r\n" + "Cache-Control: no-cache" + "\r\n" + "\r\n" +
                                            "<html>" + "\r\n" + msg + "\r\n" + "</html>");
        }

    }
}
