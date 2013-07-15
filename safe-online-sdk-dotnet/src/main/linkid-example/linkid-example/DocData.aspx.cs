using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

using safe_online_sdk_dotnet;

namespace linkid_example
{
    public partial class DocData : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            // device context
            Dictionary<string, string> deviceContextMap = new Dictionary<string, string>();
            deviceContextMap.Add(RequestConstants.DEVICE_CONTEXT_TITLE, "Test .NET context");
            LinkIDLogin.setDeviceContext(Session, deviceContextMap);
        }
    }
}
