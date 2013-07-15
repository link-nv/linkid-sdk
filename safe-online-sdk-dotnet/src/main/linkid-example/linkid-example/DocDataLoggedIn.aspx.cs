using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

using safe_online_sdk_dotnet;

namespace linkid_example
{
    public partial class DocDataLoggedIn : System.Web.UI.Page
    {
        public static readonly String DD_TOKEN = "urn:com:docdata:token";
        public static readonly String DD_TOKEN_ID = "urn:com:docdata:token:id";
        public static readonly String DD_TOKEN_PRETTY_PRINT = "urn:com:docdata:token:prettyprint";
        public static readonly String DD_TOKEN_PRIORITY = "urn:com:docdata:token:priority";
        public static readonly String DD_TOKEN_TYPE = "urn:com:docdata:token:type";

        private List<DD_Token> tokens = new List<DD_Token>();

        protected void Page_Load(object sender, EventArgs e)
        {
            AuthenticationProtocolContext authContext =
                (AuthenticationProtocolContext)Session[LinkIDLogin.SESSION_AUTH_CONTEXT];
            if (null != authContext && null != authContext.getAttributes())
            {
                // parse DocData tokens
                List<AttributeSDK> docdataTokens = authContext.getAttributes()[DD_TOKEN];
                foreach (AttributeSDK docdataToken in docdataTokens)
                {
                    Compound compound = (Compound)docdataToken.getValue();
                    AttributeSDK idMember = compound.membersMap[DD_TOKEN_ID];
                    AttributeSDK prettyPrintMember = compound.membersMap[DD_TOKEN_PRETTY_PRINT];
                    AttributeSDK typeMember = compound.membersMap[DD_TOKEN_TYPE];
                    tokens.Add(new DD_Token((String)idMember.getValue(), 
                        (String)prettyPrintMember.getValue(), (String)typeMember.getValue()));
                }
                this.DDRepeater.DataSource = tokens;
                this.DDRepeater.DataBind();
            }
        }
    }

    public class DD_Token
    {
        public String id { get; set; }
        public String prettyPrint { get; set; }
        public String type { get; set; }

        public DD_Token(String id, String prettyPrint, String type)
        {
            this.id = id;
            this.prettyPrint = prettyPrint;
            this.type = type;
        }
    }
}
