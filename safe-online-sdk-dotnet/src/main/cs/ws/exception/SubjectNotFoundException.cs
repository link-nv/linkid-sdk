/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 24/12/2008
 * Time: 11:10
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of SubjectNotFoundException.
	/// </summary>
	public class SubjectNotFoundException : System.Exception
	{
		private string messsage;
		
		public SubjectNotFoundException()
		{
		}
		
		public SubjectNotFoundException(string message) {
			this.messsage = message;
		}
		
		/// <summary>
		/// Constructor needed for serialization when exception propagates from a remoting server to the client.
		/// </summary>
		/// <param name="info"></param>
		/// <param name="context"></param>
		protected SubjectNotFoundException(System.Runtime.Serialization.SerializationInfo info, System.Runtime.Serialization.StreamingContext context) {
		}
		
		public string getMessage() {
			return this.messsage;
		}
	}
}
