Introduction
============

The Java SDK for linkID is a convenience library for providing
applications the ability to communicate with linkID services. Since
linkID has been built on standard protocols you are in fact free to
communicate with it directly using one of the protocols it supports.
This SDK will simply make the job easier on you by providing a type-safe
and complete interface to configuring, authenticating for and accessing
the identity services of linkID.

Currently, linkID supports three standard protocols for applications to
communicate with its services: SAML2, OpenID and OAuth 2.0. The SDK
provides a consistent API for using either protocol, allowing you to
effortlessly switch between them if desired. You are, however, strongly
recommended to use the more featureful and secure SAML2 protocol.

OpenID
------

The OpenID protocol is defined by an open standard endorsed by the OIDF
(OpenID Foundation). It is quickly gaining popularity among application
developers, mainly because it provides universal identity management in
a very lightweight protocol. Implementations exist for most development
platforms and have only very minimal requirements. For use with linkID,
however, the OpenID protocol has two main downsides that make it less
interesting than the alternative: security limitations and lack of
support for single logout. OpenID security relies mainly on trust in DNS
security and URL hierarchies. When using the Java SDK, an application
developer has the option of providing the SDK with the linkID operator's
SSL certificate. This allows the SDK to guarantee the authority of the
linkID server when the application and the linkID server are
communicating directly.

-   Does not support single-sign-out.

-   Does not yet support any non-essential linkID authentication
    features:

    -   Setting a non-default theme or language.

    -   Session Tracking

    -   ...

SAML2
-----

The SAML2 protocol is published and maintained by the OASIS organization
(Organization for the Advancement of Structured Information Standards).
It is an extensive and extensible XML-based authentication and
authorization standard.

The use of SAML2 for communication with linkID services is strongly
recommended since all linkID features are well supported through it.
When using the Java SDK, an application developer will need to generate
a key pair for his application. The private key should be known
exclusively to the application while the linkID operator will need a
trusted certificate for your public key. For additional security, the
application developer can also provide the SDK with certificates of the
linkID node, allowing it to perform additional verification.

OAuth 2
-------

TBD

Choosing a protocol
-------------------

By default, the linkID SDK will use the SAML2 protocol. It is the
preferred protocol because it is the most versatile and the most secure
of the options. So by not making a choice, you'll be safely using the
SAML2 protocol.

If your application is already strongly integrated with OpenID services
and you wish to add support for linkID to it, you may want to consider
our OpenID protocol. Take note of the limitations, however, and consider
whether it wouldn't make more sense to use linkID with SAML2 anyway,
considering that linkID is a secure identity provider.

SDK structure
=============

The linkID Java SDK is fully mavenized and available from our public
repository @ http://repo.linkid.be It consists several maven artifacts
listed below ( next to all the dependencies it has to third party
libraries ). The reason it was split up was to make the dependency hell
a little bit less hellish. Say for example you only want to use OpenID
and no web services, there is no need for you to have to cleanup
dependencies due to libraries such as OpenSAML, WSS4j, etc. messing your
own classpath up.

safe-online-sdk
:   This core linkID Java SDK Artifact. Depending on what
    functionality/protocols you wish to use you need to include one or
    more of the following artifacts along with this core SDK artifact.

safe-online-sdk-ws
:   Contains Web Service clients for linkID's application web services.

safe-online-sdk-saml2
:   Contains a protocol handler for the SAML v2.0 authentication
    protocol.

safe-online-sdk-openid
:   Contains a protocol handler for the OpenID authentication protocol.

safe-online-sdk-oauth2
:   Contains a protocol handler for the OAuth 2 authentication protocol.

So say for example you wish to use the SAML v2.0 protocol and have no
need for our web service clients, you'd add following dependencies to
your maven artifact:

~~~~ {.xml}
        <dependency>
            <groupId>net.lin-k.safe-online.client.sdk</groupId>
            <artifactId>safe-online-sdk</artifactId>
            <version>${linkid-sdk.version}</version>
        </dependency>
        <dependency>
            <groupId>net.lin-k.safe-online.client.sdk</groupId>
            <artifactId>safe-online-sdk-saml2</artifactId>
            <version>${linkid-sdk.version}</version>
        </dependency>
                
~~~~

Configuring the SDK
===================

There are several ways in which you can configure the linkID Java SDK
for use with your application. All configuration is accessed through and
documented in the `Config` interface (and those it references).

Application developers can either implement the `Config` interface
themselves or use the default implementation provided by the SDK.

Default Config Implementation
-----------------------------

The default implementation of the `Config` interface is the most
convenient way for application developers to configure their application
for use with the linkID services. Note, however, that the default
implementation currently only works when the application is running
inside a Java servlet container.

When using the default implementation, values for configuration
parameters are obtained from one of three locations. The search
locations have an order: If a value is not defind for the property in
one location, the next is tried.

