# From 19.04 to 19.12

## LDAP upgrade

With this release, custom new attributes are added to geOrchestra users and organisations in the LDAP, leveraging a custom, [dedicated schema](https://github.com/georchestra/georchestra/blob/master/ldap/docker-root/georchestraSchema.ldif).
As a result, the LDAP DIT should be upgraded with the provided [script](upgrade_ldap_from_19.04_to_19.12.ldif), which creates a new geOrchestra schema with the required objectClasses and attributes.

First of all, we recommend that you backup all entries:
```
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -b "ou=users,dc=georchestra,dc=org" > users.backup.ldif
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -b "ou=orgs,dc=georchestra,dc=org" > orgs.backup.ldif
ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -b "ou=roles,dc=georchestra,dc=org" > roles.backup.ldif
```

Once this is done, you can start adding the new schema:
```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/migrations/19.12/upgrade_ldap_from_19.04_to_19.12.ldif -O /tmp/upgrade_ldap_from_19.04_to_19.12.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/upgrade_ldap_from_19.04_to_19.12.ldif
```

Let's prepare a migration LDIF, for users:
```
ldapsearch -x -H ldap://localhost:389 -o ldif-wrap=no -b "ou=users,dc=georchestra,dc=org" dn |grep "^dn: uid=" | while read f ; do
    printf "$f\nchangetype:modify\nadd:objectClass\nobjectClass:georchestraUser\n\n" >> /tmp/modify.ldif
done
```
Then for orgs:
```
ldapsearch -x -H ldap://localhost:389 -o ldif-wrap=no -b "ou=orgs,dc=georchestra,dc=org" '(objectClass=organization)' dn |grep "^dn: o=" | while read f ; do
    printf "$f\nchangetype:modify\nadd:objectClass\nobjectClass:georchestraOrg\n\n" >> /tmp/modify.ldif
done
```
Check the generated `modify.ldif` file is correct. It should look like this:
```
dn: uid=aaaaaa,ou=users,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraUser

dn: uid=bbbbbb,ou=users,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraUser
.
.
.
dn: o=yyyyyy,ou=orgs,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraOrg

dn: o=zzzzzz,ou=orgs,dc=georchestra,dc=org
changetype:modify
add:objectClass
objectClass:georchestraOrg
```

Finally upgrade all entries:
```
ldapmodify -H ldap://localhost:389 -D "cn=admin,dc=georchestra,dc=org" -W -f /tmp/modify.ldif
```

If anything goes wrong during the upgrade process, you can rollback thanks to the above backup (always inserting users first, or the `memberOf` overlay won't work !).
