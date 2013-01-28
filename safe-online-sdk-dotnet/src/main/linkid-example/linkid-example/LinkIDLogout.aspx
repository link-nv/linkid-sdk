<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="LinkIDLogout.aspx.cs" Inherits="linkid_example.LinkIDLogout" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>linkID Single Logout Demo</title>
</head>
<body>    
    <h1>linkID Single Logout Demo</h1>

    <form id="form1" runat="server">
        <div>
            <p>
                <asp:Label ID="OutputLabel" runat="server" Text=""></asp:Label>
            </p>
            <p>
                <asp:Label ID="ErrorLabel" runat="server" Text=""></asp:Label>
            </p>
        </div>
    
         <asp:Button ID="LogoutButton" runat="server" Text="Log Out" onclick="Button1_Click" />
            
        <asp:HiddenField ID="HiddenField1" runat="server" />

    </form>
    
    <a href="LoggedIn.aspx">Back</a>
    
</body>
</html>
