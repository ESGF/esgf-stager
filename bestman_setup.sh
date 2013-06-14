#BESTMAN INSTALLATION SCRIPT
#
# The following are required inputs (given by user in the installation procedure)
#
# For bestman2.rc (with examples on ORNL esgf node)
# - ReplicaQualityStorageMB (i.e. BESTMAN_CACHE_LOCATION) - lustre/esgfs/SRMTemp
#   ReplicaQualityStorageMB=[20000]path=/lustre/esgfs/SRMTemp;
#
# - CustodialQualityStorageMB (i.e. Plugin classes) -
#   CustodialQualityStorageMB=[0]path=&type=gov.ornl.srm.hpss.OrnlHpss&jarFile=OrnlHpss.jar&host=esg.ccs.ornl.gov&conf=ornl.ini
#
# - Bestman operational ports
#   publicPort=46789
#   securePort=46790
#
# - Host certificate location
#   CertFileName=/etc/grid-security/hostcert.pem
#   KeyFileName=/etc/grid-security/hostkey.pem
#
# - Grid mapfile name
#   GridMapFileName=/etc/grid-security/grid-mapfile
#
# For bestman (in BESTMAN_HOME/etc)
# - SRM_HOME - same as BESTMAN_HOME
# - BESTMAN_SYSCONF- BESTMAN_HOME/etc/bestman2
# - JAVA_HOME - /usr/local/java
# - BESTMAN_LIB - BESTMAN_HOME/lib
# - X509_CERT_DIR - /etc/grid-security/certificates
# - BESTMAN2_CONF - BESTMAN_HOME/conf/bestman2.rc
# - BESTMAN2_SERVER_LIB -
# - BESTMAN2_CLIENT_LIB -
#



# 1 - Get bestman

# 2 - untar into /usr/local and set the BESTMAN2_HOME variable

# 3 - run configure in BESTMAN2_HOME/setup

# 4 - set the configuration parameters in BESTMAN2_HOME/conf/bestman2.rc and BESTMAN2_HOME/etc/bestman2 using the inputs

# 5 - write grid-mapfile and save to $GridMapFileName (see above)

# 6 - start bestman
