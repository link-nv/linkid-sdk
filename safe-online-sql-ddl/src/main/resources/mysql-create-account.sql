-- Account initialisation script for MySQL SafeOnline database
-- <![CDATA[Usage: mysql -u root -p < mysql-create-account.sql ]]>
USE mysql;
DELETE FROM user WHERE User = 'safeonline';
DELETE FROM db WHERE User = 'safeonline';
INSERT INTO user (Host, User, Password)
 VALUES ('localhost', 'safeonline', PASSWORD('safeonline'));
INSERT INTO user (Host, User, Password) 
 VALUES ('%', 'safeonline', PASSWORD('safeonline'));
INSERT INTO db (Host, Db, User, Select_priv, Insert_priv, 
 Update_priv, Delete_priv, Create_priv, Drop_priv, 
 Alter_priv, Index_priv) 
 VALUES ('localhost', 'safeonline', 'safeonline', 
 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y');
INSERT INTO db (Host, Db, User, Select_priv, Insert_priv,
 Update_priv, Delete_priv, Create_priv, Drop_priv,
 Alter_priv, Index_priv) 
 VALUES ('%', 'safeonline', 'safeonline', 
 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'Y');
FLUSH PRIVILEGES;
