/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Net.Security;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Security;
using System.Xml;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// SignTargetIdentityBehavior
	/// 
	/// This custom behavior class is used to add the TargetIdentity SOAP header to the list of to be signed elements.
	/// 
	public class SignTargetIdentityBehavior : Attribute, IContractBehavior {
		
		public void Validate(ContractDescription contractDescription, 
		                     ServiceEndpoint endpoint)
		{
		}
		
		public void ApplyDispatchBehavior(ContractDescription contractDescription, 
		                                  ServiceEndpoint endpoint, 
		                                  DispatchRuntime dispatchRuntime)
		{
		}
		
		public void ApplyClientBehavior(ContractDescription contractDescription, 
		                                ServiceEndpoint endpoint, 
		                                ClientRuntime clientRuntime)
		{
		}
		
		public void AddBindingParameters(ContractDescription contractDescription, 
		                                 ServiceEndpoint endpoint, 
		                                 BindingParameterCollection bindingParameters)
		{
			Console.WriteLine("add " + WCFUtil.TARGET_IDENTITY_HEADER_NAME + " SOAP header to signature parts");
			ChannelProtectionRequirements requirements = bindingParameters.Find<ChannelProtectionRequirements>();
			MessagePartSpecification targetIdentityPart = 
				new MessagePartSpecification(new XmlQualifiedName(WCFUtil.TARGET_IDENTITY_HEADER_NAME, WCFUtil.TARGET_IDENTITY_HEADER_NAMESPACE));
			requirements.IncomingSignatureParts.AddParts(targetIdentityPart);
		}
	}
}
