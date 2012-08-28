<?php

require_once('../_include.php');

        $as = new SimpleSAML_Auth_Simple('linkid-example');

?>

<html>
    <head>

    <!-- linkID login js for running the authentication process in framed/popup mode -->
    <script type="text/javascript" id="linkid-login-script" src="http://demo.linkid.be/linkid-static/js/linkid-min.js"></script>

    <h1>Example webapp</h1>
    </head>

    <body>


        <?php 
        
        if ($as->isAuthenticated()) {

            $authDataArray   = $as->getAuthDataArray();
            $userId          = $authDataArray['saml:sp:NameID']['Value'];
            $attributes      = $as->getAttributes();
            $authnStatements = $authDataArray['saml:sp:AuthnStatements'];
        ?>

        <p>
            <h2>Hello <?php print $authDataArray['saml:sp:NameID']['Value'] ?>  )!</h2>
        </p>

        <h3>Attributes</h3>
        <p>
          <?php print_r($as->getAttributes()); ?>
        </p>

        <h3>AuthenticationStatements</h3>

        <?php

          foreach ($authnStatements as $as) {
              print "Device: " . $as->getAuthnContext() . " @ " . $as->getAuthnInstant() . "<br/>";
          }

        ?>

        <!--

        Example accessing a compound member attribute:

        print "{$attributes['device.beid'][0]['device.beid.givenName'][0]}";

        -->    

        <a href="./logout.php">Logout</a>

        <?php
        
        } else { 
        
        ?>

        <p>
            <!-- The css class is read by the linkid.login.js for running the authentication process in a framed/popup mode -->
            <a href="./login.php" class="linkid-login">Login</a>
        </p>

        <?php } ?>

    </body>
</html>

