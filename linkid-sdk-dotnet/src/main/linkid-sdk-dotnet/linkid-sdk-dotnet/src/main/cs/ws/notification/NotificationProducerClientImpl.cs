/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using NotificationProducerWSNameSpace;
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

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the linkID WS-Notification Producer Web Service.
	/// </summary>
	public class NotificationProducerClientImpl : NotificationProducerClient
	{
		private NotificationProducerPortClient client;
		
        public NotificationProducerClientImpl(string location, X509Certificate2 appCertificate, X509Certificate2 linkidCertificate)
		{
			string address = "https://" + location + "/linkid-ws/producer";
			EndpointAddress remoteAddress = new EndpointAddress(address);
					
			this.client = new NotificationProducerPortClient(new LinkIDBinding(linkidCertificate), remoteAddress);
			
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = linkidCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			this.client.Endpoint.Behaviors.Add(new LoggingBehavior());
		}
		
		public void subscribe(string topic, string address) {
			Console.WriteLine("subscribe " + address + " to " + topic);
			SubscribeRequest request = new SubscribeRequest();
			
			EndpointReferenceType endpoint = new EndpointReferenceType();
			endpoint.Address = new AttributedURIType();
			endpoint.Address.Value = address;
			request.ConsumerReference = endpoint;
			
			FilterType filter = new FilterType();
			TopicExpressionType topicExpression = new TopicExpressionType();
			topicExpression.Dialect = NotificationServiceConstants.TOPIC_DIALECT_SIMPLE;
			XmlDocument d = new XmlDocument();
			topicExpression.Any = new XmlNode[1];
			topicExpression.Any[0] = d.CreateTextNode(topic);
			filter.Topic = topicExpression;
			request.Filter = filter;
			
			SubscribeResponse response = this.client.Subscribe(request);
			
			checkStatus(response);
		}
		
		private void checkStatus(SubscribeResponse response) {
			if ( response.SubscribeStatus.StatusCode.Equals(NotificationServiceConstants.NOTIFICATION_STATUS_SUCCESS) ) {
			    	return;
			} else if ( response.SubscribeStatus.StatusCode.Equals(NotificationServiceConstants.NOTIFICATION_STATUS_SUBSCRIPTION_FAILED) ) {
				throw new SubscriptionFailedException();
			} else {
				throw new RuntimeException("Unknown error: " + response.SubscribeStatus.StatusCode);
			}
		}
	}
}
