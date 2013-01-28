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
        public static readonly string workDir = "C:\\cygwin\\home\\devel\\keystores";

        public static readonly string testCrtIssuerName = "DemoTest";
        public static readonly string testApplicationName = "demo-test";
        public static readonly string testPfxPath = workDir + "\\demotest.p12";
        public static readonly string linkidCertPath = workDir + "\\linkid-local.crt";
//        public static readonly string linkidCertPath = workDir + "\\linkid.crt";
        public static readonly string testPfxPassword = "secret";
        public static readonly string testCrtPath = workDir + "\\demotest.crt";
        public static readonly string testKeyPath = workDir + "\\demotest.key";
        public static readonly string testFooPath = workDir + "\\foo.crt";

//        public static readonly string linkidHost = "demo.linkid.be";
        public static readonly string linkidHost = "192.168.5.14";
//        public static readonly string wsLocation = linkidHost;
        public static readonly string wsLocation = linkidHost + ":8443";
        public static readonly string linkidAuthEntry = "https://" + wsLocation + "/linkid-auth/entry";
        public static readonly string linkidLogoutEntry = "https://" + wsLocation + "/linkid-auth/logoutentry";

        public static readonly string localhost = "192.168.5.20";

        public static readonly string loginAttribute = "device.password.login";
		
		public static readonly string testLogin = "admin";
		
		public static readonly string testMultiStringAttribute = "urn:test:multi:string";
		public static readonly string testMultiDateAttribute = "urn:test:multi:date";
		public static readonly string testCompoundAttribute = "urn:test:compound";
		public static readonly string testSingleStringAttribute = "urn:test:single:string";
		public static readonly string testSingleDateAttribute = "urn:test:single:date";
		
		public static readonly string linkidTopicRemoveUser = "urn:net:lin-k:safe-online:topic:user:remove";
		public static readonly string linkidTopicUnsubscribeUser = "urn:net:lin-k:safe-online:topic:user:unsubscribe";

		private TestConstants()
		{
		}
	}
}
