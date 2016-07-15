linkID SDK
==========

This project contains the Java SDK artifacts for integrating linkID in your webapp.

If you have absolutely no idea what linkID is, please visit [www.linkid.be](http://www.linkid.be) first.

There is also support for [.NET](https://github.com/link-nv/linkid-sdk-dotnet) and [PHP](https://github.com/link-nv/linkid-sdk-php).

A good starting point if you want to know how to integrate linkID in your webapp is in the [wiki](https://github.com/link-nv/linkid-sdk/wiki)


Migration notes
===============

v4.17.0
---
  * WalletOrganization: add/update/remove/list support


v4.14.0
---
  * net.link.safeonline.sdk.api.themes.LinkIDThemeStatusCode -> net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode
  * themes -> themeList
  * walletOrganizationAdd support

v4.12.0
----
  * Unexpected, Maintenace, Permission denied error codes now thrown as runtime exceptions ( LinkIDUnexpectedException, LinkIDMaintenanceException, LinkIDPermissionDeniedException)

v4.11.4
----
  * walletOrganizationList support
  * paymentConfiguration support in auth/mandates and LTQRs
  * paymentConfiguration{add/update/delete/list} support
  
v4.11.1
----
  * Voucher organization activate/deactivate support
  * List voucher organizations: filtering on id(s) support + includeStats support
  * Voucher organization list users support

v4.8.1
----
  * Voucher support
  * configApplications operation added

v4.7.1
----
  * walletOrganizationFriendly added to LinkIDWallet(Report)Transaction

v4.7.0
----
  * themes call: optional themeName filter

v4.6.1
----
  * LinkIDWallet(Report)Transaction: walletOrganizationId added
  * LinkIDPaymentTransaction: id added
  * Application name added in notification messages
  * Theme operations: add/remove/status/list added

v4.5.0
---
  * continue in browser support dropped

v4.3.0
---
  * ID mapping WS support dropped

v4.2.0
---
  * LinkIDWalletReportTransaction.id support
    * unique ID for each wallet report entry

v4.1.5
---
  * LinkIDAuthPollResponse
    * linkIDAuthenticationState -> authenticationState
    * linkIDAuthnResponse -> authnResponse

v4.1.3
---
  * getPaymentReport -> paymentReport
  * getParkingReport -> parkingReport
  * getWalletReport -> walletReport
  * getWalletInfoReport -> walletInfoReport

v4.1.2
---
  * Payment order, Payment transaction: refundedDate field added
  * Wallet transaction: refundedDate, committedDate added
  * Payment report calls merged
  * Parking report calls merged

v4.1
---
  * Configuration simplified
    * no more need to configure a LinkIDConfigFilter or set a LinkIDConfigHolder. The WS client now takes an extra LinkIDConfigService argument. You can use the default LinkIDConfig implementation which will look for a linkID.properties file or you can use your own.
    * linkid-config module no longer exists
  * Voucher support

v4.0
---
  * LinkIDCallbackResponse to encapsulate the parsing the query parameters
  * LinkIDCallbackConstants.PARAM_APPLICATION_ID -> LinkIDCallbackConstants.PARAM_APPLICATION_NAME
  * Possibility to override the linkID notification location for LTQR, authentication and mandata payments
  * New notification topics to provide an alternative to actively polling linkID
    * AUTHENTICATION_RETRIEVED
    * AUTHENTICATION_SUCCESS
    * AUTHENTICATION_PAYMENT_FINISHED
    * AUTHENTICATION_CANCELED
    * AUTHENTICATION_FAILED
  * ConfigWalletApplications operation: fetch apps allowed to use specified wallet organization.
    * Only allowed if requesting app has the reporting permission on that wallet organization

v3.9.2
----
  * getThemes: owner flag support
  * LinkIDWalletReportInfo support

v3.9
----
  * LTQR Bulk pushs support
  * FavoritesConfiguration: pass along the URL of the image to use, not the encoded contents

v3.7.3
----
  * QRCodeInfo.targetBlank support dropped
  * LTQR FavoritesConfiguration support
  * LinkIDServiceClient.capture renamed to paymentCapture
  * paymentRefund support
  * LinkIDPaymentCaptureXXX classes moved to net.link.safeonline.sdk.api.ws.linkid.payment package from net.link.safeonline.sdk.api.ws.linkid.capture
  * LinkIDMandatePaymentXXX classes moved to net.link.safeonline.sdk.api.ws.linkid.payment package from net.link.safeonline.sdk.api.ws.linkid.mandate
  * LinkIDWalletReports: type support 
  * getWalletInfoReport support 
  * walletXXX calls and reporting calls return a permission denied error code
  * WalletInfoReport: returns localized wallet organization
  * WalletReport: returns localized application, returns payment description if applicable
  * LTQR.change: conflict error code introduced case the change operation fails due to locking

v3.4
----
  * All linkID WSDL's have been merged into 1 and so have the web service clients
  	* See: [LinkIDServiceClient](https://github.com/link-nv/linkid-sdk/blob/master/linkid-sdk-api/src/main/java/net/link/safeonline/sdk/api/ws/linkid/LinkIDServiceClient.java)
  	* Note: the attribute, data, idmapping, xkms2 and STS web services remained the same
  * Support for the HAWS protocol has been dropped
  * Payment status: refundAmount support
  * Payment reports: refundAmount support
  * LTQR Payment state == LinkIDPaymentState
  * LTQR: push, change, info now has userAgent support and will return more info about the linkID QR code
  * The linkID QR code info in LinkIDAuthSession is now avail in qrCodeInfo
  * Support for notification topic: **urn:net:lin-k:linkid:topic:ltqr:session:new**
  * Support for notification topic: **urn:net:lin-k:linkid:topic:ltqr:session:update**
  * Convenience class **LinkIDNotificationMessage** for parsing linkID notification topics
  * Support for notification topic: **urn:net:lin-k:linkid:topic:config:update**
  * Support for notification topic: **urn:net:lin-k:linkid:topic:payment:update**
    * This can be used as replacement for the paymentStatusLocation configuration
  * LTQR
    * oneTimeUse is now the ON_FINISH locktype (see: LinkIDLTQRLockType)
    * extra lock option: ON_SCAN (lock the LTQR code when 1st user scans it)
    * waitForUnlock is now waitForUnblock
    * resetUsed is now unlock
    * unlock is now unblock ( sorry :/ )
  * SAML v2.0 objects encapsulated from the WS client, no more need for the LinkIDAuthWSUtils class
  * No longer possible to specify a list of identity profiles, only 1 is allowed now.
    * If you want more complex identity profiles, just request a new identity from someone from linkID
    