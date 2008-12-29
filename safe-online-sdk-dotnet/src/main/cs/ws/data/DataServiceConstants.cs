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
	/// Constants for the Data Web Service.
	/// </summary>
	public class DataServiceConstants
	{
		public static readonly string ATTRIBUTE_OBJECT_TYPE = "Attribute";
		
		public static readonly string LIBERTY_SOAP_BINDING_NAMESPACE = "urn:liberty:sb:2005-11";
		
		public static readonly string TOPLEVEL_STATUS_CODE_OK = "OK";
		public static readonly string TOPLEVEL_STATUS_CODE_FAILED = "Failed";
		
		public static readonly string SECONDLEVEL_STATUS_CODE_NOT_AUTHORIZED = "ActionNotAuthorized";
		public static readonly string SECONDLEVEL_STATUS_CODE_UNSUPPORTED_OBJECT_TYPE = "UnsupportedObjectType";
		public static readonly string SECONDLEVEL_STATUS_CODE_NO_MULTIPLE_ALLOWED = "NoMultipleAllowed";
		public static readonly string SECONDLEVEL_STATUS_CODE_PAGINATION_NOT_SUPPORTED = "PaginationNotSupported";
		public static readonly string SECONDLEVEL_STATUS_CODE_DOES_NOT_EXIST = "DoesNotExist";
		public static readonly string SECONDLEVEL_STATUS_CODE_MISSING_OBJECT_TYPE = "MissingObjectType";
		public static readonly string SECONDLEVEL_STATUS_CODE_INVALID_DATA = "InvalidData";
		public static readonly string SECONDLEVEL_STATUS_CODE_EMPTY_REQUEST = "EmptyRequest";
		public static readonly string SECONDLEVEL_STATUS_CODE_MISSING_SELECT = "MissingSelect";
		public static readonly string SECONDLEVEL_STATUS_CODE_MISSING_CREDENTIALS = "MissingCredentials";
		public static readonly string SECONDLEVEL_STATUS_CODE_MISSING_NEW_DATA_ELEMENT = "MissingNewDataElement";
		
		private DataServiceConstants()
		{
		}
	}
}
