linkID SDK
==========

This project contains the SDK artifacts for integrating linkID in your webapp.

If you have absolutely no idea what linkID is, please visit [www.linkid.be](http://www.linkid.be) first.

How does it work?
-----------------

Roughly, if a user wants to authenticate using linkID on some website, the following will happen:

1. The user visiting your web application clicks on the linkID button
2. An authentication request is sent to the linkID service
3. linkID validates this request and shows a QR code in the browser
4. The user scans that QR code with the linkID mobile app
5. The user enters his pin code in the linkID mobile app
6. linkID marks the session attached to that QR code as authenticated with that user
7. Browser which was polling linkID sees this and an authentication response containing the user's linkID identity is sent to the web application.

What do you need to do to integrate?
------------------------------------

linkID does not allow any web application to make use of its service, it needs to know who's asking so it'll know what parts of the linkID user's identity the application has permission to view. For this purpose, the authentication request we talked about needs to be digitally signd by you. We are using the [SAML v2.0](http://docs.oasis-open.org/security/saml/v2.0/saml-bindings-2.0-os.pdf) standard for this.

Therefore, the first thing you will need to do, is contact someone @ linkID ( help@linkid.be ) who will setup an application configuration within the linkID service for you. You'll receive a keypair and certificate from us ( or you can provide one yourself if you want ) which you will need later on during configuration. Also a unique name for your web application needs to be agreed on.

We have 2 linkID service's running, [demo.linkid.be](demo.linkid.be) and [service.linkid.be](service.linkid.be), the latter being our production service. In the following sections we'll allways refer to demo.linkid.be so when you are switching over to production, don't forget to change these to service.linkid.be

Client side
-----------

To initiate a linkID login on your web page, you'll need to include the [linkID javascript file](https://demo.linkid.be/linkid-static/js/linkid.js) in your web page.
So:

 ```javascript
<script type="text/javascript" id="linkid-login-script" src="https://demo.linkid.be/linkid-static/js/linkid.js"\></script>
 ```

For the linkID login link you'll need to add

	<div>
	    <iframe id="linkid" style="display: none;"></iframe>
	</div>
	<div>
    	<a id="linkid-login-link" class="linkid-login" data-mobile-minimal="linkid" 
    		data-login-href="./startlogin" data-completion-href="./authenticated">
    		Login with linkID
    	</a>
	</div>

The iframe is where the QR code will be loaded in after linkID has validated the authentication request. The id of the iframe needs to match the **data-mobile-minimal** attribute.

The **data-login-href** points to the location that will send the authentication request. Check the Java or .NET section for more detail.

The **data-completion-href** points to the location the web application should redirect to when the user has successfully logged in with linkID.

Payments
--------

linkID can also be used as a payment provider. It acts as a proxy to the DocData payment provider. This allows you to be shielded as much as possible from the technicalities of integrating payment support in your web application. To be able to do this, you'll need to contact linkID and we will setup togeher with the DocData the payment merchant configuration for your web application.

Ok, but what more integration work do I need for this?
------------------------------------------------------
Payments have been added as an optional extension to the regular linkID authentication flow, making it hardly any more effort to integrate this in your web application.

Where the authentication request is created and sent, you'll just need to add a payment context to that authentication request. How to do so depends a bit on the framework you are using so check the Java or .NET sections for some more detail on this.

The payment context consists of:

* **Amount**: amount to pay, expressed in cents so amount of 100 == 1 EUR
* **Currency**: currency of the amount, only EUR is supported for now
* **Optional payment description**: linkID will allways first show a default, non modifiable payment description in the mobile app. This to ensure that the web application cannot by accident show a wrong payment amount to the user. The optional payment description can be used to add some extra meaningfull context to the payment transaction
* **Optional payment profile**: within the payment configuration in linkID and DocData for your application, you can define multiple payment profiles. These allows for specifying that for example for a certain transaction only credit cards are allowed to be used.
* **Optional payment validation time**: payment validation time is the time linkID will wait for validation of the payment transaction.

When the payment transaction has been validated, or the validation time has exceeded, an authentication response similar to the one you get when doing an authentication without payment will be returned. But now a payment response object will also be included here, containing the payment transaction ID, and the payment state of that transaction.

Wait, payment validation time?
-------------------------------
Yes, depending on the type of payment method, validation of the payment transaction can take more time than you want. Depending on your needs you can optionally make linkID wait a bit longer or not wait at all. By default linkID waits 5 seconds by the way. Now if you don't wait at all off course you'll want to be informed by other means that the payment transaction is considered valid. For that purpose when configuring your web application in linkID, you'll need to provide us a location where linkID can poke you to notify when a status update is available for a specific payment transaction. At that time, you'll fetch the status report of that transaction using the linkID payment WS.

The web service client is off course included in the SDK's, next to an example page in the example web applications

Java
====

We provide a very [basic web application](https://github.com/link-nv/linkid-sdk/tree/master/safe-online-sdk-example-mobile) to illustrate how to integrate linkID using our Java SDK. You can find this webapp here.

If you really can't wait to get this example working, the only thing you need to change is a file called [linkID.xml](https://github.com/link-nv/linkid-sdk/blob/master/safe-online-sdk-example-mobile/src/main/resources/linkID.xml). This contains properties telling our SDK to which linkID we will talk, and the location of the example webapp itself. Mofify the web.appBase and web.appConfidentialBase properties, build the war file, deploy it in tomcat and it should be working with our demo linkID server.

If you go to production and want to use the linkID production service ( service.linkid.be ), don't forget to change the properties in this file.

Maven
-----

First, you will need to add following maven dependencies available from our [maven repository](http://repo.linkid.be/).

	<dependency>
    	<groupId>net.lin-k.safe-online.client.sdk</groupId>
	    <artifactId>safe-online-sdk</artifactId>
    	<version>2.1</version>
	</dependency>
	<dependency>
    	<groupId>net.lin-k.safe-online.client.sdk</groupId>
	    <artifactId>safe-online-sdk-saml2</artifactId>
    	<version>2.1</version>
	</dependency>
	<dependency>
    	<groupId>net.lin-k.safe-online.client.sdk</groupId>
	    <artifactId>safe-online-sdk-ws</artifactId>
    	<version>2.1</version>
	</dependency>

If you are not a big fan of maven and use ant or your own fancy build tool, you can find those jar files directly [here](http://repo.linkid.be/releases/net/lin-k/safe-online/client/sdk/).

Configuration
-------------

That keystore and specific application name we talked about before, well we'll need to to let the SDK components know how to get those by providing following 3 context parameters in your web.xml:

* **linkID.app.name**: the specific application name
* **linkID.app.keyProvider**: provides the keystore to the SDK
* **web.landingPath**: the path where the authentication response will be sent to eventually

The key provider needs some extra mentioning. As the name hints, it points to the provider of the keys to be used to sign the authentication request. This context param will have the form of something like:

	class://example-mobile:secret:secret@net.link.safeonline.sdk.example.mobile.ExampleMobileKeyProviderService

Here example-mobile is the key entry alias of your keystore, the first "secret" is the keystore password and the second "secret" is the key entry password. The last part is a class you need to provide that will be used to load in the keystore.

In the example web application the keystore is expected to be in the classpath of the web application. You have other alternatives of providing the keystore. For this, please refer to the [Util configuration manual](https://github.com/link-nv/util/tree/master/util-config-manual)

Authentication Context
----------------------

Remember we talked about that location that will create and send the authentication request? Well this is the one we are talking about now.

The SDK provides a default servlet **net.link.safeonline.sdk.auth.servlet.InitiateLoginServlet** which will work out of the box if you have setup the keystore and app name configuration ok. Just make sure that it matches the path of the **data-login-href** JS data attribute of your linkID button

You can customize the authentication context a bit tho, simply override the InitiateLoginServlet and override the configureAuthenticationContext method.

For example if you are doing an authentication without payment context, it is possible to provide a custom context which will be shown in the linkID mobile app when the user has to enter his pin. You can do this as follows:

	authenticationContext.getDeviceContext().put( DeviceContextConstants.CONTEXT_TITLE, "Some custom login context" );

If you want to add a payment context of let's say 2 euro's, simply add the following:

	authenticationContext.setPaymentContext( new PaymentContextDO( 200, Currency.EUR ) );


Authentication response
-----------------------

When the authentication has finished, we said that linkID will send an authentication response with the user's linkID identity in it right? Well this authentication response, similarly to the authentication request you sent, has to be digitally signed to ensure nobody has tampered with it. To ensure it has been signed with the correct linkID keypair, you must provide the expected linkID certificate to the SDK. You can do this the easiest by adding that linkID certificate under the alias **linkid** in your keystore.

The SDK provides a servlet that handles all this magic for you, being **net.link.safeonline.sdk.auth.servlet.LoginServlet**. Just make sure it is bound to the same location you specified for the **web.landingPath** context parameter in your web.xml

When the response has been validated and parsed, the user's identity information will be pushed to the session. Also if a payment context was provided, a payment response object will be pushed on the session. You can access all that information using the **LoginManager** class.

Configuration filter
--------------------

The **net.link.safeonline.sdk.configuration.SDKConfigFilter** is a filter that will do all the magic of configuring the key providers, providing the app name to the SDK's servlet etc. Without this filter all of em will fail.

Payment status updates
----------------------

To receive payment status updates, you'll need to provide to us the location where we will poke your web application when we get an update. We will add a query parameter txn_id to that request with the transaction ID. So the request will look something like: 

	https://your.host.com/site/paymentUpdate?txn_id=132456-78996-4563-45636

When you'll receive such an update, you'll then have to fetch the complete status report using the linkID payment web service. The mobile example webapp contains an example servlet **PaymentStateChangedServlet** that illustrates this.

.NET
====

WIP

We provide a very basic web application to illustrate how to integrate linkID using our .NET SDK. You can find this webapp @ [linkID .NET SDK](http://repo.linkid.be/releases/net/lin-k/safe-online/client/sdk/safe-online-sdk-dotnet).




