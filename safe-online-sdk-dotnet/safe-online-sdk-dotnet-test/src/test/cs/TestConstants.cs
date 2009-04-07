/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;

namespace safe_online_sdk_dotnet_test.test.cs
{
	public sealed class TestConstants
	{
		public static readonly string workDir = "C:\\Users\\devel\\Csharp";
		
		public static readonly string testCrtIssuerName = "test";
		public static readonly string testApplicationName = "test";
		public static readonly string testPfxPath = workDir + "\\test.pfx";
		public static readonly string testPfxPassword = "secret";
		public static readonly string testCrtPath = workDir + "\\test.crt";
		
		public static readonly string olasCertPath = workDir + "\\sign.crt";
					
		public static readonly string olasHost = "sebeco-dev-22";
		public static readonly string wsLocation = olasHost + ":8443";
		public static readonly string olasAuthEntry = "https://" + wsLocation + "/olas-auth/entry";
		
		public static readonly string localhost = "10.0.2.15";
		
		public static readonly string loginAttribute = "urn:net:lin-k:safe-online:attribute:login";
		
		public static readonly string testLogin = "wvdhaute";
		
		public static readonly string testMultiStringAttribute = "urn:test:multi:string";
		public static readonly string testMultiDateAttribute = "urn:test:multi:date";
		public static readonly string testCompoundAttribute = "urn:test:compound";
		public static readonly string testSingleStringAttribute = "urn:test:single:string";
		public static readonly string testSingleDateAttribute = "urn:test:single:date";
		
		public static readonly string olasTopicRemoveUser = "urn:net:lin-k:safe-online:topic:user:remove";
		public static readonly string olasTopicUnsubscribeUser = "urn:net:lin-k:safe-online:topic:user:unsubscribe";

		private TestConstants()
		{
		}
	}
}
