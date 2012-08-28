<?php

require_once('../_include.php');

        $as = new SimpleSAML_Auth_Simple('linkid-example');

       if (!$as->isAuthenticated()) {

            // initiate linkID login
            $as->login(array(
                'saml:idp' => 'linkID',
                'ReturnTo' => 'http://localhost/linkid-example/index.php',
                // This parameter is optional for running the linkID authentication process in a popup/redirect mode.
                // Please refer to the linkID SDK Manual for more information on this.
                // 'DestinationParams' => '?login_mode=REDIRECT',
                'ErrorURL'  => 'http://localhost/linkid-example/error.php'
            ));

    } else {

header("Location: ./index.php");

    }
?>
