<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="LinkIDLogin.aspx.cs" Inherits="linkid_example.LinkIDLogin" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title></title>
    
    <meta http-equiv="pragma" content="no-cache" />
    <meta http-equiv="cache-control" content="no-cache, must-revalidate" />
    <meta http-equiv="expires" content="-1" />
    
</head>
<body onload="document.forms[0].submit()">
<!--
<body>
-->

    <noscript>
        <p>
            <strong>Note:</strong> Since your browser does not support JavaScript, you must press the Continue button once to proceed.
        </p>
    </noscript>

    <form id="form1" runat="server" method="post" autocomplete="off" target="_self">
    
        <asp:HiddenField ID="SAMLRequest" runat="server" />
        <asp:HiddenField ID="LoginMode" runat="server" />
        <asp:HiddenField ID="StartPage" runat="server" />
        
        
        <noscript>
            <input type="submit" value="Continue" />
        </noscript>
        
    </form>

</body>
</html>
