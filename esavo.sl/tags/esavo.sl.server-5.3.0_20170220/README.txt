This project is a template for creating TAP server layers.

Steps:

1. Download source code to a temporary directory

svn co https://satscm.esac.esa.int/svnvo/esavo.sl/trunk/server tmpdir

2. Create your project dir

mkdir myProjectDir

3. Copy all files but '.svn' files

a) with rsync:

rsync -av --exclude=".svn" tmpdir myProjectDir

b) with cp:

cp -R tmpdir/* myProjectDir
cd myProjectDir
find . -name ".svn" | xargs rm -rf

4. Update build.properties.xxx files

NOTE: in order to set password properties you can use either a server to provide passwords or a file.
ant+build.xml will try to load 'build.properties.pwds.<environment>' file. After that,
if the property 'pw.server.host' is not empty, passwords are loaded from that machine.

a) password server: you must set the follwing properties
pw.server.host=
pw.server.username=
pw.server.keyfile=

b) file(s) (these files MUST NOT BE IN SVN):
Create a file named 'build.properties.pwds.<environment>' (this file is used by ant+build.xml)

ant+build.xml will use these files to set passwords.
These files are loaded after the environment properties file is loaded.

At least, you must define the following passwords (based on the corresponding users you have defined):
db.pwd=
db.jobs.sync.pwd=
db.jobs.async.pwd=
db.management.pwd=
ldap.pwd=



5. ant deploy local,op,int ...

5.1. ant deploy local will create the following directories
build
WebContent

6. Create an eclipse project (Dynamic Web Project)
6.1 add src/java & test as source folders
6.2 output folder: 'build/classes'
6.3 context-root: tomcat context for your tap: e.g. 'tap-local', '<your_project>-tap', etc.
6.4 do not generate web.xml
6.5 check/add all jars in 'lib' directory

7. DB update:
7.1 uws_schema
Execute sql at:
https://satscm.esac.esa.int/svnvo/esavo.uws/trunk/setup/db_schema/
-uws_tables.sql
-notifications.sql
-db_user_usage.sql
-db_table_usage.sql

https://satscm.esac.esa.int/svnvo/esavo.uws/trunk/setup/db_schema/share/
-share.sql

7.2 tap_schema
https://satscm.esac.esa.int/svnvo/esavo.tap/trunk/src/java/esavo/tap/sql/
-tap_create.sql
-tap_insert.sql

https://satscm.esac.esa.int/svnvo/esavo.tap/trunk/setup
-tap_view.sql
-tap_functions.sql

8. Test your app
8.1 In ecipse, run your application on your server (right click in your project, run on server):
http://localhost:8080/<context>/tap/tables


9. Create an 'admin' user for TAP
9.1 execute a login (LDAP account)
curl -k -c cookies.txt -X POST -d username=USERNAME -d password=PASSWORD -L "http://localhost:8080/<context>/login"
9.2 Edit db uws2_schema.owners, set 'roles' = '1'

10. Publish your tables
(You must be an administrator)
10.1 Login
curl -k -c cookies.txt -X POST -d username=USERNAME -d password=PASSWORD -L "http://localhost:8080/<context>/login"

10.2 publish your table(s)
curl -k -b cookies.txt "http://localhost:8080/<context>/tap/tables?tap_publish=create&tap_schema=tap_schema&tables=db_schema.db_table_name,db_schema2.db_table_name2..."

10.3 make your table(s) public:
curl -k -b cookies.txt "http://localhost:8080/<context>/tap/tables?tap_publish=public&tap_schema=tap_schema&tables=db_schema.db_table_name,db_schema2.db_table_name2..."

10.4 Logout
curl -k -b cookies.txt -X POST -d -L "http://localhost:8080/<context>/logout"

10.5 Tunning your published tables


11. Import your project to svn
11.1 Be sure you do not have any '.svn' file from esavo.sl.server example
11.2 Be sure you do not import your password files (e.g. mv build.properties.pwds.* /tmp)
11.3 Import to svn
	Go to parent directory: cd ..
	svn import <your_project_dir> https://satscm.esac.esa.int/<svn_dst_path> -m "Initial import"
	(restore your passwords files: cd <your_project_dir>; mv /tmp/build.properties.pwds.* .)
	
	Note: in case you do not have the suitable svn path:
	svn mkdir https://satscm.esac.esa.int/<svn_path> -m "Created initial TAP directory"



EXAMPLES:

curl "http://localhost:8080/<context>/tap/sync?LANG=ADQL&REQUEST=doQuery&QUERY=select+top+5+*+from+public.my_table"




