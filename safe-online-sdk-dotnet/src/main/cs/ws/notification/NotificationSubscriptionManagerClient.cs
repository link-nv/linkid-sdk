/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;

namespace safe_online_sdk_dotnet
{
	public interface NotificationSubscriptionManagerClient
	{
		/// <summary>
		/// Unsubscribe application at specified address from specific topic.
		/// </summary>
		/// <param name="topic"></param>
		/// <param name="address"></param>
		/// <exception cref="SubscriptionNotFoundException"></exception>
		/// <exception cref="RequestDeniedException"></exception>
		void unsubscribe(string topic, string address);
	}
}
