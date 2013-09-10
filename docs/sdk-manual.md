Introduction
============

> This chapter will bring a short overview of the services offered by
> linkID.

linkID is an authentication, signature and user data exchange service.
It allows 3th party web applications to authenticate users, involve
users in transactions and obtain user identity data. It brings to users
a central identity management system.

linkID offers several services to applications:

1.  Authentication Service
    An application calls this service for user to authenticate using one
    of his configured devices. Upon success the application is informed
    and can grant access to the user.

    The authentication service has support for:

    -   OASIS SAML version 2.0 Web SSO profile with HTTP POST binding or
        HTTP REDIRECT binding.
    -   OASIS SAML version 2.0 Single Logout profile with HTTP POST
        binding
    -   OpenID Authentication v2.0 with support for OpenID Attribute
        Exchange v1.0

2.  Attribute Service
    An application can query the user’s attributes and use their values
    in its business processes. Attributes can only be read if the user
    has given explicit permission to do so.

    The implementation is according to the OASIS SAML version 2.0
    Attribute Query profile with SOAP binding.

3.  Data Service
    An application can push attributes to the user’s profile. These
    attributes could then be used by other applications (if allowed by
    the user and the providing application).

    The implementation is according to the Liberty Alliance ID-WSF Data
    Service Template version 2.1 specification.

4.  Name Identifier Mapping Service
    Via this service an application can perform a mapping from a certain
    identifier attribute to the user identifier. Of course this service
    is only available for an application after explicit authorization
    has been given by the operator.

    The implementation is an extension of the OASIS SAML version 2.0
    Name Identifier Mapping profile with SOAP binding.

5.  Security Token Service
    Via this service an application can validate an authentication token
    that has been received from the authentication web application.

    The implementation is according to the OASIS WS-Trust 1.3
    specification.

6.  Notification Service
    Via this service an application or device issuer can subscribe to a
    list of topics on which it wants to receive notifications from. In
    order to receive those notifications a consumer web service needs to
    be setup.

    The implementation is according to the OASIS WS-Notification 1.3
    specification.

The linkID SDK's provided components to ease the communication with the
linkID services. Besides client components for the web services you'll
also find various components ease integration of the linkID
authentication web application.

Concepts
--------

Some key concepts will be elaborated on to provide a good understanding
of the different SDK components.

Application
:   The entity that invokes operations on the linkID web services and
    linkID authentication web application. Every application is trusted
    by the linkID services via an X509 certificate and corresponding
    private key.

Operator
:   The entity that manages the linkID services and defines the trust
    relationship and access control towards applications.

Application Owner
:   Every application is owned by an application owner. The application
    owner can manage its application via the linkID application owner
    web application.

Application Pool
:   Every application can belong to one or more application pools.
    Application pools specify the timeout of a single sign-on
    authentication. If an authentication has taken place for an
    application in the same pool of another application, and the timeout
    has not exceeded, single sign-on can be used ( if configured to be
    allowed to use ).

Attributes
----------

Every subject (i.e. user) has different attributes. An attribute is
composed of a name, a UUID and a value. For example: the attribute with
name 'profile.givenName' has value 'Richard' and has a UUID
'0e05ffbc-94f9-4a29-a0b0-3bbe9a83a79e'.

A linkID attribute's value has a certain attribute type. Supported types
are:

-   String

-   Boolean

-   Integer

-   Double

-   Date

Besides the value type, different types of attributes are possible. We
define the following attribute categories:

### Single-valued Attributes

A single-valued attribute is, as the name suggests, an attribute with
only one single value attached to it.

### Multi-valued Attributes

A multi-valued attribute is an attribute where the attribute value is a
collection of primitive values. For example a mobile phone attribute
where a user can have multiple mobile phones. Note that each value has a
UUID linked to it.

### Compounded Attributes

A compounded attribute is an attribute that is composed of other
attributes. These other attributes are called the member attributes of
the compounded attribute. A compounded attribute only really makes sense
if the member attributes are multi-valued. The following picture
highlights the principal behind compounded attributes. The attribute
matrix.

![alt text](compounded-attribute.svg "The attribute matrix.")

As in multivalued attributes, each compound attribute value has its
UUID. Note that the members of a specific compound value have the same
UUID as the compound itself.

The operator can define new attribute types within the linkID system.
Every subject can have values for these attributes. A multi-valued
attribute is simply a grouping of attribute values over the horizontal
axis. A compounded attribute is defined as a grouping of attribute
values over the vertical axis.

Application Identity
--------------------

Every application can define its own application identity. An
application identity is a set of attribute types over which the
application wants access for a given subscribed subject. The application
identity is managed by the linkID operator and has a version which
allows it to evolve over time. Before a user can log in into an
application, he has to confirm usage of the attributes as defined in the
current application identity.

