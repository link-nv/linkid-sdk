package net.link.safeonline.common;

public class SafeOnlineCookies {

    /*
     * Cookie used by the authentication webapp to determine the application to
     * provide a link to upon a timeout in the authentication webapp
     */
    public static final String APPLICATION_COOKIE   = "OLAS.auth.ApplicationCookie";

    /*
     * Cookie used by the authentication webapp to determine that we already
     * detected the timeout when we are on the timeout page.
     */
    public static final String TIMEOUT_COOKIE       = "OLAS.auth.TimeoutCookie";

    /*
     * Cookie used by the authentication webapp to determine we entered the
     * authentication webapp for the first time or not. First time we should off
     * course not timeout on an invalid session.
     */
    public static final String ENTRY_COOKIE         = "OLAS.auth.EntryCookie";

    /*
     * Cookie used by the SafeOnline webapps to help in detecting an application
     * level session timeout.
     */
    public static final String LOGIN_COOKIE         = "OLAS.login";

    /*
     * Cookie used by the authentication webapp language phase listener for
     * language settings.
     */
    public static final String AUTH_LANGUAGE_COOKIE = "OLAS.auth.language";

    /*
     * Cookie used by the SafeOnline webapps for language settings.
     */
    public static final String LANGUAGE_COOKIE      = "OLAS.language";

}
