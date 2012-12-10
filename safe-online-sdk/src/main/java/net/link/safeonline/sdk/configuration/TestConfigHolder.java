/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.configuration;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Locale;
import javax.security.auth.x500.X500Principal;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import net.link.util.common.DummyServletRequest;
import net.link.util.config.*;
import org.jetbrains.annotations.Nullable;
import org.joda.time.Duration;


/**
 * <h2>{@link TestConfigHolder}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Mar 24, 2009</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings("PublicField")
public class TestConfigHolder extends ConfigHolder {

    private static TestSDKConfig testConfig;

    /**
     * Only use this method when you're certain a TestConfigHolder has been set as the active config.
     *
     * @return The currently active config, cast to TestConfigHolder.
     */
    public static TestSDKConfig testConfig() {

        return testConfig;
    }

    public TestConfigHolder() {

        this( null, null );
    }

    public TestConfigHolder(@Nullable final String appBase, @Nullable final ServletContext servletContext) {

        this( appBase, null, servletContext );
    }

    public TestConfigHolder(final String appBase, @Nullable final AppConfig appConfig, final ServletContext servletContext) {

        super( TestSDKConfig.class, new SafeOnlineDefaultConfigFactory() {

            @Override
            protected ServletContext getServletContext() {

                return servletContext;
            }

            @Override
            protected ServletRequest getServletRequest() {

                return new DummyServletRequest() {
                    @Override
                    public Locale getLocale() {

                        return new Locale( "en" );
                    }

                    @Override
                    public String getContextPath() {

                        return "/";
                    }
                };
            }
        }, testConfig = new TestSDKConfig( appBase, appConfig ) );
    }

    public void install() {

        ConfigHolder.setGlobalConfigHolder( this );
    }

    public static class TestSDKConfig implements SDKConfig {

        private final TestWebConfig      web    = new TestWebConfig();
        private final TestProtocolConfig proto  = new TestProtocolConfig();
        private final TestLinkIDConfig   linkID = new TestLinkIDConfig();
        private final TestJAASConfig     jaas   = new TestJAASConfig();
        private final AppConfig appConfig;

        TestSDKConfig(String appBase, AppConfig appConfig) {

            this.appConfig = appConfig;

            web.appBase = appBase == null? "http://appBaseIsNull:0": appBase;
            web.appConfidentialBase = appBase == null? "https://appBaseIsNull:0": appBase;
        }

        @Override
        public TestWebConfig web() {

            return web;
        }

        @Override
        public TestProtocolConfig proto() {

            return proto;
        }

        @Override
        public TestLinkIDConfig linkID() {

            return linkID;
        }

        @Override
        public TestJAASConfig jaas() {

            return jaas;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends AppConfig> C app(Class<C> appConfigType) {

            return checkNotNull( (C) appConfig, "Can't use app config: it hasn't been set." );
        }

        public static class TestWebConfig implements WebConfig {

            public String appBase;
            public String appConfidentialBase;
            public String appPath;
            public String authBase;
            public String qrAuthURL;
            public String userBase;
            public String staticBase;
            public String wsBase;
            public String authWsBase;
            public String landingPath;

            @Override
            public String appBase() {

                return appBase;
            }

            @Override
            public String appConfidentialBase() {

                return appConfidentialBase;
            }

            @Override
            public String appPath() {

                return appPath;
            }

            @Override
            public String authBase() {

                return authBase;
            }

            @Override
            public String mobileAuthURL() {

                return qrAuthURL;
            }

            @Override
            public String staticBase() {

                return staticBase;
            }

            @Override
            public String userBase() {

                return userBase;
            }

            @Override
            public String wsBase() {

                return wsBase;
            }

            @Override
            public String authWsBase() {

                return authWsBase;
            }

            @Override
            public String landingPath() {

                return landingPath;
            }
        }


        public static class TestProtocolConfig implements ProtocolConfig {

            private final TestOpenIDProtocolConfig openid = new TestOpenIDProtocolConfig();
            private final TestSAMLProtocolConfig   saml   = new TestSAMLProtocolConfig();
            private final TestOAuthProtocolConfig  oauth  = new TestOAuthProtocolConfig();
            public Protocol defaultProtocol;
            public Duration maxTimeOffset;

            @Override
            public TestOpenIDProtocolConfig openid() {

                return openid;
            }

            @Override
            public TestSAMLProtocolConfig saml() {

                return saml;
            }

            @Override
            public OAuth2ProtocolConfig oauth2() {

                return oauth;
            }

            @Override
            public Protocol defaultProtocol() {

                return defaultProtocol;
            }

            @Override
            public Duration maxTimeOffset() {

                return maxTimeOffset;
            }

            public static class TestSAMLProtocolConfig implements SAMLProtocolConfig {

                public String      postBindingTemplate;
                public SAMLBinding binding;
                public String      relayState;

                @Override
                public String postBindingTemplate() {

                    return postBindingTemplate;
                }

                @Override
                public SAMLBinding binding() {

                    return binding;
                }

                @Override
                public String relayState() {

                    return relayState;
                }
            }


            public static class TestOpenIDProtocolConfig implements OpenIDProtocolConfig {

                public String realm;
                public String discoveryPath;

                @Override
                public String realm() {

                    return realm;
                }

                @Override
                public String discoveryPath() {

                    return discoveryPath;
                }
            }


            public static class TestOAuthProtocolConfig implements OAuth2ProtocolConfig {

                public String authorizationPath;
                public String tokenPath;
                public String validationPath;
                public String attributesPath;
                public String binding;
                public String clientSecret;
                public String clientId;

                @Override
                public String authorizationPath() {

                    return authorizationPath;
                }

                @Override
                public String tokenPath() {

                    return tokenPath;
                }

                @Override
                public String validationPath() {

                    return validationPath;
                }

                @Override
                public String attributesPath() {

                    return attributesPath;
                }

                @Override
                public String binding() {

                    return binding;
                }

                @Override
                public String clientSecret() {

                    return clientSecret;
                }

                @Override
                public String clientId() {

                    return clientId;
                }
            }
        }


        public static class TestLinkIDConfig implements LinkIDConfig {

            public TestAppLinkIDConfig app = new TestAppLinkIDConfig();
            public String authPath;
            public String logoutPath;
            public String logoutExitPath;
            public String theme;
            public Locale language;

            @Override
            public TestAppLinkIDConfig app() {

                return app;
            }

            @Override
            public String authPath() {

                return authPath;
            }

            @Override
            public String logoutPath() {

                return logoutPath;
            }

            @Override
            public String logoutExitPath() {

                return logoutExitPath;
            }

            @Override
            public String theme() {

                return theme;
            }

            @Override
            public Locale language() {

                return language;
            }

            public static class TestAppLinkIDConfig implements AppLinkIDConfig {

                public String name = "test-application";
                public KeyProvider   keyProvider;
                public X500Principal trustedDN;

                @Override
                public String name() {

                    return name;
                }

                @Override
                public KeyProvider keyProvider() {

                    return keyProvider;
                }

                @Override
                public X500Principal trustedDN() {

                    return trustedDN;
                }
            }
        }


        public static class TestJAASConfig implements JAASConfig {

            public String context;
            public String loginPath;

            public List<String> publicPaths;

            @Override
            public String context() {

                return context;
            }

            @Override
            public String loginPath() {

                return loginPath;
            }

            @Override
            public List<String> publicPaths() {

                return publicPaths;
            }
        }
    }
}
