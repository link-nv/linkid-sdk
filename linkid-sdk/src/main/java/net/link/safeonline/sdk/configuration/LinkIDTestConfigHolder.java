/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;
import javax.security.auth.x500.X500Principal;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import net.link.util.common.DummyServletRequest;
import net.link.util.config.AppConfig;
import net.link.util.config.ConfigHolder;
import net.link.util.config.KeyProvider;
import org.jetbrains.annotations.Nullable;
import org.joda.time.Duration;


/**
 * <h2>{@link LinkIDTestConfigHolder}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Mar 24, 2009</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings("PublicField")
public class LinkIDTestConfigHolder extends ConfigHolder {

    private static TestLinkIDSDKConfig testConfig;

    /**
     * Only use this method when you're certain a TestConfigHolder has been set as the active config.
     *
     * @return The currently active config, cast to TestConfigHolder.
     */
    public static TestLinkIDSDKConfig testConfig() {

        return testConfig;
    }

    public LinkIDTestConfigHolder() {

        this( null, null );
    }

    public LinkIDTestConfigHolder(@Nullable final String appBase, @Nullable final ServletContext servletContext) {

        this( appBase, null, servletContext );
    }

    public LinkIDTestConfigHolder(final String appBase, @Nullable final AppConfig appConfig, final ServletContext servletContext) {

        super( TestLinkIDSDKConfig.class, new LinkIDSDKDefaultConfigFactory() {

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
        }, testConfig = new TestLinkIDSDKConfig( appBase, appConfig ) );
    }

    public void install() {

        ConfigHolder.setGlobalConfigHolder( this );
    }

    public static class TestLinkIDSDKConfig implements LinkIDSDKConfig {

        private final TestLinkIDWebConfig      web    = new TestLinkIDWebConfig();
        private final TestLinkIDProtocolConfig proto  = new TestLinkIDProtocolConfig();
        private final TestLinkIDConfig         linkID = new TestLinkIDConfig();
        private final AppConfig appConfig;

        TestLinkIDSDKConfig(String appBase, AppConfig appConfig) {

            this.appConfig = appConfig;

            web.appBase = appBase == null? "http://appBaseIsNull:0": appBase;
            web.appConfidentialBase = appBase == null? "https://appBaseIsNull:0": appBase;
        }

        @Override
        public TestLinkIDWebConfig web() {

            return web;
        }

        @Override
        public TestLinkIDProtocolConfig proto() {

            return proto;
        }

        @Override
        public TestLinkIDConfig linkID() {

            return linkID;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends AppConfig> C app(Class<C> appConfigType) {

            return checkNotNull( (C) appConfig, "Can't use app config: it hasn't been set." );
        }

        public static class TestLinkIDWebConfig implements LinkIDWebConfig {

            public String appBase;
            public String appConfidentialBase;
            public String appPath;
            public String linkIDBase;

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
            public String linkIDBase() {

                return linkIDBase;
            }
        }


        public static class TestLinkIDProtocolConfig implements LinkIDProtocolConfig {

            public Duration maxTimeOffset;

            @Override
            public Duration maxTimeOffset() {

                return maxTimeOffset;
            }

        }


        public static class TestLinkIDConfig implements LinkIDConfig {

            public TestLinkIDAppConfig app = new TestLinkIDAppConfig();
            public Locale language;

            @Override
            public TestLinkIDAppConfig app() {

                return app;
            }

            @Override
            public Locale language() {

                return language;
            }

            public static class TestLinkIDAppConfig implements LinkIDAppConfig {

                public String name = "test-application";
                public KeyProvider   keyProvider;
                public X500Principal trustedDN;
                public String        username;
                public String        password;

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

                @Override
                public String username() {

                    return username;
                }

                @Override
                public String password() {

                    return password;
                }
            }
        }

    }
}
