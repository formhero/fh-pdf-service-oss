# Set environment variables
export MONGODB_AUTHDB=admin
export MONGODB_CONFIGDB=pdf-service
export MONGODB_HOST=localhost
export MONGODB_PASSWORD=pdf-service
export MONGODB_USERNAME=pdf-service
export MONGODB_USESEEDLIST=false
export FORMHERO_ENVIRONMENT=test
export FORMHERO_ENVIRONMENT_OWNER=test

# Run PDF service with local logging
java -Xms1024M -Xmx3072M \
  -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider \
  -Dlog4j.configurationFile=./local-log4j2.xml \
  -jar ./target/fh-pdf-refinery.one-jar.jar


