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
using DataWSNamespace;


namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the OLAS Data Web Service.
	/// 
	/// This is only a proof-of-concept implementation.
	/// </summary>
	public class DataClientImpl : DataClient
	{
		private DataServicePortClient client;
		
		public DataClientImpl(string location, string testPfxPath, string testPfxPassword, string olasCrtPath)
		{
			ServicePointManager.ServerCertificateValidationCallback = 
				new RemoteCertificateValidationCallback(WCFUtil.AnyCertificateValidationCallback);
			string address = "https://" + location + "/safe-online-ws/data";
			EndpointAddress remoteAddress = new EndpointAddress(address);
			
			this.client = new DataServicePortClient(new OlasBinding(), remoteAddress);
			
			X509Certificate2 certificate = new X509Certificate2(testPfxPath, testPfxPassword);
			this.client.ClientCredentials.ClientCertificate.Certificate = certificate;
			
			X509Certificate2 serviceCertificate = new X509Certificate2(olasCrtPath);
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = serviceCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			this.client.Endpoint.Contract.Behaviors.Add(new SignTargetIdentityBehavior());
			this.client.Endpoint.Behaviors.Add(new LoggingBehavior());
		}
		
		public string getStringAttributeValue(string userId, string attributeName) {
			// Add TargetIdentity SOAP header
			this.client.Endpoint.Behaviors.Add(new TargetIdentityBehavior(userId));

			// Create Attribute Query
			QueryType query = new QueryType();
			QueryItemType queryItem = new QueryItemType();
			query.QueryItem = new QueryItemType[] {queryItem};
			
			queryItem.objectType = "Attribute";
			SelectType select = new SelectType();
			select.Value = attributeName;
			queryItem.Select = select;
			
			// Do request
			QueryResponseType response = this.client.Query(query);
			
			// Check response status
			StatusType status = response.Status;
			Console.WriteLine("status: " + status.code);
			if ( status.code.Equals("Failed")) {
				Console.WriteLine("Query failed: " + status.comment);
				return null;
			}
			
			// Get attribute value
			DataType data = response.Data[0];
			AttributeType attribute = data.Attribute;
			if ( null == attribute ) {
				// This happens when the attribute entity does not exist.
				return null;
			}
			return (string) attribute.AttributeValue[0];
		}
	}
}
