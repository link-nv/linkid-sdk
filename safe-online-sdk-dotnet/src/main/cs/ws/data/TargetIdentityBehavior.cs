/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.Xml;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// This endpoint behaviour will add the TargetIdentity SOAP header.
	/// </summary>
	public class TargetIdentityBehavior : IEndpointBehavior {
		
		private string userId;
		
		public TargetIdentityBehavior(string userId) {
			this.userId = userId;
		}
	
	
		public void Validate(ServiceEndpoint endpoint)
		{
		}
		
		public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
		{
		}
		
		public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
		{
			clientRuntime.MessageInspectors.Add(new TargetIdentityClientMessageInspector(this.userId));
		}
		
		public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
		{
			throw new NotImplementedException();
		}

	/// <summary>
	/// TargetIdentityClientMessageInspector
	/// 
	/// This is the client inspector class that is used to intercept the message on the client side.
	/// Before a request is made to the server, the TargetIdentity header will be added.
	/// This is required by the OLAS Data Web Service to communicate the OLAS user ID.
	/// </summary>
	public class TargetIdentityClientMessageInspector : IClientMessageInspector
	{
		private string userId;
		
		public TargetIdentityClientMessageInspector(string userId) {
			this.userId = userId;
		}
	
		public object BeforeSendRequest(ref Message request, IClientChannel channel)
		{
			// Prepare the request message copy to be modified
			MessageBuffer buffer = request.CreateBufferedCopy(Int32.MaxValue);
			request = buffer.CreateMessage();
			
			TargetIdentityHeader header = new TargetIdentityHeader(this.userId);
			
			// Add the header to the request
			request.Headers.Add(header);
			
			return null;
		}
		
		public void AfterReceiveReply(ref Message reply, object correlationState)
		{
			
		}
	}

	/// <summary>
	/// Custom TargetIdentity SOAP header. The OLAS subject is wrapped in a SAML subject.
	/// </summary>
	public class TargetIdentityHeader : MessageHeader
    {
        private string targetIdentity;
        
        public TargetIdentityHeader(String targetIdentity)
        {
            this.targetIdentity = targetIdentity;
        }

        public override string Name
        {
            get { return (WCFUtil.TARGET_IDENTITY_HEADER_NAME); }
        }

        public override string Namespace
        {
            get { return (WCFUtil.TARGET_IDENTITY_HEADER_NAMESPACE); }
        }
        
        public override bool MustUnderstand
        {
        	get { return true; }
        }
        
        protected override void OnWriteHeaderContents(XmlDictionaryWriter writer, MessageVersion messageVersion)
        {
        	writer.WriteStartElement("Subject", WCFUtil.SAML_ASSERTION_NAMESPACE);
        	writer.WriteElementString("NameID", WCFUtil.SAML_ASSERTION_NAMESPACE, targetIdentity);
        	writer.WriteEndElement();
        }
    } 
}
}
