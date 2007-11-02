/*
 * Gemaakt met SharpDevelop.
 * Gebruiker: Frank Cornelis
 * Datum: 2/11/2007
 * Tijd: 14:57
 * 
 * Dit sjabloon wijzigen: Extra | Opties |Coderen | Standaard kop bewerken.
 */

using System;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;

namespace safe_online_sdk_dotnet.test.cs
{
	[TestFixture]
	public class TestIdMapping
	{
		[Test]
		public void TestMethod()
		{
			IdMappingClient idMappingClient = new IdMappingClientImpl("192.168.5.102:8080");
			idMappingClient.getUsername("admin");
		}
	}
}
