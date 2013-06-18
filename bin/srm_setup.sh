#!/bin/bash
#
# Full SRM installation script
# 
# Inputs for this script include:
#
# - certificate(s) for the esg2-sdnn1 proxy (i.e. the proxy server)  
#
# - BESTMAN_HOME
#
#


# 1 - Bestman setup
# call the bestman setup script here

#if [[ "$1" = "" ]]; then
#  echo 'empty'
#fi

BESTMAN_PATH='/tmp'
HPSS_PROXY_CERTIFICATE_LOCATION=''

while getopts a:b:c: flag; do
  case $flag in 
    a)
      echo "-a used: $OPTARG";
      ;;
    b) 
      echo "-b used: $OPTARG"; BESTMAN_PATH="$OPTARG"
      ;;
    c) 
      echo "-c used: $OPTARG"; HPSS_PROXY_CERTIFICATE_LOCATION="$OPTARG"
      ;;
    ?)
      exit;
      ;;
  esac
done

echo 'bpath: ' $BESTMAN_PATH
echo 'hpss cert: ' $HPSS_PROXY_CERTIFICATE_LOCATION

#sh bestman_setup.sh -b $BESTMAN_PATH

# 2 - Copy the esg2-sdnn1 certificate into /etc/grid-security/certificates

if [[ -e $HPSS_PROXY_CERTIFICATE_LOCATION ]] ; then
  cp $HPSS_PROXY_CERTIFICATE_LOCATION/* /etc/grid-security/certificates
fi

# 3 - Set the ports for globus (i.e. call esg-gridftp-restart)
sh /usr/local/bin/esg-gridftp-restart

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

cd /usr/local/src
if [[ ! -d /usr/local/src/esgf-stager ]] ; then
  /usr/local/git/bin/git clone https://github.com/ESGF/esgf-stager.git
fi
cd esgf-stager
/usr/local/git/bin/git checkout devel-newStager
/usr/local/ant/bin/ant make_war

# 9 - Deploy in tomcat

