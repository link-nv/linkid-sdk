/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;

namespace safe_online_sdk_dotnet
{
	public interface DataClient
	{
		void setAttributeValue(string userId, string attributeName, object attributeValue);
		
		/// <summary>
		/// Gives back the attribute value of an attribute.
		/// If attribute is missing a AttributeNotFoundException is thrown.
		/// null is returned if the attribute value is actually null.
		/// </summary>
		/// <param name="userId"></param>
		/// <param name="attributeName"></param>
		/// <returns>default(T), in case T is of type DateTime DateTime.MIN_VALUE is returned.</returns>
		/// <exception cref="RuntimeException">Invalid response received.</exception>
		/// <exception cref="RequestDeniedException">Data request denied.</exception>
		/// <exception cref="SubjectNotFoundException">Subject not found.</exception>
		T getAttributeValue<T>(string userId, string attributeName);

		/// <summary>
		/// Creates a new (empty) attribute for the given subject.
		/// </summary>
		/// <param name="userId"></param>
		/// <param name="attributeName"></param>
		/// <param name="objectValue">A string, DateTime, ... or an array of those types or 
		/// a Dictionary<string, object> in case of compound.</string,></param>
		/// <exception cref="RuntimeException">Creation failed.</exception>
		void createAttribute(string userId, string attributeName, object objectValue);
		
		/// <summary>
		/// Removes an attribute for the given subject.
		/// </summary>
		/// <param name="userId">the subject from which to remove the attribute</param>
		/// <param name="attributeName">the name of the attribute to be removed</param>
		/// <param name="attributeId">the optional attributeId in case of a compound attribute</param>
		/// <exception cref="RuntimeException">Delete failed.</exception>
		void removeAttribute(string userId, string attributeName, string attributeId);
		
	}
}
