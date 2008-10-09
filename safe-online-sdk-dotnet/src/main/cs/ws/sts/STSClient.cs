/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;

namespace safe_online_sdk_dotnet
{
	public interface STSClient
	{
		bool validateAuthnResponse(string authnResponse, TrustDomainType trustDomain);
	}
}
