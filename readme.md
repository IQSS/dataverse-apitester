# Datavese API Tester

https://build.hmdc.harvard.edu:8443/job/dataverse-apitester/

mvn clean test -Dapitester.baseuri="http://apitest.dataverse.org" -Dtest=MainTest,SwordTest

mvn clean test -Dtest=MainTest,SwordTest

curl -X DELETE http://localhost:8080/api/admin/authenticatedUsers/saber
