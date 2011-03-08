/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 22/12/2008
 * Time: 12:18
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Collections;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of AuthenticationProtocolContext.
	/// </summary>
	public class AuthenticationProtocolContext
	{
		private readonly string userId;
		
		private readonly List<String> authenticatedDevices;

        private readonly Dictionary<String, List<AttributeSDK>> attributes;

        public AuthenticationProtocolContext()
        {
            this.userId = null;
            this.authenticatedDevices = null;
            this.attributes = null;
        }

        public AuthenticationProtocolContext(string userId, List<String> authenticatedDevices, Dictionary<String, List<AttributeSDK>> attributes)
		{
			this.userId = userId;
			this.authenticatedDevices = authenticatedDevices;
			this.attributes = attributes;
		}
		
		public string getUserId() {
			return this.userId;
		}

		public List<String> getAuthenticatedDevices() {
			return this.authenticatedDevices;
		}

        public Dictionary<String, List<AttributeSDK>> getAttributes()
        {
			return this.attributes;
		}
	}
}
