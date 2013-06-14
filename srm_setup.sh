# Full SRM installation script
#
# Inputs for this script include:
#
#
#



# 1 - Bestman setup
# call the bestman setup script here

# 2 - Copy the esg2-sdnn1 certificate into /etc/grid-security/certificates

# 3 - Set the ports for globus (i.e. call esg-gridftp-restart)

# 4 - Obtain a certificate via myproxy-logon (using globus services)
# $GLOBUS_HOME/bin/myproxy-logon -s esg -l <username>
# NOTE: certificate must be for user "tomcat" (i.e. /tmp/x509uxxx where xxx is the tomcat user on the system)

# 5 - Set CLASSPATH for tomcat (to be used with Bestman and Axis)

# 6 - Write the srm.properties file (for front end)

# 7 - Create new schema in postgres
#
# file_id varchar(128),dataset_id varchar(128),timeStamp varchar(128), expiration varchar(128), bestmanNumber varchar(128),
# primary key (file_id,dataset_id));
#

# 8 - Pull esgf-stager from repo

# 9 - Deploy in tomcat