Web Services
============

> This chapter describes the web services that linkID offers towards
> applications.

A web service definition is composed out of a WSDL and a set of XSD
files.

Every linkID web service is secured via server-side SSL and a
client-side WS-Security signature on the SOAP message body. The
WS-Security signature is according to OASIS Web Services Security: SOAP
Message Security 1.0 Standard 200401, March 2004.

The request SOAP messages are to be signed with the X509v3 application
certificate. The certificate must be attached as `BinarySecurityToken`
within the `Security` SOAP header element according to 6.3 Binary
Security Tokens. The WS-Security SOAP header should also contain a
`Timestamp` according to section 10 - Security Timestamps of the OASIS
WS-Security 1.0 specification. The WS-Security signature should sign
both the SOAP body and the WS-Security `Timestamp` element. The
`KeyInfo` XML Digital Signature element should contain a corresponding
`SecurityTokenReference` according to 7.2 - Direct References.

An example SOAP message with WS-Security header is given in the
following example (we removed the namespace declaration attributes for
readability).

                    
    <soap:Envelope>
        <soap:Header>
            <wsse:Security soap:mustUnderstand="1">
                <wsse:BinarySecurityToken
                    EncodingType="http://docs.oasis-open.org/...#Base64Binary"
                    ValueType="http://docs.oasis-open.org/...#X509v3"
                    wsu:Id="the-cert">
                    ...
                </wsse:BinarySecurityToken>
                <ds:Signature>
                    <ds:SignedInfo>
                        <ds:CanonicalizationMethod
                            Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
                        <ds:SignatureMethod
                            Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
                        <ds:Reference URI="#the-body">
                            <ds:Transforms>
                                <ds:Transform
                                    Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
                            </ds:Transforms>
                            <ds:DigestMethod
                                Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
                            <ds:DigestValue>...</ds:DigestValue>
                        </ds:Reference>
                        <ds:Reference URI="#the-timestamp">
                            <ds:Transforms>
                                <ds:Transform
                                    Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
                            </ds:Transforms>
                            <ds:DigestMethod
                                Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
                            <ds:DigestValue>...</ds:DigestValue>
                        </ds:Reference>
                    </ds:SignedInfo>
                    <ds:SignatureValue>
                        ...
                    </ds:SignatureValue>
                    <ds:KeyInfo>
                        <wsse:SecurityTokenReference>
                            <wsse:Reference URI="#the-cert"
                                ValueType="http://docs.oasis-open.org/...#X509v3"/>
                        </wsse:SecurityTokenReference>
                    </ds:KeyInfo>
                </ds:Signature>
                <wsu:Timestamp
                    wsu:Id="the-timestamp">
                    <wsu:Created>2007-01-01T00:00:00.000Z</wsu:Created>
                </wsu:Timestamp>
            </wss:Security>
        </soap:Header>
        <soap:Body wsu:Id="the-body">
            ...
        </soap:Body>
    </soap:Envelope>
                    
                

Notification consumer service
-----------------------------

To be able to receive certain notifications of linkID events, an
application ( or device issuer ) needs to subscribe to the linkID
Notification web service and setup a Notification consumer webservice
which linkID uses to send to. The notification service is implemented
according to the OASIS WS-Notification 1.3 specification.

In order to subscribe and unsubscribe for certain topic one can use the
provided SDK clients or an operator can manually add or remove
subscriptions using the linkID operator pages.

A notification message allways has the following format:

1.  Topic
2.  Destination : The destination for who this notification is destined.
    This if one consumer web service is intended for multiple
    applications. It will contain the applications name as known to
    linkID.
3.  Subject : linkID subject about who the notification handles
4.  Content : Specific content for the notification message

Following topics are currently available :

1.  urn:net:lin-k:safe-online:topic:user:remove
2.  urn:net:lin-k:safe-online:topic:user:unsubscribe

Authentication Web Application
==============================

> This chapter will give an overview of the functionality provided by
> the linkID authentication web application.

The linkID authentication web application is used to authentication
users via a web interface. In more detail the linkID authentication web
application allows you to:

-   Register new users. A new user is also required to directly register
    an initial authentication device.

-   Authenticate existing users via a device required by the requesting
    application.

-   Register new authentication devices.

-   Subscribe a user to an application if he hasn't done that yet.

-   Let the user confirm the usage of the identity attributes required
    by the requesting application.

-   Let the user fill in missing identity attributes as required by the
    requesting application.

-   Generate an authentication token according to the authentication
    protocol used by the requesting application.

The linkID authentication web application supports different
authentication protocols that can be used by the applications to
initiate a new user authentication process. The most important
authentication protocol is the SAML version 2.0 Web SSO profile with
HTTP POST binding and there is also support for OpenID Authentication
v2.0 protocol with support for OpenID Attribute Exchange v1.0 .

