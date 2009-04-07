/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 22/12/2008
 * Time: 12:18
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of AuthenticationProtocolContext.
	/// </summary>
	public class AuthenticationProtocolContext
	{
		private readonly string userId;
		
		private readonly string authenticatedDevice;
		
		public AuthenticationProtocolContext(string userId, string authenticatedDevice)
		{
			this.userId = userId;
			this.authenticatedDevice = authenticatedDevice;
		}
		
		public string getUserId() {
			return this.userId;
		}
		
		public String getAuthenticatedDevice() {
			return this.authenticatedDevice;
		}
	}
}
