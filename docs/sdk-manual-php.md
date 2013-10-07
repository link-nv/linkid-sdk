Introduction
============

The linKID PHP SDK is using the SimpleSAMLphp library as a base. See
[](http://simplesamlphp.org/) for more information. The version of
SimpleSAMLphp this SDK is based on is `1.8.2` A few patches were needed
on this library to work with linkID. The original files are placed
besides the patched ones ( .orig ). Following files were patched:

-   lib/SAML2/Binding.php

-   lib/SAML2/HTTPRedirect.php

-   lib/SAML2/Assertion.php

-   modules/saml/lib/Auth/Source/SP.php

-   lib/SimpleSAML/Auth/Default.php

-   modules/saml/www/sp/saml2-acs.php

Please note that in order for the SimpleSAMLphp library to work, you
will need to set an alias simplesaml to simplesamlphp/www in your
webserver configuration. So e.g.

                    
    Alias /simplesaml <your-sites-path>/simplesamlphp-1.8.2/www
                            
                

SAML v2.0 is a protocol that describes the means for applications to
authenticate themselves in a standard way with external authentication
and identity providers. This relieves the burden of identity management
from service providers such that they can concentrate on their service
and rest assured that user identity is managed for them in a secure
environment. For more information on the protocol and how linkID
incorporates it please refer to the general linkID SDK Manual.

Configuration
=============

This chapter explains the minimal steps needed to setup a SP application
which can authenticate with linkID using the SAML v2.0 protocol.

Setup linkID as a remote IdP (Identity Provider)
------------------------------------------------

You first have to configure linkID as a remote IdP. This is done by
editing the `metadata/saml20-idp-remote.php` file. The SDK already
provides you with a default setup, using the demo.linkid.be service.

                        

	$metadata['linkID'] = array(
       'name'                 => array(
           'en' => 'linkID',
       ),

       'SingleSignOnService' => 'https://192.168.5.14:8443/linkid-qr/auth-min',
       'SingleLogoutService' => 'https://192.168.5.14:8443/linkid-auth/logoutentry',

       'redirect.sign'      => true,
       'redirect.validate'  => true,
       'certificate'         => 'local-linkid.crt'
       //'caFile'             => 'local-linkid-ca.crt',
    );

                        
                    

Note that you will have to modify certificate and fingerprint to match
the linkID service's node certificate. This certificate is used for
validating the XML signature on incoming linkID SAML v2.0 responses. You
can download this certificate from the linkID service @
`https://demo.linkid.be/linkid-auth/pki-cert.pem`. Off course change
this url to the appropriate linkID service you are using. The PEM
encoded certificate needs to be stored in `cert/linkid.crt/`.

Setup your SP (Service Provider)
--------------------------------

You will have to configure your application as an authentication source
in the SDK. Do this by editing the `config/authsources.php` file. The
SDK already provides you with an example setup.

                        

    // linkID Example authentication source
    'example-mobile' => array('saml:SP',
        'privatekey'    => 'example-mobile.pem',
        'certificate'   => 'example-mobile.crt',
        'RelayState'    => '/www/example-mobile/loggedin.php',
        'entityID'      => 'example-mobile',
        'idp'           => 'linkID',
        'authproc'      => array(
            10 => array(
                'class' => 'core:LinkIDAttributes',
                'removeurnprefix'
            ),
        ),
    )

                        
                    

Note that you will have to generate your own keypair and certificate and
as the linkID operator to configure your application within linkID. The
private key and certificate should be placed in the `cert` directory of
the SDK and should match the `privatekey` and `certificate` parameters
in the authentication source config. Both are pem encoded. The `idp`
paramete should match the application name your linkID operator has
given to your application. This value will be used as `Issues` of the
SAML v2.0 AuthnRequest message. The `RelayState` points to the default
return URL the SDK will redirect to upon successful validation of the
linkID SAML v2.0 authentication Response.

Authentication
==============

This chapter explains how to initiate the linkID authentication process.

The SDK contains a basic linkID Mobile example which you could use to start
from. This example is located in `www/linkid-mobile`.

If you have successfully complete the configuration of the SDK for your
SP application, initiating the linkID authentication process is as
simple as follows:

                    

    $as = new SimpleSAML_Auth_Simple('linkid-mobile');

    if (!$as->isAuthenticated()) {

        // initiate linkID login
        $as->login(array(
            'saml:idp' => 'linkID',
            'ReturnTo' => 'http://localhost/linkid-example/index.php',
	        'ErrorURL'  => 'http://192.168.5.14/example-mobile/failed.php',
	        'linkID:mobileMinimal' => 'true'
        ));

                    
               

Note that the value passed to instantiating `SimpleSAML_Auth_Simple`
must match the unique name you gave your SP (authentication source) in
`config/authsources.php`. Also the `saml:idp` must match the unique name
you gave to the linkID IdP in `metadata/saml20-idp-remote.php`. Note
that in this example we explicitly say which page to redirect to upon
finalizatiton of the linkID authentication process using the `ReturnTo`
configuration parameter.

The `linkID:mobileMinimal` configuration parameter is needed to specify to SimpleSAMLPhp to jump out of the linkID QR iframe when done.

For more information on the HTML part of the linkID integration, please read the [linkID SDK docs](https://github.com/link-nv/linkid-sdk/wiki)

To set the language for the linkID authentication process, you can optionally add the `Language` request parameter. Supported locales are `nl,en,fr`.
Add the `DestinationParams` to the login configuration parameters as follows:

        $as->login(array(
            'saml:idp' => 'linkID',
            'ReturnTo' => 'http://localhost/linkid-example/index.php',
	        'ErrorURL'  => 'http://192.168.5.14/example-mobile/failed.php',
	        'linkID:mobileMinimal' => 'true'
            'DestinationParams' => '?Language=nl',
        ));
              

Attributes
==========

This chapter explains how to access the user's authentication
information upon successful linkID authentication.

After the user has authentication successfully against the linkID
service, linkID will send back a SAML v2.0 authentication response to
you. This Response will contain a SAML v2.0 assertion which will include
the linkID userID of that user and the attributes of him as configured
towards your SP application by the linkID operator. You can access this
information in the following manner:

                    

    if ($as->isAuthenticated()) {

        $authDataArray = $as->getAuthDataArray();
        $userId        = $authDataArray['saml:sp:NameID']['Value'];
        $attributes    = $as->getAttributes();

                    
                

There are 3 sorts of linkID attributes, single valued, multi valued and
compound. Refer to the general SDK manual for more information on this.
The SDK will return a map of the linKID attributes. You can access the
single valued and multi (non-compound) valued attributes in the
following manner:

                    

    $attributes['attribute.test'][0]

                    
               

Compound attributes are a bit more complex as the value of a compound
attribute is again a map of attributes. You can access a member of a
compound attribute in the following way:

                    

    $attributes['compound.attribute'][0]['compound.member.attribute'][0]

                    
               

SSL
===

When a user has completed the linkID authentication process, linkID will
post a SAML2 authentication response back to the SP. The SimpleSAMLphp
library constructs th path to where the POST is done ( the SAML v2.0
Assertion consumer service URL ) to something like

                    
                <host>/simplesaml/module.php/saml/sp/saml2-acs.php/listlist
                    
                

This URL is included in the SAML v2.0 authentication request that
SimpleSAMLphp constructs. To make sure that this URL is over SSL, so the
HTTP Post occurs safely over SSL, you should go to the "login.php" page
( where the SAML authentication process is initiated ) over SSL. Not
doing so will result in a browser warning and moreover sending the SAML
response which contains the user's ID and attributes unencrypted.

Non-mobile
===========

If you wish to use the "non-mobile" linkID authentication process in your webapp you'll need to firstly change the linkID entry location, that is the location where the SAML v2.0 authentication request will be sent to. This is specifid in `metadata/saml20-idp-remote.php`

       'SingleSignOnService' => 'https://192.168.5.14:8443/linkid-auth/entry',

In this authentication flow, linkID will not start in an iframe but in or a POPUP window or via a REDIRECT. The default mode is in a POPUP window.
If you wish to override the default popup mode, you can specify so by
adding an extra HTTP Request parameter in the SAML redirect binding
request towards the linkID service. Do this as follows:

                    

                $as->login(array(
                    'saml:idp' => 'linkID',
                    'ReturnTo' => 'http://localhost/linkid-example/index.php'
                    'DestinationParams' => '?login_mode=POPUP',
                    'ErrorURL' => 'http://localhost/linkid-example/error.php'
                ));

                    
               

Allowed values for login\_mode are: `REDIRECT, POPUP` The ErrorURL page
is where simplesamlphp will redirect to when a failed SAML response is
sent back by linkID. Note that a failed SAML response can just be that
the user has cancelled the authentication process. In that case the SAML
Response StatusCode URI will be
urn:oasis:names:tc:SAML:2.0:status:AuthnFailed.

To set the language for the linkID authentication process, you can
optionally add the `Language` request parameter just as you do with the
login\_mode. Note that in case the user has explicitly set his language
during the linkID authentication process, this setting will allways be
preferred. Allowed values are `nl,en,fr`. Example:

                    

                $as->login(array(
                    'saml:idp' => 'linkID',
                    'ReturnTo' => 'http://localhost/linkid-example/index.php'
                    'DestinationParams' => '?login_mode=POPUP&Language=nl',
                ));

                    
               

There is an optional HTTP request parameter for forcing the linkID
authentication process to go to the "register device" page or the
"authenticate device" page regardless of whether the user has
successfully authenticated himself in the browser ( a "deflowered"
cookie is set then ) This can be achieved by setting the parameter
`start_page`. Possible values are `NONE, REGISTER, AUTHENTICATE`
Example:

                    

                $as->login(array(
                    'saml:idp' => 'linkID',
                    'ReturnTo' => 'http://localhost/linkid-example/index.php'
                    'DestinationParams' => '?login_mode=POPUP&start_page=REGISTER',
                ));

                    
