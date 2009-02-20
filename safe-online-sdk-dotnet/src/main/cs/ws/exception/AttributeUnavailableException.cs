/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 23/12/2008
 * Time: 14:56
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of AttributeUnavailableException.
	/// </summary>
	public class AttributeUnavailableException : System.Exception
	{
		private string messsage;
		
		public AttributeUnavailableException()
		{
		}
		
		public AttributeUnavailableException(string message) {
			this.messsage = message;
		}
		
		/// <summary>
		/// Constructor needed for serialization when exception propagates from a remoting server to the client.
		/// </summary>
		/// <param name="info"></param>
		/// <param name="context"></param>
		protected AttributeUnavailableException(System.Runtime.Serialization.SerializationInfo info, System.Runtime.Serialization.StreamingContext context) {
		}
		
		public string getMessage() {
			return this.messsage;
		}
	}
}
