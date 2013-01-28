/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 22/12/2008
 * Time: 21:31
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Authentication Exception class. Throw if violations are detected in the authentication protocol.
	/// </summary>
    [Serializable]
	public class AuthenticationExceptionInvalidAudience : System.Exception
	{
		private string messsage;
		
		public AuthenticationExceptionInvalidAudience()
		{
		}
		
		public AuthenticationExceptionInvalidAudience(string message) {
			this.messsage = message;
		}
		
		/// <summary>
		/// Constructor needed for serialization when exception propagates from a remoting server to the client.
		/// </summary>
		/// <param name="info"></param>
		/// <param name="context"></param>
        protected AuthenticationExceptionInvalidAudience(System.Runtime.Serialization.SerializationInfo info, System.Runtime.Serialization.StreamingContext context)
        {
		}
		
		public string getMessage() {
			return this.messsage;
		}
	}
}
