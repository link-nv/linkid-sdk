/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using PaymentWSNameSpace;
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

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the linkID ID Mapping Web Service.
	/// </summary>
	public class PaymentClientImpl : PaymentClient
	{
		private PaymentServicePortClient client;

        public PaymentClientImpl(string location, X509Certificate2 appCertificate, X509Certificate2 linkidCertificate)
		{			
			string address = "https://" + location + "/linkid-ws/payment";
			EndpointAddress remoteAddress = new EndpointAddress(address);

            this.client = new PaymentServicePortClient(new LinkIDBinding(linkidCertificate), remoteAddress);
			
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = linkidCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			this.client.Endpoint.Behaviors.Add(new LoggingBehavior());
		}
		
		public PaymentState getStatus(String transactionId) {

            PaymentStatusRequest request = new PaymentStatusRequest();
            request.transactionId = transactionId;
			PaymentStatusResponse response = this.client.status(request);

            switch (response.paymentStatus)
            {
                case PaymentStatusType.STARTED:
                    return PaymentState.STARTED;
                case PaymentStatusType.AUTHORIZED:
                    return PaymentState.PAYED;
                case PaymentStatusType.FAILED:
                    return PaymentState.FAILED;
            }

            throw new RuntimeException("Payment state type " + response.paymentStatus + "is not supported!");
		}
	}
}
