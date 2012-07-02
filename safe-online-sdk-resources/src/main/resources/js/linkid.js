/*
 * Widget to include a LinkID login modal window on a page.
 *
 * Widget structure based on: http://alexmarandon.com/articles/web_widget_jquery/
 * @author Stein Desmet
 */

// Bootstrap necessary JS/CSS needed for linkID Login
(function () {

    // get the URL of this script, so we can use it later to dynamically load other JS scripts from the linkid server this is hosted on
    var basePath;

    function findBasePathUsing(name) {
        if (basePath)
            return;

        var scripts = document.getElementsByTagName( 'script' );
        for (var i = scripts.length - 1; i >= 0; --i) {
            var src = scripts[i].src;
            var l = src.length;
            var length = name.length;
            if (src.substr( l - length ) == name) {
                // set a global propery here
                basePath = src.substr( 0, l - length );
            }
        }
    }

    findBasePathUsing( 'js/linkid-min.js' );
    findBasePathUsing( 'js/linkid.js' );
    if (!basePath) {
        basePath = "https://service.linkid.be/linkid-static/"
    }

    /******** Load Lazyload if not present *********/
    if (window.LazyLoad === undefined) {
        getScript( basePath + "js/lazyload-min.js", loadCSS );
    } else {
        loadCSS();
    }
    // Bootstrap function to load a script if LazyLoad is not present yet (performs callback when ready)
    function getScript(url, callback) {
        var script_tag = document.createElement( 'script' );
        script_tag.setAttribute( "type", "text/javascript" );
        script_tag.setAttribute( "src", url );
        if (script_tag.readyState) {
            script_tag.onreadystatechange = function () { // For old versions of IE
                if (this.readyState == 'complete' || this.readyState == 'loaded') {
                    callback();
                }
            };
        } else {
            script_tag.onload = callback;
        }
        // Try to find the head, otherwise default to the documentElement
        (document.getElementsByTagName( "head" )[0] || document.documentElement).appendChild( script_tag );
    }

    //Load css and perform callback when ready
    function loadCSS() {
        LazyLoad.css( basePath + "css/login-modal.css", loadJS );
    }

    //Load Javascript dependencies if not present (using LazyLoad). Callback to main when ready
    function loadJS() {
        var scriptsToLoad = [];
        if (window.jQuery === undefined) {
            scriptsToLoad.push( basePath + "js/jquery.js" );
        }
        if (window.colorbox === undefined) {
            scriptsToLoad.push( basePath + 'js/jquery.colorbox-min.js' );
        }
        if (scriptsToLoad.length > 0) {
            LazyLoad.js( scriptsToLoad, initializeLinkID );
        } else {
            initializeLinkID();
        }
    }

})(); // We call our anonymous function immediately

var cfg = {
    width:640,
    height:480,
    loginURL:"/startlogin",
    logoutURL:"/startlogout",
    linkURL:"/mobile_link"
};

/******** linkID Initialization function ********/
function bindLinkIDLogin() {

    //construct loginURL with necessary query parameters. See also net.link.safeonline.sdk.auth.servlet.InitiateLoginServlet
    var loginURL = $( this ).attr( "href" );
    if (loginURL == undefined || loginURL == "#")
        loginURL = cfg.loginURL;

    var return_uri = $( this ).attr( "data-completion-href" );
    if (return_uri == undefined || return_uri == "#")
        return_uri = window.location.pathname;
    loginURL = loginURL + (loginURL.indexOf( "?" ) > -1? "&": "?") + "return_uri=" + encodeURIComponent( return_uri );

    //attach login action. Type of login depends on login-mode (options: redirect to LinkID login, open a popupwindow, open a modal window with iFrame (default))
    var mode = $( this ).attr( "data-mode" );
    if (mode == "link" || mode == "redirect") {
        $( this ).click( function () {
            loginURL = loginURL + (loginURL.indexOf( "?" ) > -1? "&": "?") + "login_mode=redirect";
            window.location.replace( loginURL );
        } );
    } else if (mode == "popup" || mode == "popup_no_close") {
        $( this ).click( function () {
            $( this ).attr( "href", "#" );
            loginURL = loginURL + (loginURL.indexOf( "?" ) > -1? "&": "?") + ("login_mode=" + encodeURIComponent( mode ));
            var linkidWindow = window.open( loginURL, "LinkID-Login",
                    "toolbar=0, titlebar=0, menubar=0, location=0, status=0, directories=0, height=" + cfg.height + ",width=" + cfg.width );
            linkidWindow.focus();
        } );
    } else {
        if (mode == "framed_no_breakframe") {
            loginURL = loginURL + (loginURL.indexOf( "?" ) > -1? "&": "?") + "login_mode=framed_no_breakframe";
        } else { // default to frame
            loginURL = loginURL + (loginURL.indexOf( "?" ) > -1? "&": "?") + "login_mode=framed";
        }

        $( this ).colorbox( {
            iframe:true,
            fastIframe:false,
            innerWidth:cfg.width,
            innerHeight:cfg.height,
            href:loginURL,
            scrolling:false,
            overlayClose:false,
            opacity:0.85
        } );
    }
}

function bindLinkIDLogout() {

    var logoutURL = $( this ).attr( "href" );

    if (logoutURL == undefined || logoutURL == "#")
        logoutURL = cfg.logoutURL;

    var return_uri = $( this ).attr( "data-completion-href" );
    if (return_uri == undefined || return_uri == "#")
        return_uri = window.location.pathname;
    logoutURL = logoutURL + (logoutURL.indexOf( "?" ) > -1? "&": "?") + "return_uri=" + return_uri;

    $( this ).click( function () {
        window.location.replace( logoutURL );
    } );
}

function bindLinkIDMobileLinking() {

    var linkURL = $( this ).attr( "href" );
    if (linkURL == undefined || linkURL == "#")
        linkURL = cfg.linkURL;

    var mode = $( this ).attr( "data-mode" );
    if (mode == "link") {
        $( this ).click( function () {
            window.location.replace( loginURL );
        } );
    } else if (mode == "popup") {
        $( this ).click( function () {
            $( this ).attr( "href", "#" );
            var linkidWindow = window.open( loginURL, "LinkID-Link",
                    "toolbar=0, titlebar=0, menubar=0, location=0, status=0, directories=0, height=" + cfg.height + ",width=" + cfg.width );
            linkidWindow.focus();
        } );
    } else {
        $( this ).colorbox( {
            iframe:true,
            fastIframe:false,
            innerWidth:cfg.width,
            innerHeight:cfg.height,
            href:linkURL,
            scrolling:false,
            overlayClose:false,
            opacity:0.85
        } );
    }
}

/****** Attaches handlers to linkID links *******/
function initializeLinkID() {

    $( document ).ready( function ($) {

        // linkID Login
        $( ".linkid-login" ).each( bindLinkIDLogin );

        // linkID Logout
        $( ".linkid-logout" ).each( bindLinkIDLogout );

        // linkID Mobile linking
        $( ".linkid-link" ).each( bindLinkIDMobileLinking );
    } );
}
