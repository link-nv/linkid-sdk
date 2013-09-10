<?php

require_once('../_include.php');

SimpleSAML_Session::getInstance()->doLogout();

header("Location: ./index.php");

?>
