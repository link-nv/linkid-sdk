/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 6/04/2009
 * Time: 16:10
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Description of SessionTrackingClient.
	/// </summary>
	public interface SessionTrackingClient
	{
		List<SessionAssertion> getAssertions(string session, string subject, 
		                                     List<string> applicationPools);
		
	}
}
