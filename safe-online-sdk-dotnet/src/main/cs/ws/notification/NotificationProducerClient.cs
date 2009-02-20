/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;

namespace safe_online_sdk_dotnet
{
	public interface NotificationProducerClient
	{
		/// <summary>
		/// Subscription for the application at the specified address to the specified topic.
		/// </summary>
		/// <param name="topic"></param>
		/// <param name="address"></param>
		/// <exception cref="SubscriptionFailedException"></exception>
		/// <exception cref="RuntimeException"></exception>
		void subscribe(string topic, string address);
	}
}