Property File
:   The classpath is searched for a property file named `linkID.xml`
    (for XML-encoded properties) or `linkID.properties` (for plain
    properties). The former is used and the latter discarded if both
    exist. Since XML-encoded property files are validatable and specify
    their own text encoding, they are recommended over plain property
    files.

Servlet Context
:   If a property has no value defined in a property file then an init
    parameter on the servlet context of the active web application is
    searched for a value. These init parameters are commonly provided by
    use of `context-param` elements in the application's `web.xml`.

Defaults
:   At last, when a property had no value defined in either a property
    file or the servlet context, a preconfigured default can be used.
    The SDK provides default values for almost all properties.

Since the SDK provides sensible defaults for almost all properties, the
application developer can focus on customizing those properties that
matter to his application only.

Activating the Config Framework
-------------------------------

To allow the SDK to find the configuration implementation it is
necessary to activate it using a `ConfigHolder`.

Since your JVM may be running multiple applications at once or serving
multiple unrelated requests at once, the configuration interface is made
to bind itself to the active thread. Once bound, any calls to it will
operate on the thread-specfic configuration. To facilitate this for use
within servlet containers, the SDK provides a servlet filter,
`ConfigFilter`, which takes care of binding the correct config to the
active thread when a servlet request is initiated and unbinding the
config from the thread after the request has ended.

The simplest way for application developers to activate linkID's
configuration framework is by adding the `SDKConfigFilter`
implementation of the `ConfigFilter` class to their servlet
configuration.

If you do this configuration through `web.xml`, this is what it would
look like:

~~~~ {.xml}
    <filter>
        <filter-name>ConfigFilter</filter-name>
        <filter-class>net.link.safeonline.sdk.configuration.SDKConfigFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>ConfigFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
                
~~~~

Make sure the mapping comes early enough: The `ConfigFilter` must have
been activated before any SDK code is invoked.

Custom Config Implementation
----------------------------

The SDK's configuration framework was written with maximum
customizability and freedom to application developers in mind. There are
situation where the default implementation is inadequate: You may prefer
to provide your configuration values in a more type-safe way, your
application may require you to dynamically provide different
configuration values for certain properties depending on some external
state or your application may not be running inside a servlet container
at all.

To provide your own implementation of the SDK config, all you need to do
is implement the Config interface and the interfaces it references.

Application Config
------------------

The SDK's configuration framework also provides you with a way of
putting your own application's configuration that's unrelated to linkID
or the SDK together with the configuration of the SDK. This is an
entirely optional operation and solely exists for the possible benefit
of application developers.

To extend the SDK's configuration framework with your own application's
configuration properties, all you need to do is extend the AppConfig
interface with your own interface and add your properties to it. Here's
an example:

~~~~ {.java}
public interface MyAppConfig extends AppConfig {

    @Config.Property(required = true, unset = "16")
    Integer minimumAge();
}
                
~~~~

Extend `AppConfig`
:   All interfaces providing application configuration must extend the
    `AppConfig` interface. This interface marks them for use as
    application configuration and indicates the default group prefix
    that your configuration will be stored under (which is `app`).

Properties as methods
:   Configuration properties are methods in the interface. You should
    type them as strongly as possible by using the correct return type.
    When using the default configuration implementation, string values
    will be converted to the return type where possible.

Annotate property methods
:   Use the `@Property` annotation to indicate to the default
    configuration implementation that the value should be loaded from
    its configuration sources. The annotation also indicates whether or
    not it's acceptable when no value can be resolved for the property
    (not even a default value) by setting `required = true` and
    indicates what value to use as a default value when neither the
    property file or servlet context have been able to provide a value
    for the property by using `unset = "[default value]"`.

If necessary, you can group properties together into separate
configuration interfaces as is done with the SDK's configuration
interfaces. To create a group of properties, simply create a new
interface and annotate it with `@Group`. Put the group's properties in
this interface just like you would have in your `AppConfig` interface
extension. The `@Group` annotation requires an option, though, which is
the prefix under which the default configuration implementation should
look for the group's properties.

If, for example, you used the `@Group(prefix="users")` annotation on
your group interface, referenced it from your `AppConfig` interface, and
put a property method in it with the signature:
`@Property Integer maxUsers();`, then the default configuration
implementation would search your property file and servlet context for a
property named: `app.users.maxUsers`. If a value was found for this
property, the `String` value would be converted to an `Integer` instance
(by invoking `Integer`'s constructor that takes a `String` argument with
the value string) and returned to the code calling the method.

Activating your Custom Implementation or Config
-----------------------------------------------

Once you've created either (or both) a custom configuration
implementation and/or an application-specific configuration interface,
the next step to actually using them is by telling the SDK about them.

If your application runs inside a servlet container and you're using the
`ConfigFilter`, things are fairly easy for you: All you have to do is
extend the `SDKConfigFilter` class, create a public no-arg constructor
(also called default constructor), and call
`super( new SDKConfigHolder(...) )` from it. What goes on the dots
depends on whether you want to provide either a custom configuration
implementation, just an application configuration interface, or both.

