postgresql foreign data wrapper メモ

Unixでコンパイル
cd /var/lib/pgsql/9.5
--ここにgitのフォルダコピー
ldap_fdw-master とする
cd ldap_fdw-master
--コンパイル
make
make installcheck
make install
--環境変数足りない「/bin/sh: ～～: command not found」とかでたら、環境変数を追加する。
export PATH="$PATH:/usr/pgsql-9.5/bin"
--Windowsのばあい、「Path」環境変数に直接追加する。


--コマンド

CREATE EXTENSION ldap_fdw;

CREATE SERVER ldap_myldap_server
FOREIGN DATA WRAPPER ldap_fdw
OPTIONS ( address '192.168.1.7', port '389');

CREATE USER MAPPING FOR current_user
SERVER ldap_myldap_server
OPTIONS (user_dn 'cn=Directory Manager', password 'Passw0rd');

CREATE FOREIGN TABLE ldap_people (
   dn text,
   object_body text
)
SERVER ldap_myldap_server
OPTIONS (base_dn 'dc=example,dc=com');



SELECT * FROM ldap_people;


 
 