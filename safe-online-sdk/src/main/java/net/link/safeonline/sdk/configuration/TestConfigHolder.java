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
import net.link.safeonline.sdk.auth.protocol.Protocol;
import net.link.safeonline.sdk.auth.protocol.saml2.SAMLBinding;
import net.link.util.common.DummyServletRequest;
import net.link.util.config.*;
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
public class TestConfigHolder extends ConfigHolder<TestConfigHolder.TestSDKConfig> {

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

    public TestConfigHolder(final String appBase, final ServletContext servletContext) {

        this( appBase, null, servletContext );
    }

    public TestConfigHolder(final String appBase, final AppConfig appConfig, final ServletContext servletContext) {

        super( new SafeOnlineDefaultConfigFactory() {

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
                }, TestSDKConfig.class, testConfig = new TestSDKConfig( appBase, appConfig ) );
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

        public TestWebConfig web() {

            return web;
        }

        public TestProtocolConfig proto() {

            return proto;
        }

        public TestLinkIDConfig linkID() {

            return linkID;
        }

        public TestJAASConfig jaas() {

            return jaas;
        }

        @SuppressWarnings( { "unchecked" })
        public <C extends AppConfig> C app(Class<C> appConfigType) {

            return checkNotNull( (C) appConfig, "Can't use app config: it hasn't been set." );
        }

        public static class TestWebConfig implements WebConfig {

            public String appBase;
            public String appConfidentialBase;
            public String appPath;
            public String authBase;
            public String userBase;
            public String wsBase;
            public String landingPath;

            public String appBase() {

                return appBase;
            }

            public String appConfidentialBase() {

                return appConfidentialBase;
            }

            public String appPath() {

                return appPath;
            }

            public String authBase() {

                return authBase;
            }

            public String userBase() {

                return userBase;
            }

            public String wsBase() {

                return wsBase;
            }

            public String landingPath() {

                return landingPath;
            }
        }


        public static class TestProtocolConfig implements ProtocolConfig {

            private final TestOpenIDProtocolConfig openid = new TestOpenIDProtocolConfig();
            private final TestSAMLProtocolConfig   saml   = new TestSAMLProtocolConfig();
            public Protocol defaultProtocol;
            public Duration maxTimeOffset;

            public TestOpenIDProtocolConfig openID() {

                return openid;
            }

            public TestSAMLProtocolConfig saml() {

                return saml;
            }

            public Protocol defaultProtocol() {

                return defaultProtocol;
            }

            public Duration maxTimeOffset() {

                return maxTimeOffset;
            }

            public static class TestSAMLProtocolConfig implements SAMLProtocolConfig {

                public String      postBindingTemplate;
                public SAMLBinding binding;
                public String      relayState;
                public boolean     breakFrame;

                public String postBindingTemplate() {

                    return postBindingTemplate;
                }

                public SAMLBinding binding() {

                    return binding;
                }

                public String relayState() {

                    return relayState;
                }

                public Boolean breakFrame() {

                    return breakFrame;
                }
            }


            public static class TestOpenIDProtocolConfig implements OpenIDProtocolConfig {

                public String realm;
                public String discoveryPath;

                public String realm() {

                    return realm;
                }

                public String discoveryPath() {

                    return discoveryPath;
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

            public TestAppLinkIDConfig app() {

                return app;
            }

            public String authPath() {

                return authPath;
            }

            public String logoutPath() {

                return logoutPath;
            }

            public String logoutExitPath() {

                return logoutExitPath;
            }

            public String theme() {

                return theme;
            }

            public Locale language() {

                return language;
            }

            public static class TestAppLinkIDConfig implements AppLinkIDConfig {

                public String name = "test-application";
                public KeyProvider   keyProvider;
                public X500Principal trustedDN;

                public String name() {

                    return name;
                }

                public KeyProvider keyProvider() {

                    return keyProvider;
                }

                public X500Principal trustedDN() {

                    return trustedDN;
                }
            }
        }


        public static class TestJAASConfig implements JAASConfig {

            public String context;
            public String loginPath;

            public List<String> publicPaths;

            public String context() {

                return context;
            }

            public String loginPath() {

                return loginPath;
            }

            public List<String> publicPaths() {

                return publicPaths;
            }
        }
    }
}
