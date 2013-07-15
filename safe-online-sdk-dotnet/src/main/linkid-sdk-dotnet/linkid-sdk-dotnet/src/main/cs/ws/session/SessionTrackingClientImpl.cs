/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 6/04/2009
 * Time: 16:10
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */

using SessionWSNameSpace;
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
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the Session Tracking WS
	/// </summary>
	public class SessionTrackingClientImpl : SessionTrackingClient
	{
		public static readonly string SUCCESS = "urn:net:lin-k:safe-online:ws:session:status:success";
		public static readonly string APPLICATION_POOL_NOT_FOUND = "urn:net:lin-k:safe-online:ws:session:status:ApplicationNotFound";
		public static readonly string SUBJECT_NOT_FOUND = "urn:net:lin-k:safe-online:ws:session:status:SubjectNotFound";
		
		private SessionTrackingPortClient client;

        public SessionTrackingClientImpl(string location, X509Certificate2 appCertificate, X509Certificate2 linkidCertificate)
		{
			string address = "https://" + location + "/linkid-ws/session";
			EndpointAddress remoteAddress = new EndpointAddress(address);
					
			this.client = new SessionTrackingPortClient(new LinkIDBinding(linkidCertificate), remoteAddress);
			
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = linkidCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			this.client.Endpoint.Behaviors.Add(new LoggingBehavior());
		}
		
		public List<SessionAssertion> getAssertions(string session, string subject, 
		                                            List<string> applicationPools) {
			Console.WriteLine("get assertions: session=" + session + " subject=" + subject);
			
			SessionTrackingRequestType request = new SessionTrackingRequestType();
			request.Session = session;
			request.Subject = subject;
			if ( null != applicationPools && applicationPools.Count > 0) {
				List<ApplicationPoolType> applicationPoolTypes = new List<ApplicationPoolType>();
				foreach(string applicationPool in applicationPools) {
					Console.WriteLine("application pool: " + applicationPool);
					ApplicationPoolType applicationPoolType = new ApplicationPoolType();
					applicationPoolType.Name = applicationPool;
					applicationPoolTypes.Add(applicationPoolType);
				}
				request.ApplicationPools = applicationPoolTypes.ToArray();
			}

			SessionTrackingResponseType response = client.getAssertions(request);
			
			checkStatus(response);
			
			return getAssertions(response);
		}
		
		private List<SessionAssertion> getAssertions(SessionTrackingResponseType response) {
			List<SessionAssertion> assertions = new List<SessionAssertion>();
			foreach(AssertionType assertionType in response.Assertions) {
				assertions.Add(new SessionAssertion(assertionType));
			}
			return assertions;
		}
		
		private void checkStatus(SessionTrackingResponseType response) {
			if ( response.Status.Value.Equals(SUCCESS) ) {
				return;
			} else if (response.Status.Value.Equals(SUBJECT_NOT_FOUND) ) {
				throw new SubjectNotFoundException();
			} else if (response.Status.Value.Equals(APPLICATION_POOL_NOT_FOUND) ) {
				throw new ApplicationPoolNotFoundException();
			}
		}
	}
}
