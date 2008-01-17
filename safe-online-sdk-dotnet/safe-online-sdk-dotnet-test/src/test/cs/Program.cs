using System;

using safe_online_sdk_dotnet_test.test.cs;

namespace safe_online_sdk_dotnet_test
{
	class Program
	{
		public static void Main(string[] args)
		{
			Console.WriteLine("starting web server...");
			new WebServerTest().StartWebServer();
		}
	}
}
