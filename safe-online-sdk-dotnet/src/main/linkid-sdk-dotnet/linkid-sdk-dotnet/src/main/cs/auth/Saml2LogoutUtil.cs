/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Security.Cryptography;
using System.Security.Cryptography.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Xml;
using System.Text;
using System.Collections;
using System.Collections.Generic;

using IdMappingWSNamespace;
using System.Security.Cryptography.X509Certificates;

namespace safe_online_sdk_dotnet
{
    /// <summary>
    /// Saml2AuthUtil
    /// 
    /// This utility class generates a SAML v2.0 AuthenticationRequest with HTTP Browser Post binding
    /// and validates the returned SAML v2.0 Response.
    /// </summary>
    public class Saml2LogoutUtil
    {
        private readonly RSACryptoServiceProvider key;

        private string expectedChallenge;

        public Saml2LogoutUtil(RSACryptoServiceProvider key)
        {
            this.key = key;
        }

        public string getChallenge()
        {
            return this.expectedChallenge;
        }

        /// <summary>
        /// Generates a SAML v2.0 Logout Request to LinkID for the specified subject.
        /// </summary>
        /// <param name="userId">The linkID user ID</param>
        /// <param name="applicationName"></param>
        /// <param name="identityProviderUrl"></param>
        /// <returns>Base64 encoded SAML v2.0 Logout Request</returns>
        public string generateEncodedLogoutRequest(string userId, string applicationName, string identityProviderUrl)
        {

            string logoutRequest = generateLogoutRequest(userId, applicationName, identityProviderUrl);
            return Convert.ToBase64String(Encoding.ASCII.GetBytes(logoutRequest));
        }

        /// <summary>
        /// Generates a SAML v2.0 Logout Request to LinkID for the specified subject. The return string containing the request
        /// is NOT Base64 encoded.
        /// </summary>
        /// <param name="userId">The linkID user ID</param>
        /// <param name="applicationName"></param>
        /// <param name="identityProviderUrl"></param>
        /// <returns>Plain SAML v2.0 Logout Request</returns>
        public string generateLogoutRequest(string userId, string applicationName, string identityProviderUrl)
        {

            this.expectedChallenge = Guid.NewGuid().ToString();

            LogoutRequestType logoutRequest = new LogoutRequestType();
            logoutRequest.ID = this.expectedChallenge;
            logoutRequest.Version = "2.0";
            logoutRequest.IssueInstant = DateTime.UtcNow;

            NameIDType issuer = new NameIDType();
            issuer.Value = applicationName;
            logoutRequest.Issuer = issuer;

            logoutRequest.Destination = identityProviderUrl;

            NameIDType nameID = new NameIDType();
            nameID.Value = userId;
            nameID.Format = Saml2Constants.SAML2_NAMEID_FORMAT_ENTITY;
            logoutRequest.Item = nameID;

            XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
            ns.Add("samlp", Saml2Constants.SAML2_PROTOCOL_NAMESPACE);
            ns.Add("saml", Saml2Constants.SAML2_ASSERTION_NAMESPACE);

            XmlRootAttribute xRoot = new XmlRootAttribute();
            xRoot.ElementName = "LogoutRequest";
            xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;
            XmlSerializer serializer = new XmlSerializer(typeof(LogoutRequestType), xRoot);
            MemoryStream memoryStream = new MemoryStream();
            XmlTextWriter xmlTextWriter = new XmlTextWriter(memoryStream, Encoding.UTF8);
            serializer.Serialize(xmlTextWriter, logoutRequest, ns);

            XmlDocument document = new XmlDocument();
            memoryStream.Seek(0, SeekOrigin.Begin);
            document.Load(memoryStream);

            string signedLogoutRequest = Saml2Util.signDocument(document, key, logoutRequest.ID);
            xmlTextWriter.Close();

            return signedLogoutRequest;
        }

        /// <summary>
        /// Validates returned SAML v2.0 Logout Response from LinkID. Return true or false upon success or not.
        /// </summary>
        /// <param name="encodedLogoutResponse"></param>
        /// <param name="wsLocation"></param>
        /// <param name="appCertificate"></param>
        /// <param name="linkidCertificate"></param>
        /// <returns>True or false upon success or not</returns>
        public bool validateEncodedLogoutResponse(string encodedLogoutResponse, string wsLocation,
                                                                         X509Certificate2 appCertificate,
                                                                         X509Certificate2 linkidCertificate)
        {

            byte[] logoutResponseData = Convert.FromBase64String(encodedLogoutResponse);
            string logoutResponse = Encoding.UTF8.GetString(logoutResponseData);
            return validateLogoutResponse(logoutResponse, wsLocation, appCertificate, linkidCertificate);
        }

        /// <summary>
        /// Validates returned SAML v2.0 Logout Response from LinkID. Return true or false upon success or not.
        /// </summary>
        /// <param name="logoutResponse"></param>
        /// <param name="wsLocation"></param>
        /// <param name="appCertificate"></param>
        /// <param name="linkidCertificate"></param>
        /// <returns>True or false upon success or not</returns>
        public bool validateLogoutResponse(string logoutResponse, string wsLocation,
                                                                   X509Certificate2 appCertificate,
                                                                         X509Certificate2 linkidCertificate)
        {
            DateTime now = DateTime.UtcNow;

            STSClient stsClient = new STSClientImpl(wsLocation, appCertificate, linkidCertificate);
            bool result = stsClient.validateToken(logoutResponse, TrustDomainType.NODE);
            if (false == result)
            {
                return false;
            }

            XmlRootAttribute xRoot = new XmlRootAttribute();
            xRoot.ElementName = "LogoutResponse";
            xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;

            TextReader reader = new StringReader(logoutResponse);
            XmlSerializer serializer = new XmlSerializer(typeof(StatusResponseType), xRoot);
            StatusResponseType response = (StatusResponseType)serializer.Deserialize(reader);
            reader.Close();

            if (!response.InResponseTo.Equals(this.expectedChallenge))
            {
                throw new AuthenticationException("SAML logout response is not a response belonging to the original request.");
            }

            if (response.Status.StatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_SUCCESS))
            {
                return true;
            }
            Console.WriteLine("Status: " + response.Status.StatusCode.Value);

