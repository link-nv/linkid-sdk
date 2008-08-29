-- Account initialisation script for Oracle SafeOnline database
-- <![CDATA[Usage: sqlplus / as sysdba @oracle-create-account.sql ]]>
DROP USER safeonline CASCADE;
CREATE USER safeonline IDENTIFIED BY safeonline;
GRANT ALL PRIVILEGES to safeonline;