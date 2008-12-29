/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Security;
using System.ServiceModel.Security.Tokens;
using System.Security.Cryptography.X509Certificates;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// OlasBinding.
	/// 
	/// This custom binding provides both transport security as message integrity, as enforced by the OLAS web services.
	/// </summary>
	public class OlasBinding : Binding
	{
		private BindingElementCollection bindingElements;
		
		public OlasBinding(X509Certificate2 olasCertificate) {
			
			// Get CN from olas certificate, used to set Dns Identity Claim
			string[] issuer = olasCertificate.Issuer.Split(',');
			string cn = issuer[0].Split('=')[1];
			
			HttpsTransportBindingElement httpsTransport = new HttpsTransportBindingElement();
			TextMessageEncodingBindingElement encoding = new TextMessageEncodingBindingElement();
			encoding.MessageVersion = MessageVersion.Soap11;

			AsymmetricSecurityBindingElement securityBinding = SecurityBindingElement.CreateMutualCertificateDuplexBindingElement();
			securityBinding.LocalClientSettings.IdentityVerifier = new DnsIdentityVerifier(new DnsEndpointIdentity(cn));
			securityBinding.AllowSerializedSigningTokenOnReply = true;
			securityBinding.SecurityHeaderLayout = SecurityHeaderLayout.Lax;
			
			this.bindingElements = new BindingElementCollection();
			this.bindingElements.Add(securityBinding);
			this.bindingElements.Add(encoding);
			this.bindingElements.Add(httpsTransport);
		}
		
		public override BindingElementCollection CreateBindingElements() {
			return this.bindingElements.Clone();
		}
		
		public override string Scheme {
			get { return "https"; }
		}
		
		public override IChannelFactory<TChannel> BuildChannelFactory<TChannel>(BindingParameterCollection parameters)
		{
			Console.WriteLine("build channel factory");
			return null;
		}
		
		public override bool CanBuildChannelFactory<TChannel>(BindingParameterCollection parameters)
		{
			Console.WriteLine("can build channel factory");
			return true;
		}
	}
}