Single Sign-On
--------------

linkID provided configurable single sign-on functionality. It is the
linkID operator that decides which applications will provide single
sign-on functionality to each other. The operator does this by creating
application pools, grouping applications for single sign-on. Each such
pool has a configurable single sign-on timeout, defining the lifetime of
the actual authentication. The operator also configures for each
application if single sign-on is allowed.

If Single Sign On is used, the authentication web application will
firstly check if the application is allowed to use single sign-on. If it
finds a stored authentication for an application in a common application
pool as the application that is trying to log in, and that stored
authentication has not yet timed out, single sign-on will be allowed.
Off course the checks like device policy, application identity will
still be done. When no stored authentication was found, the user will
have to authenticate and this authentication will be stored for use of
other applications in the same application pool as this application.

Single Log-Out
--------------

As linkID provides single sign-on, single logout is also supported. The
linkID authentication web application supports different single logout
protocols that can be used. The most important one is the SAML version
2.0 Single Logout profile with HTTP Post binding.

If an application initiates a single logout process, following will
happen:

1.  The application will issue a logout request to the linkID
    authentication web application.

2.  The linkID authentication web application will, for each application
    pool the application is in, collect the applications that are stored
    having authenticated or made use of single sign-on for
    authentication.

3.  For each application in this collection, the linkID authentication
    web application will issue a logout request to that application.

4.  That application will receive this request, do the necessary logout
    steps for itself and return a logout response.

5.  After 2 previous steps have been repeated for each application, the
    linkID authentication web application will return a logout response
    to the application that issued the initial logout request.

6.  Upon receiving this response, the application can do the necessary
    logout steps for itself also and the single logout is finished.

Standard linkID attributes
==========================

> linkID has a default set of attributes configured in its core. Also
> certain devices like the Belgian eID card provide certain attributes.
> Below we list them and provide some more info on what to expect in
> those attributes.

Profile attributes
------------------

### profile.givenName

A single valued, STRING attribute containing the given name as specified
by the user.

### profile.familyName

A single valued, STRING attribute containing the family name as
specified by the user.

### profile.dob

A single valued, DATE attribute containing the date of birth as
specified by the user.

### profile.gender

A single valued, STRING attribute containing the gender as specified by
the user. Possible values are `MALE` and `FEMALE`.

### profile.language

A single valued, STRING attribute containing the language as specified
by the user. The value is specified in `ISO-639-1` format.

### profile.mobile

A single valued, STRING attribute containing the mobile as specified by
the user.

### profile.phone

A single valued, STRING attribute containing the phone as specified by
the user.

### profile.address

A compound attribute containing the user's specified addresses. The
compound consists of following members:

profile.address.street
:   STRING attribute containing the address street name.

profile.address.streetNumber
:   STRING attribute containing the address street number.

profile.address.streetBus
:   optional STRING attribute containing the address street bus.

profile.address.postalCode
:   STRING attribute containing the address postal code.

profile.address.city
:   STRING attribute containing the address city.

profile.address.country
:   STRING attribute containing the address country. The value is
    returned in `ISO-3166 alpha-2` format.

### profile.email

A compound attribute containing the user's email addresses. The compound
consists of following members:

profile.email.address
:   STRING attribute containing the email address

profile.email.confirmed
:   BOOLEAN attribute telling if the user has confirmed this email
    address.

Belgian eID card attributes
---------------------------

### device.beid.nrn

A single valued, STRING attribute containing the national registry
number of the user's eID card.

### device.beid.maskedNrn

A single valued, STRING attribute containing the hash of the national
registry number of the user's eID card.

### device.beid.surname

A single valued, STRING attribute containing the family name of the user
as specified on the eID card.

### device.beid.givenName

A single valued, STRING attribute containing the given name of the user
as specified on the eID card.

### device.beid.nationality

A single valued, STRING attribute containing the nationality of the user
as specified on the eID card.

### device.beid.placeOfBirth

A single valued, DATE attribute containing the place of birth of the
user as specified on the eID card.

### device.beid.dateOfBirth

A single valued, DATE attribute containing the place of birth of the
user as specified on the eID card.

### device.beid.gender

A single valued, DATE attribute containing the gender of the user as
specified on the eID card. Possible values are `MALE` and `FEMALE`.

### device.beid.streetAndNumber

A single valued, STRING attribute containing the street and number of
the user as specified on the eID card.

### device.beid.zip

A single valued, STRING attribute containing the postal code of the user
as specified on the eID card.

### device.beid.municipality

A single valued, STRING attribute containing the city of the user as
specified on the eID card.