Here's an example filter class that an application could use to activate
its own application configuration interface while still using the
default configuration implementation:

~~~~ {.java}
public class MyConfigFilter extends SDKConfigFilter {

    public MyConfigFilter() {

        super( new SDKConfigHolder( MyAppConfig.class ) );
    }
}
                
~~~~

Once you have this class, all you need to do is reference it instead of
the `SDKConfigFilter` in your servlet configuration. For example, to
activate the example filter above, the application developer might have
the following configuration in his `web.xml`:

~~~~ {.xml}
    <filter>
        <filter-name>ConfigFilter</filter-name>
        <filter-class>com.myapp.webapp.configuration.MyConfigFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>ConfigFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
                
~~~~

Now, the application's custom filter will load the `SDKConfigHolder`
with its custom `AppConfig` and activate the configuration framework on
the current thread for this setup. Any further request processing will
be able to access the configuration provided by the default
configuration implementation.

Configuration Overview
----------------------

Here's a brief overview of all properties exposed by the SDK. You can
customize the default values for each of these. For more information on
what each option does, review the JavaDoc of the config class that
declares the property.

  ------------------------------------------------------------------------
  Property Name                Default Value
  ---------------------------- -------------------------------------------
  `web.appBase`                *No default value*

  `web.appConfidentialBase`    *No default value*

  `web.appPath`                Use servlet request's context path.

  `web.userBase`               `https://demo.linkid.be/linkid-user`

  `web.authBase`               `https://demo.linkid.be/linkid-auth`

  `web.wsBase`                 `https://demo.linkid.be/linkid-ws`

  `web.landingPath`            Send protocol responses to the target URL.

  `proto.defaultProtocol`      `SAML2`

  `proto.maxTimeOffset`        5 Minutes (`300000` ms).

  `proto.openID.realm`         The application's confidential root URL.

  `proto.openID.discoveryPath` `/openid`

  `proto.saml.postBindingTempl A built-in template.
  ate`                         

  `proto.saml.binding`         `HTTP_POST`

  `proto.saml.relayState`      RelayState is not used.

  `proto.saml.breakFrame`      SAML2 response posted with "target=\_top"
                               for breaking out of an iframe.

  `linkID.authPath`            `/entry`

  `linkID.logoutPath`          `/logout`

  `linkID.logoutExitPath`      `/logoutexit`

  `linkID.theme`               The application's default theme.

  `linkID.language`            The language set by the current browser
                               request.

  `linkID.app.name`            *No default value*

  `linkID.app.keyStore`        `res:application.jks`

  `linkID.app.keyStorePass`    `secret`

  `linkID.app.keyEntryPass`    `secret`

  `linkID.app.keyEntryAlias`   Use the linkID application name as alias.

  `jaas.context`               `client-login`

  `jaas.loginPath`             Unauthenticated users are not redirected.

  `jaas.publicPaths`           No public paths.
  ------------------------------------------------------------------------

  : All SDK configuration parameters

Recommended Configuration
-------------------------

There's a small set of properties that are required but have no defaults
within the SDK. You're required to provide values for these properties
yourself in order to make use of linkID services. Here's an overview of
these properties and what they mean:

`linkID.app.name`
:   Technical name of the application to the linkID Operator.

