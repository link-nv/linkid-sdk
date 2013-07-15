/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using NotificationSubscriptionManagerWSNameSpace;
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
	/// Client implementation of the linkID WS-Notification Subscription Manager Web Service.
	/// </summary>
	public class NotificationSubscriptionManagerClientImpl : NotificationSubscriptionManagerClient
	{
		private NotificationSubscriptionManagerPortClient client;
		
        public NotificationSubscriptionManagerClientImpl(string location, X509Certificate2 appCertificate, X509Certificate2 linkidCertificate)
        {			
			string address = "https://" + location + "/linkid-ws/subscription";
			EndpointAddress remoteAddress = new EndpointAddress(address);
					
			this.client = new NotificationSubscriptionManagerPortClient(new LinkIDBinding(linkidCertificate), remoteAddress);
			
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = linkidCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			this.client.Endpoint.Behaviors.Add(new LoggingBehavior());
		}
		
		public void unsubscribe(string topic, string address) {
			Console.WriteLine("unsubscribe " + address + " from topic " + topic);
			UnsubscribeRequest request = new UnsubscribeRequest();
			
			EndpointReferenceType endpoint = new EndpointReferenceType();
			endpoint.Address = new AttributedURIType();
			endpoint.Address.Value = address;
			request.ConsumerReference = endpoint;

			TopicType topicType = new TopicType();
			TopicExpressionType topicExpression = new TopicExpressionType();
			topicExpression.Dialect = NotificationServiceConstants.TOPIC_DIALECT_SIMPLE;
			XmlDocument d = new XmlDocument();
			topicExpression.Any = new XmlNode[1];
			topicExpression.Any[0] = d.CreateTextNode(topic);
			topicType.Topic = topicExpression;
			request.Topic = topicType;
			
			UnsubscribeResponse response = this.client.Unsubscribe(request);
			
			checkStatus(response);
		}
		
		private void checkStatus(UnsubscribeResponse response) {
			StatusType status = response.Status;
			if ( status.StatusCode.Value.Equals(NotificationServiceConstants.NOTIFICATION_STATUS_SUCCESS) ) {
			    	return;
			} else if ( status.StatusCode.Value.Equals(NotificationServiceConstants.NOTIFICATION_STATUS_SUBSCRIPTION_NOT_FOUND) ) {
				throw new SubscriptionNotFoundException();
			} else if ( status.StatusCode.Value.Equals(NotificationServiceConstants.NOTIFICATION_STATUS_PERMISSION_DENIED) ) {
				throw new RequestDeniedException();
			}
		}
	}
}
