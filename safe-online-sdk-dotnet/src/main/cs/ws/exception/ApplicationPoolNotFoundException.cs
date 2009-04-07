/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 6/04/2009
 * Time: 16:41
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of ApplicationPoolNotFoundException.
	/// </summary>
	public class ApplicationPoolNotFoundException : System.Exception
	{
		private string messsage;
		
		public ApplicationPoolNotFoundException()
		{
		}
		
		public ApplicationPoolNotFoundException(string message) {
			this.messsage = message;
		}
		
		/// <summary>
		/// Constructor needed for serialization when exception propagates from a remoting server to the client.
		/// </summary>
		/// <param name="info"></param>
		/// <param name="context"></param>
		protected ApplicationPoolNotFoundException(System.Runtime.Serialization.SerializationInfo info, System.Runtime.Serialization.StreamingContext context) {
		}
		
		public string getMessage() {
			return this.messsage;
		}
	}
}
