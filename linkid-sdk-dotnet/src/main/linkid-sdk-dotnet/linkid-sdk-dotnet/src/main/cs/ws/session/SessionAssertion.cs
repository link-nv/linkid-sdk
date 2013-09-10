/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 6/04/2009
 * Time: 16:11
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
 
using SessionWSNameSpace;
using System;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of SessionAssertion.
	/// </summary>
	public class SessionAssertion
	{
		private string subject;
		
		private string applicationPool;
		
		private Dictionary<DateTime, string> authentications;
		
		public SessionAssertion(AssertionType assertion)
		{
			subject = assertion.Subject;
			applicationPool = assertion.ApplicationPool;
			authentications = new Dictionary<DateTime, string>();
			foreach(AuthnStatementType statement in assertion.AuthnStatement) {
				authentications.Add(statement.Time, statement.Device);
			}
		}
		
		public string getSubject() {
			return subject;
		}
		
		public string getApplicationPool() {
			return applicationPool;
		}
		
		public Dictionary<DateTime, string> getAuthentications() {
			return authentications;
		}
	}
}
