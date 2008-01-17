using System;

namespace safe_online_sdk_dotnet
{
	public interface STSClient
	{
		bool validateAuthnResponse(string authnResponse);
	}
}