            return false;
        }

        /// <summary>
        /// Validates incoming SAML v2.0 Logout Request from LinkID. This request will have been sent due to a logout request
        /// sent by another application in the same Single Sign On Application Pool as this application.
        /// </summary>
        /// <param name="encodedLogoutRequest"></param>
        /// <param name="wsLocation"></param>
        /// <param name="appCertificate"></param>
        /// <param name="linkidCertificate"></param>
        /// <returns>The subject to logout</returns>
        public string validateEncodedLogoutRequest(string encodedLogoutRequest, string wsLocation,
                                                                         X509Certificate2 appCertificate,
                                                                         X509Certificate2 linkidCertificate)
        {

            byte[] logoutRequestData = Convert.FromBase64String(encodedLogoutRequest);
            string logoutRequest = Encoding.UTF8.GetString(logoutRequestData);
            return validateLogoutRequest(logoutRequest, wsLocation, appCertificate, linkidCertificate);
        }

        /// <summary>
        /// Validates incoming SAML v2.0 Logout Request from LinkID. This request will have been sent due to a logout request
        /// sent by another application in the same Single Sign On Application Pool as this application.
        /// </summary>
        /// <param name="logoutRequest"></param>
        /// <param name="wsLocation"></param>
        /// <param name="appCertificate"></param>
        /// <param name="linkidCertificate"></param>
        /// <returns>The subject to logout</returns>
        public string validateLogoutRequest(string logoutRequest, string wsLocation,
                                                                  X509Certificate2 appCertificate,
                                                                  X509Certificate2 linkidCertificate)
        {
            DateTime now = DateTime.UtcNow;

            STSClient stsClient = new STSClientImpl(wsLocation, appCertificate, linkidCertificate);
            bool result = stsClient.validateToken(logoutRequest, TrustDomainType.NODE);
            if (false == result)
            {
                return null;
            }

            XmlRootAttribute xRoot = new XmlRootAttribute();
            xRoot.ElementName = "LogoutRequest";
            xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;

            TextReader reader = new StringReader(logoutRequest);
            XmlSerializer serializer = new XmlSerializer(typeof(LogoutRequestType), xRoot);
            LogoutRequestType request = (LogoutRequestType)serializer.Deserialize(reader);
            reader.Close();

            this.expectedChallenge = request.ID;
            Console.WriteLine("Challenge: " + this.expectedChallenge);

            string userId = ((NameIDType)request.Item).Value;
            Console.WriteLine("Logout user : " + userId);
            return userId;
        }

        /// <summary>
        /// Generates a SAML v2.0 Logout Response to return to LinkID.
        /// </summary>
        /// <param name="applicationName"></param>
        /// <param name="identityProviderUrl"></param>
        /// <returns>Base64 encoded SAML v2.0 Logout Response</returns>
        public string generateEncodedLogoutResponse(bool success, string applicationName, string identityProviderUrl)
        {

            string logoutResponse = generateLogoutResponse(success, applicationName, identityProviderUrl);
            return Convert.ToBase64String(Encoding.ASCII.GetBytes(logoutResponse));
        }

        /// <summary>
        /// Generates a SAML v2.0 Logout Response to return to LinkID. The return string containing the response
        /// is NOT Base64 encoded.
        /// </summary>
        /// <param name="applicationName"></param>
        /// <param name="identityProviderUrl"></param>
        /// <returns>Plain SAML v2.0 Logout Response</returns>
        public string generateLogoutResponse(bool success, string applicationName, string identityProviderUrl)
        {

            StatusResponseType logoutResponse = new StatusResponseType();
            logoutResponse.ID = this.expectedChallenge;
            logoutResponse.Version = "2.0";
            logoutResponse.IssueInstant = DateTime.UtcNow;

            NameIDType issuer = new NameIDType();
            issuer.Value = applicationName;
            logoutResponse.Issuer = issuer;

            logoutResponse.Destination = identityProviderUrl;

            StatusType status = new StatusType();
            status.StatusCode = new StatusCodeType();
            if (success)
                status.StatusCode.Value = Saml2Constants.SAML2_STATUS_SUCCESS;
            else
                status.StatusCode.Value = Saml2Constants.SAML2_STATUS_PARTIAL_LOGOUT;
            logoutResponse.Status = status;

            XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
            ns.Add("samlp", Saml2Constants.SAML2_PROTOCOL_NAMESPACE);
            ns.Add("saml", Saml2Constants.SAML2_ASSERTION_NAMESPACE);

            XmlRootAttribute xRoot = new XmlRootAttribute();
            xRoot.ElementName = "LogoutResponse";
            xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;
            XmlSerializer serializer = new XmlSerializer(typeof(StatusResponseType), xRoot);
            MemoryStream memoryStream = new MemoryStream();
            XmlTextWriter xmlTextWriter = new XmlTextWriter(memoryStream, Encoding.UTF8);
            serializer.Serialize(xmlTextWriter, logoutResponse, ns);

            XmlDocument document = new XmlDocument();
            memoryStream.Seek(0, SeekOrigin.Begin);
            document.Load(memoryStream);

            string signedLogoutResponse = Saml2Util.signDocument(document, key, logoutResponse.ID);
            xmlTextWriter.Close();

            return signedLogoutResponse;
        }

    }
}
