linkID SDK
==========

This project contains the Java SDK artifacts for integrating linkID in your webapp.

If you have absolutely no idea what linkID is, please visit [www.linkid.be](http://www.linkid.be) first.

There is also support for [.NET](https://github.com/link-nv/linkid-sdk-dotnet) and [PHP](https://github.com/link-nv/linkid-sdk-php).

A good starting point if you want to know how to integrate linkID in your webapp is in the [wiki](https://github.com/link-nv/linkid-sdk/wiki)


Migration notes
===============

v3.0
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

v3.2
----
  * Support for notification topic: **urn:net:lin-k:linkid:topic:ltqr:session:new**
  * Support for notification topic: **urn:net:lin-k:linkid:topic:ltqr:session:update**
  * Convenience class **LinkIDNotificationMessage** for parsing linkID notification topics
  
v3.3
----
  * Support for notification topic: **urn:net:lin-k:linkid:topic:config:update**

v3.4
---
  * Support for notification topic: **urn:net:lin-k:linkid:topic:payment:update**
    * This can be used as replacement for the paymentStatusLocation configuration
