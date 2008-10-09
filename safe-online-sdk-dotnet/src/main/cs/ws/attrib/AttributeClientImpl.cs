/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Security;
using System.ServiceModel.Security.Tokens;
using System.Xml;
using AttributeWSNamespace;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation for the OLAS Attribute Web Service.
	/// 
	/// This is only a proof-of-concept implementation.
	/// </summary>
	public class AttributeClientImpl : AttributeClient
	{
		private SAMLAttributePortClient client;
		
		public AttributeClientImpl(string location, string testPfxPath, string testPfxPassword, string olasCertPath)
		{
			ServicePointManager.ServerCertificateValidationCallback = 
				new RemoteCertificateValidationCallback(WCFUtil.AnyCertificateValidationCallback);
			string address = "https://" + location + "/safe-online-ws/attrib";
			EndpointAddress remoteAddress = new EndpointAddress(address);
			
			this.client = new SAMLAttributePortClient(new OlasBinding(), remoteAddress);
			
			X509Certificate2 certificate = new X509Certificate2(testPfxPath, testPfxPassword);
			this.client.ClientCredentials.ClientCertificate.Certificate = certificate;

			X509Certificate2 serviceCertificate = new X509Certificate2(olasCertPath);
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = serviceCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;

		}
		
		public string getStringAttributeValue(string userId, string attributeName) {
			Console.WriteLine("get attribute value for subject "  + userId + " attribute name " + attributeName);
			AttributeQueryType request = getAttributeQuery(userId, attributeName);
			ResponseType response = getResponse(request);
			checkStatus(response);
			return getAttributeValue(response);
		}
		
		private AttributeQueryType getAttributeQuery(string subjectLogin, string attributeName) {
			AttributeQueryType attributeQuery = new AttributeQueryType();
			
			SubjectType subject = new SubjectType();
			NameIDType subjectName = new NameIDType();
			subjectName.Value = subjectLogin;
			subject.Items = new Object[] {subjectName};
			attributeQuery.Subject = subject;
			
			AttributeType attribute = new AttributeType();
			attribute.Name = attributeName;
			attributeQuery.Attribute = new AttributeType[] {attribute};
			return attributeQuery;
		}
		
		private ResponseType getResponse(AttributeQueryType request) {
			return this.client.AttributeQuery(request);
		}
		
		private void checkStatus(ResponseType response) {
			StatusType status = response.Status;
			StatusCodeType statusCode = status.StatusCode;
			if ( ! statusCode.Value.Equals("urn:oasis:names:tc:SAML:2.0:status:Success") ) {
				throw new Exception("Attribute query failed: " + statusCode.Value);
			}
		}
		
		private String getAttributeValue(ResponseType response) {
			AssertionType assertion = (AssertionType) response.Items[0];
			AttributeStatementType attributeStatement = (AttributeStatementType) assertion.Items[0];
			AttributeType attribute = (AttributeType) attributeStatement.Items[0];
			return (string) attribute.AttributeValue[0];
		}
	}
}