`web.appBase`
:   The base URL where your applications are accessed. (Does not include
    the application's context path.)

`web.appConfidentialBase`
:   The base URL where confidential information for your applications
    are accessed or submitted. (Does not include the application's
    context path.)

Additionally, for some of the parameters that do have default values you
are strongly recommended to provide your own:

`web.userBase`; `web.authBase`; `web.wsBase`
:   These parameters tell the SDK what the location of the linkID
    operator's services is. The default values reference linkID's demo
    service. This will probably be fine during application development
    and testing, but you will need to reference a production site when
    your application goes live.

`web.landingPath`
:   This parameter contains the location where the linkID services will
    make the user post authentication responses. If unset (the default),
    these responses will be sent to the target URL (that's the URL the
    application wants the user to go to after the protocol response has
    been handled). If your application uses the `AuthnResponseFilter` on
    the target URL, the default value for this property will probably be
    fine. However, note that in this case your application (or at least,
    the target URL) *should* be running on HTTPS. The reason is that
    sensitive information is being submitted using these responses.
    Additionally, if your application is not on HTTPS, some browsers
    will warn the user about dangerous activity when submitting to an
    HTTP site (your application) from an HTTPS site (linkID). If you do
    not use the `AuthnResponseFilter` or want to run your application on
    HTTP, then you should specify a path in this parameter, relative to
    your application's context path, where linkID responses can be
    handled. This path should be HTTPS and can either have the
    `AuthnResponseFilter` or the `LoginServlet` bound to it.

`linkID.app.keyStore`; `linkID.app.keyStorePass`; `linkID.app.keyEntryAlias`; `linkID.app.keyEntryPass`
:   These parameters are used by the SDK when it needs to load key or
    certificate information out of the key store. By default, the SDK
    will look for a key store in the classpath named `application.jks`
    and unlock it (and if necessary, the key entry within it) with the
    password `secret`. If a key entry is needed and no alias is
    configured, the application's name (`linkID.app.name`) is used as
    the alias. The SDK uses the key store to obtain certificate
    information about the linkID server or the application's certificate
    and private key when building SAML2 requests or contacting linkID
    web services. You are recommended to build your own key store with
    your own sufficiently secure passwords.

For example, here's what a basic SDK configuration of an application
could look like, if defined by the default property file, `linkID.xml`:

~~~~ {.xml}
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">

<properties>

    <entry key="linkID.app.name">myapp</entry>
    <entry key="web.appBase">http://www.myapp.com</entry>
    <entry key="web.appConfidentialBase">https://www.myapp.com</entry>
    <entry key="web.landingPath">/linkID-login</entry>

    <entry key="web.userBase">https://my.linkid.be/linkid-user</entry>
    <entry key="web.authBase">https://my.linkid.be/linkid-auth</entry>
    <entry key="web.wsBase">https://my.linkid.be/linkid-ws</entry>

    <entry key="linkID.app.keyStorePass">SecretAndSecureKeyStorePassword</entry>
    <entry key="linkID.app.keyEntryAlias">myapp</entry>
    <entry key="linkID.app.keyEntryPass">SecretAndSecureKeyEntryPassword</entry>

</properties>
                
~~~~

If the developer prefers, he can also specify this configuration from
his servlet context. For example, an application developer might use the
following `web.xml` to deploy his application:

~~~~ {.xml}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
        "http://java.sun.com/dtd/web-app_2_3.dtd">

<web-app>

    <display-name>My Application</display-name>

    <!-- SDK Configuration -->
    <context-param>
        <param-name>linkID.app.name</param-name>
        <param-value>myapp</param-value>
    </context-param>
    <context-param>
        <param-name>web.appBase</param-name>
        <param-value>http://www.myapp.com</param-value>
    </context-param>
    <context-param>
        <param-name>web.appConfidentialBase</param-name>
        <param-value>https://www.myapp.com</param-value>
    </context-param>
    <context-param>
        <param-name>web.landingPath</param-name>
        <param-value>/linkID-login</param-value>
    </context-param>

    <context-param>
        <param-name>web.userBase</param-name>
        <param-value>https://my.linkid.be/linkid-user</param-value>
    </context-param>
    <context-param>
        <param-name>web.authBase</param-name>
        <param-value>https://my.linkid.be/linkid-auth</param-value>
    </context-param>
    <context-param>
        <param-name>web.wsBase</param-name>
        <param-value>https://my.linkid.be/linkid-ws</param-value>
    </context-param>

    <context-param>
        <param-name>linkID.app.keyStorePass</param-name>
        <param-value>SecretAndSecureKeyStorePassword</param-value>
    </context-param>
    <context-param>
        <param-name>linkID.app.keyEntryAlias</param-name>
        <param-value>myapp</param-value>
    </context-param>
    <context-param>
        <param-name>linkID.app.keyEntryPass</param-name>
        <param-value>SecretAndSecureKeyEntryPassword</param-value>
    </context-param>

    <!-- SDK Configuration Filter -->
    <filter>
        <filter-name>ConfigFilter</filter-name>
        <filter-class>net.link.safeonline.sdk.configuration.SDKConfigFilter</filter-class>
    </filter>

    <!-- Application's Web Framework -->
    <filter>
        <filter-name>WicketFilter</filter-name>
        <filter-class>org.apache.wicket.protocol.http.WicketFilter</filter-class>

        <init-param>
            <param-name>applicationClassName</param-name>
            <param-value>com.myapp.webapp.MyApplication</param-value>
        </init-param>
    </filter>

    <!-- Map filters to URLs -->
    <filter-mapping>
        <filter-name>ConfigFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    <filter-mapping>
        <filter-name>WicketFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <!-- SDK Response Servlets -->
    <servlet>
        <servlet-name>LoginServlet</servlet-name>
        <servlet-class>net.link.safeonline.sdk.auth.servlet.LoginServlet</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>LogoutServlet</servlet-name>
        <servlet-class>net.link.safeonline.sdk.auth.servlet.LogoutServlet</servlet-class>
    </servlet>

    <!-- Map servlets to URLs -->
    <servlet-mapping>
        <servlet-name>LoginServlet</servlet-name>
        <url-pattern>/linkID-login</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>LogoutServlet</servlet-name>
        <url-pattern>/linkID-logout</url-pattern>
    </servlet-mapping>

</web-app>
                
~~~~

It's good to note the main advantage of not putting the property values
in your `web.xml`: If they are defined in a property file that's in your
classpath, you can put the file somewhere in your servlet container's
configuration or lib directory, making it easily accessible while the
server is running. That means it's easy to update these values without
needing to rebuild or repackage the application. With this in mind, also
note that it's possible to divide parameters between both sources. You
may choose to define most parameters in a property file and leave the
static ones defined in your application's `web.xml`. As a last remark;
note that even if you have defined a parameter in your application's
`web.xml`, it's still possible to change it without repackaging the
application simply by adding it to your property file as well. When the
SDK goes looking for a value for the property and finds one in the
property file, it will not go on looking at the `web.xml`.

Keys and Certificates
---------------------

If you have decided to use the SAML2 protocol with the SDK for
communicating with linkID services, this chapter details another
required step in setting up your application for use with linkID. If
your application uses the OpenID protocol, this chapter is optional, but
still highly recommended.

For the linkID Java SDK, all required keys and certificates are obtained
from a single key store. To find this key store, the SDK queries the
parameter `linkID.app.keyStore` (default value `res:application.jks`).
With this default value, the SDK will load the resource
`application.jks` from the application's classpath if it exists.

`res:[classpath resource]`
:   Use this syntax to load the keystore from a resource in the
    classpath.

`url:[absolute url]`
:   Use this syntax to load the keystore from a resource found at a
    specified URL.

`file:[filesystem path]`
:   Use this syntax to load the keystore from a file found at the given
    path in the local filesystem.

`class:[keystore class]`
:   Use this syntax to load the keystore using a custom class that
    implements `LinkIDKeyStore`.

The key store file will be unlocked using the password provided by
`linkID.app.keyStorePass`. If the key entry is needed, it will be
obtained from the entry with the alias which is the value of
`linkID.app.keyEntryAlias`. The entry will be unlocked using the
password provided by `linkID.app.keyEntryPass`. The key entry will only
be needed when the application authenticates using the SAML2 protocol or
utilizes linkID's web services.

The application's private key entry and its certificate
:   This information is accessed by the SDK when it builds SAML2
    messages or webservice requests. The certificate is used to identify
    the application to the linkID service the message is sent to, and
    the key is used to sign the message. If you're going to use SAML2
    for authentication or any of the web services, add your
    application's key and certificate to the key store under the alias
    configured in the value of `linkID.app.keyEntryAlias`. You may omit
    this value if you decide to use your application's name
    (`linkID.app.name`) as the alias.

    To generate a key and certificate for your application, you can use
    the following `keytool` command (don't forget to change the alias
    `myapp` to the alias you wish to use):

    `
                                    keytool -keystore application.jks -genkey -alias myapp
                                `

    Once generated, you need to export your new certificate and send it
    to the linkID operator. That way, the operator can authorize your
    application access to his linkID services. To export your
    certificate, you can use the following `keytool` command (don't
    forget to change the alias `myapp` to the alias you wish to use):

    `
                                    keytool -keystore application.jks -export -alias myapp -file application.crt
                                `

    The certificate file (`application.crt`) is not secret. The key
    store file (`application.jks`), however, *is* secret. Make sure that
    only the most trusted parties within your organization gain access
    to the key store and the key store's passwords. *If there is ever
    suspicion of either of these being compromised, alert your linkID
    operator immediately and create your keystore anew with different
    passwords*.

The linkID service's SSL certificate
:   Whenever the SDK communicates directly with the linkID services (by
    use of web services), this certificate is the only certificate that
    will be trusted. If no SSL certificate is provided by the
    configuration, any server certificate is trusted blindly. Whether
    you use the OpenID or SAML2 protocol, you are strongly recommended
    to add this certificate to your key store under the alias
    `linkID_ssl`. To obtain the correct SSL certificate for the linkID
    service you'll be using, contact the linkID operator.

The linkID service certificate
:   Whenever the SDK validates messages sent from the linkID services,
    it checks whether these messages have been signed by the certificate
    that is also included in the message. This check is not perfect,
    however, since the message is validated using a certificate that was
    obtained along with the message itself. As an extra security check,
    the SDK will ask the configuration to provide it with the real and
    trusted certificate of the linkID service and make sure that it's
    the same as the certificate that came with the message. You are
    strongly recommended to add this certificate to your key store under
    the alias `linkID_service`. The service certificate can also become
    required in case you are using the SAML v2.0 HTTP-Redirect binding,
    in which case it will be used for validation of the signature on the
    authentication responses and logout request/responses. To obtain the
    correct service certificate for the linkID service you'll be using,
    the PEM encoded certificate can be downloaded at:
    `$linkID_host/linkid-auth/pki-cert.pem` or at:
    `$linkID_host/linkid-auth`. For rollover purposes it is possible to
    add multiple linkID service certificates to your keystore. To do
    this, they have to have an alias, prefixed with `linkID_service.`.

The linkID service root certificate
:   The linkID service will allways sign its authentication responses
    and logout request/responses. When using an authentication protocol
    like SAML v2.0 with HTTP-Post binding, the linkID service
    certificate chain will be embedded in the XML DSig's KeyInfo. This
    allows for local validation of those authentication respones and
    logout request/respones. Besides that it is possible to perform a
    local trust validation of this certificate chain by specifying the
    linkID service root certificate, which is strongly recommended. You
    can do this by adding this certificate to your key store under the
    alias `linkID_service_root`. Again, for rollover purposes, it is
    possible to add multiple linkID service root certificates to your
    keystore by adding them under an alias prefixed with
    `linkID_service_root.`. To obtain the correct service root
    certificate for the linkID service, you can download the PEM encoded
    certificate at: `$linkID_host/linkid-auth/pki-cert-root.pem`. or at:
    `$linkID_host/linkid-auth`.

Using the SDK
=============

Once set up, using the SDK to initiate authentication and logout
requests or manage user identity information becomes trivial. The SDK
allows application developers to get started quickly and easily by
providing defaults for any non-essential parameters and offering a clean
and consistent API for most interactions between applications and the
linkID services. If necessary, the SDK's API is versatile enough to
allow the developer the ability to drill deep into the inner workings of
the SDK and configure more esoteric parameters.

As mentioned before, there are two protocols with which the SDK can
initiate an authentication request. Whichever protocol you've opted for
in your configuration (the default is SAML2), the API usage to initiate
authentication remains identical. This also means that it's nearly
seamless to switch between protocols should you desire to do so.

The API provides two main methods of initiating authentication: Via a
filter or via a utility method. We will detail each method and the ways
in which you can customize their operation. If necessary, you could also
bypass these convenience methods and construct the requests yourself.
Unless you're doing something very complex and have some very good
reasons for working around the SDK, however, this is inadvisable.

Authentication Filter
---------------------

The authentication filters provided by the SDK allow developers to mark
parts (or all) of their application as "requiring authentication". Users
that make a request on any URL that the filter is mapped to will cause
it to check whether the user is currently authenticated. If the user is
not yet authenticated the filter will intervene and send the user to
linkID for authentication. After the user has authenticated himself and
is returned to the URL he was trying to access, the filter will trigger
again, find the user to be authenticated and let him through to your
application. Your application can then rely on the guarantee that no
requests will be made to those URLs by unauthenticated users.

`AuthnRequestFilter`
:   This is the filter that verifies whether the user requesting the URL
    has been authenticated. It does this by searching the HTTP session
    for linkID credentials. If none are found, this filter builds an
    authentication request for the linkID service and makes the user's
    browser submit that request to your linkID operator. The user is not
    allowed to pass into your application at this time.

    If, during the search of linkID credentials on the session, valid
    credentials are found, the filter lets the user pass and your
    application's code is executed for the request, as normal.

`AuthnResponseFilter`
:   This filter searches each request for a linkID authentication
    response. If it finds one, the filter handles the response by
    delegating it to the appropriate handler for the protocol. If the
    response is found valid, the filter sets (or updates) the linkID
    credentials on the user's HTTP session.

Generally, you'll want to bind both these filters to the same URL
pattern. By default, the request filter will tell linkID to send the
user back to the URL he tried to access once the authentication process
is complete. That means the response filter will need to be present on
that same URL in order for linkID's authentication response to be
processed and the HTTP session updated. Also note that if both filters
are active on a certain URL, you need to make sure the response filter
is activated before the request filter. If not, when a user comes back
from linkID with an authentication response, the request filter might
abort that request and send the user to linkID instead of letting the
response filter handle the linkID response in the user's request. The
user will be unable to enter the application.

Authentication Utilities
------------------------

You can also manually initiate the authentication request with the
linkID services. This makes most sense for applications that have a
"login"-type of button or to activate the authentication process
manually from a web framework.

To manually initiate an authentication request with linkID, invoke one
of `AuthenticationUtils`' `login` methods. There are several methods
that take a different level of additional arguments. Eventually, they
all do the same, but the simpler ones use default (or configured) values
as much as possible. Refer to the JavaDoc in this class for full details
on how you can use the methods. To customize the parameters used to
create the authentication request, you can build your own
`AuthenticationContext` object and pass it along. Refer to the JavaDoc
of this class for information on what can be customized.

Once the user has completed his authentication with the linkID services,
he will be returned to your application. The URL where the user will be
posting his authentication response is determined by the
`web.landingPath` parameter. If this parameter is not filled in and the
request was manually initiated using the authentication utilities, the
user will submit the authentication response to the application's root.
We therefore recommend application developers provide a custom
`web.landingPath` and install either the SDK's `LoginServlet` at that
path or the `AuthnResponseFilter`. After the user's response has been
handled and found valid, the user will be redirected to the target URL
that can be specified when manually initiating the authentication
request. The user won't notice much of all this processing and
redirecting: He will simply see his browser showing him the linkID
services first, and after authenticating, the site at your application's
target URL.

The authentication utilities also provide a second method: the `logout`
method. When you invoke this method, the SDK will build and make the
user submit a single-sign-on logout (also referred to as
single-sign-out) request. Such a request only makes sense if your
application uses linkID's single-sign-on services together with other
applications to provide a seamless interaction between your and these
other applications. Using single-sign-on the user must authenticate
himself only once, after which he will be allowed access without the
need for re-authentication to all applications that have agreed to work
together in a single-sign-on "pool". Using this single-sign-out request,
the application can tell the linkID services that the user wishes to log
himself out. Upon such a request, linkID will do its best to notify each
and every application the user has used since he last signed himself in.
All notified applications will do their best to log the user out of
their own respective application sessions. This guarantees that if the
user leaves his workstation, no-one else can take over his browser and
try to gain access to applications using the still-active linkID
single-sign-on session.

To support single-sign-out, an application must bind the `LogoutServlet`
(or a custom implementation of the `AbstractLogoutServlet`) on a certain
URL and pass that URL to the linkID operator. The operator sets the URL
in its configuration of your application so that it knows where to send
users to when these users initiate single-sign-out from another
application.

Integrating the linkID login process
------------------------------------

In order to integrate the linkID authentication process in a seamless
and uniform way across applications, the linkID SDK provides a number of
options.

First, the previously mentioned `AuthenticationContext` has a parameter
`loginmode` allowing you to manually specify how you will display the
login process in your application (e.g. inside a popup window, embedded
in the page, etc.). Based on the value of this parameter, the linkID
platform will take a number of decisions on how to display the
authentication process:

Redirect
:   When the authentication process is started, the user leaves the
    original page and is redirected to the normal linkID authentication
    application pages. When the authentication process is complete, the
    user will be redirected back to the original page. This is the
    default if not specified.

Popup
:   This indicates to linkID that the authentication process will take
    place in a popup window. LinkID will use a custom CSS style to
    display the login process in a popup window. Note that this window
    is required to have a size of 640x480 pixels. Furthermore, upon
    completion of the authentication process, the `LoginServlet` landing
    servlet will not issue a redirect to the user (as is the case in the
    previous mode), but instead generate a special response page
    containing javascript which causes the popup window to close and the
    parent page (the main page) to refresh.

Popup\_no\_close
:   The same as before, but the `LoginServlet` landing servlet will not
    close the popup window, and issue the normal redirect.

Furthermore, two complementary components are available to allow you to
quickly add a linkID login procedure to your pages. More precisely,
there is a client-side JavaScript widget allowing you to quickly add a
linkID login link to your pages, and a server-side servlet to handle
initiating the login process.

The JavaScript widget is called `linkid.js` and is provided both in the
SDK and hosted on the linkID servers. It allows you to quickly add a
LinkID login link or button to any web page by adding an anchor tag with
a certain class. The widget also offers a number of configuration
options allowing you to style the login process to your needs (for
example, open the login window in a popup or in a modal window).

To use the widget, simply import the JavaScript in the head of your html
document, and once you have done this you can create a login link using
the `<a href="#" class="linkid-login">Login!</a>` element as illustrated
below:

~~~~ {.xml}
                   <html>
                       <head>
                           <title>My LinkID Login Page</title>
                           <script type="text/javascript" id="linkid-login-script"
                                    src="https://demo.linkid.be/linkid-static/js/linkid.js" />
                       </head>
                       <body>
                           <a href="#" class="linkid-login">My first linkID login!</a>
                       </body>
                   </html>
               
~~~~

This particular snippet will create a link which will start the linkID
authentication process in an iframe, shown in a modal window. The
configuraton options will be discussed later on.

The widget is currently hosted on
`http://<linkidinstance>/linkid-static/js/linkid.js"`. Minified versions
are also available (` linkid-min.js`).

It should be noted that the authentication request (be it a SAML2 or
OpenID request) naturally needs to be generated server-side in your
application. Thus, the login widget cannot work on its own and needs a
server side component which creates the authentication context and
request, and starts the authentication process by calling `login` on
`AuthenticationUtils`.

You can manually create such a component (for example, a servlet), as
discussed in the previous sections, using the authentication utils.
However, if you do not need custom functionality a convenience servlet
is already provided for you in the SDK which does just this:
`InitiateLoginServlet`. Simply add the following code to your `web.xml`
in order to add it tou your project:

~~~~ {.xml}
                    <servlet>
                        <servlet-name>AutoLoginServlet</servlet-name>
                        <servlet-class>net.link.safeonline.sdk.auth.servlet.InitiateLoginServlet</servlet-class>
                    </servlet>
                    <servlet-mapping>
                        <servlet-name>AutoLoginServlet</servlet-name>
                        <url-pattern>/startlogin</url-pattern>
                    </servlet-mapping>
               
~~~~

Navigating to the servlet's URL will cause the authentication procedure
to be started on linkID.

Both the login widget and login servlet have a number of parameters
which can be set. The `InitiateLoginServlet` accepts the following query
parameters:

login\_mode
:   The type of login which will be set on the `AuthenticationContext`
    (see earlier). Possible values are: 'redirect', 'popup' (see also
    the `LoginMode` and `AuthenticationContext` classes in the SDK).
    Defaults to redirect.

return\_uri
:   The URL to return to upon completion of the authentication process.
    See `AuthenticationContext`

In order to configure the login widget, the following attributes can be
set on each anchor tag containing the 'linkid-login' class:

href
:   Sets the URL for `InitiateLoginServlet` or any other similar
    component which starts the authentication process. Defaults to
    `/startlogin` if not set or set to "\#".

login-mode
:   Sets the login mode and adds the `login_mode` query parameter to the
    login URL. Possible values are: 'redirect', 'popup',
    'popup\_no\_close' (default is popup). If the value is 'popup', a
    popup will be opened.

redirect-to-on-complete
:   Specifies the URL to return to after the authentication process is
    complete (sets the return\_uri query parameter for the
    `InitiateLoginServlet`). Defaults to the current page.

Thus, using both the JavaScript login widget and the
`InitiateLoginServlet` you can easily and quickly add a LinkID login to
your application.

Identity Management Services
----------------------------

The linkID service provides application developers with a set of web
services it can use to access and manage the identity of its users. The
SDK provides convenience methods for making the communication with these
services simple and trivial.

Several web services are available to applications, however, this
section will document only those that are directly useful to
applications. The other web services are either used internally by the
SDK or provide more advanced and esoteric use-cases. Some of these are
documented in later chapters while others are purely documented by the
JavaDoc and relevant WSDL files. Should you need any of these other web
services, contact your linkID operator for more information.

Attribute Service
:   The attribute service is a web service provided by linkID which
    allows applications to look up the current values for user
    attributes. Applications can only access attributes that the user
    has previously approved access to for the application.

Data Service
:   The data service allows applications to update the value of a user's
    linkID attributes. This service can only be used on attributes are
    provided to the user by the application. Contact the linkID operator
    if you wish to provide certain attributes.

Identity Mapping Service
:   The identity service provides a way for applications to identify
    users based on the current value of one of their attributes. Using
    this service, it becomes possible to search for users based on
    attribute values.

Accessing one of these services is trivial thanks to the SDK's API. The
`LinkIDServiceFactory` class has methods that allow an application to
easily construct a proxy object. There are, again, similar methods that
take several degrees of arguments. The no-arg methods simply use the
SDK's configuration to obtain all their information. It is recommended
that you use these no-arg methods whenever possible. The proxy objects
(also referred to as "client objects"), will transparently build web
service requests for methods invoked on it, query the relevant linkID
web service, and yield the result to you as return value. Note that
method invocations on a client object returned from this factory can
take some time to finish since a lot of network activity is happening in
the background. Invoking these methods may stall your application
momentarily.

Using the SDK from Wicket
=========================

Wicket is a Java web framework that's been gaining in popularity. The
key difference between Wicket and ordinary Java web frameworks is that
Wicket is an entirely stateful framework, it allows the web developer to
write a fully type-safe user interface, and it forces a strong and clear
separation between markup and logic. Each of these factors allow
application developers to build more maintainable and *testable*
web-based user interfaces.

The linkID Java SDK provides several convenience components that
integrate linkID services nicely into wicket. The `LinkIDApplication`
class is a base class for wicket applications that wish to make use of
these convenience integrations. If your application's class extends this
class instead of the `WebApplication` class, it will be able to use
`LinkIDApplicationPage` as the base page for its web pages. In doing so,
web pages will receive notifications when newly authenticated users land
on them by means of a call to `onLinkIDAuthenticated()`.

Additionally, application developers can now annotate their components
with the `@RequireLogin` annotation to indicate that the component may
only be rendered when the user has already been authenticated by linkID.
Another annotation, `@ForceLogout`, can be set on a page to indicate
that when users arrive on the page while being authenticated, their
linkID credentials should be wiped and wicket session should be logged
out of.

Whether a wicket developer chooses to use the `LinkIDApplication` base
class or not, he always has the ability to use the `LinkIDLoginLink` and
`LinkIDLogoutLink` components on his page. These components are
effectively links that invoke `AuthenticationUtils`' `login` and
`logout` methods, respectively. The components, like many other SDK
classes, can be extended (as new classes or anonymous inner classes) to
customize the parameters used to initiate these login and logout
requests with linkID.

Another provided component is the `LinkIDJavaScriptLoginLink`. It
provides the same functionality as `LinkIDLoginLink`, but works in
conjunction with the JavaScript login widget mentioned previously. If
specified, the component will automatically add the `linkid.js` widget
to the page's header. Note that it is not required to add the
`InitiateLoginServlet` to your project, as this component also serves as
the target for the login widget, and invokes `AuthenticationUtils`'
`login` by itself.

As a wrapper for the SDK's `LoginManager` utility methods, the wicket
SDK provides `LinkIDWicketUtils`. This handy utility class provides the
wicket developer with easy access to linkID credentials and attributes
stored on the HTTP session.

Revision history
================

  Date               Author             Description
  ------------------ ------------------ ------------------------------------
  29 Sep 2010        Maarten Billemont  Initial version.
  15 Nov 2012        Wim Vandenhaute    Update for Java SDK split up.

  : Revision history


