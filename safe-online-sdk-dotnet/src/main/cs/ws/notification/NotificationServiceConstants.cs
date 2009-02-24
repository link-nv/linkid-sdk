/*
 * Created by SharpDevelop.
 * User: devel
 * Date: 24/12/2008
 * Time: 10:43
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;

namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Constants for the WS-Notification Web Services.
	/// </summary>
	public class NotificationServiceConstants
	{
		public static readonly string TOPIC_DIALECT_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";
	
		public static readonly string NOTIFICATION_STATUS_SUCCESS = "urn:net:lin-k:safe-online:notification:status:Success";
		public static readonly string NOTIFICATION_STATUS_SUBSCRIPTION_NOT_FOUND = "urn:net:lin-k:safe-online:notification:status:SubscriptionNotFound";
		public static readonly string NOTIFICATION_STATUS_PERMISSION_DENIED = "urn:net:lin-k:safe-online:notification:status:PermissionDenied";
		public static readonly string NOTIFICATION_STATUS_SUBSCRIPTION_FAILED = "urn:net:lin-k:safe-online:notification:status:SubscriptionFailed";
		
		private NotificationServiceConstants()
		{
		}
	}
}
