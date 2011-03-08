<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="linkid_example._Default" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title></title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <asp:Button ID="Button1" runat="server" Text="Log In" onclick="Button1_Click" />
        
        <p>
            <asp:Label ID="OutputLabel" runat="server" Text=""></asp:Label>
        </p>
        <p>
            <asp:Label ID="ErrorLabel" runat="server" Text=""></asp:Label>
        </p>
    </div>
    <asp:HiddenField ID="HiddenField1" runat="server" />
    </form>
</body>
</html>
