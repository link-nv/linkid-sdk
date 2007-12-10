-- Account initialisation script for PostgreSQL SafeOnline database
-- <![CDATA[Usage: psql -u root < postgresql-create-account.sql ]]>
DROP USER safeonline;
CREATE USER safeonline PASSWORD 'safeonline';
GRANT ALL ON DATABASE safeonline TO safeonline;