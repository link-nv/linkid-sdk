/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
using System;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
	public interface AttributeClient
	{
		/// <summary>
		/// Gives back the attribute value of a single attribute of the given subject.
		/// This can be single-valued, multi-valued or compounded. 
		/// Compound attributes are returned with an array of Dictionaries.
		/// </summary>
		/// <param name="userId"></param>
		/// <param name="attributeName"></param>
		/// <returns></returns>
		/// <exception cref="RuntimeException">Invalid response received</exception>
		/// <exception cref="AttributeNotFoundException">Attribute not found</exception>
		/// <exception cref="RequestDeniedException">Attribute Request denied</exception>
		/// <exception cref="AttributeUnavailableException">Attribute unavailable</exception>
		T getAttributeValue<T>(string userId, string attributeName);
		
		/// <summary>
		/// Gives back the attribute values via the specified dictionary of attributes. 
		/// The dictionary should hold the requested attribute names as keys. 
		/// The method will find the corresponding values.
		/// </summary>
		/// <param name="userId"></param>
		/// <param name="attributes"></param>
		/// <exception cref="RuntimeException">Invalid response received</exception>
		/// <exception cref="AttributeNotFoundException">Attribute not found</exception>
		/// <exception cref="RequestDeniedException">Attribute Request denied</exception>
		/// <exception cref="AttributeUnavailableException">Attribute unavailable</exception>
		void getAttributeValues(string userId, Dictionary<string, object> attributes);
		
		/// <summary>
		/// Gives back a dictionary of attributes for the given subject that this application is allowed to read.
		/// </summary>
		/// <param name="userId"></param>
		/// <returns></returns>
		/// <exception cref="RuntimeException">Invalid response received</exception>
		/// <exception cref="AttributeNotFoundException">Attribute not found</exception>
		/// <exception cref="RequestDeniedException">Attribute Request denied</exception>
		/// <exception cref="AttributeUnavailableException">Attribute unavailable</exception>
		Dictionary<string, object> getAttributeValues(string userId);
	}
}
