#!/bin/bash
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




export BESTMAN_PARENT=/usr/local
export BESTMAN_DIST=bestman2-2.3.0.tar.gz
export BESTMAN_URL=https://codeforge.lbl.gov/frs/download.php/402

while getopts b: option; do
   case $option in
       b)  
           export BESTMAN_PARENT=$OPTARG
           ;;
       ?)  
           echo "invalid option: $flag"
           exit 1
           ;;
       
   esac
done

# 1 - Get bestman
cd /tmp
wget ${BESTMAN_URL}/${BESTMAN_DIST}

# 2 - untar into /usr/local and set the BESTMAN2_HOME variable
mkdir -p ${BESTMAN_PARENT}
cd ${BESTMAN_PARENT}
tar zxf /tmp/${BESTMAN_DIST}

# 3 - run configure in BESTMAN2_HOME/setup
cd ${BESTMAN_PARENT}/bestman2/setup

./configure

# 4 - set the configuration parameters in BESTMAN2_HOME/conf/bestman2.rc and BESTMAN2_HOME/etc/bestman2 using the inputs
# TODO: Need to identify the parameters to be customized from inputs
cat - > $BESTMAN_HOME/conf/bestman2.rc <<EOF
###########################################################
###########################################################
###########################################################
###########################################################
# Below is ONLY for BeStMan server INTERNAL definitions
###########################################################
# BeStMan server GATEWAY mode configuration
# installed on Tue Dec  6 15:28:36 PST 2011 
# administrative guide on http://sdm.lbl.gov/bestman
###########################################################
###########################################################
EventLogLocation=/var/log
# Modify eventLogLevel to adjust logging level (DEBUG | INFO)
eventLogLevel=INFO
###########################################################
#F# ReplicaQualityStorageMB=[[REPLICA_STORAGE_SIZE]]path=REPLICA_STORAGE_PATH;
#F# NOTE: If you have more paths to support, 
#F#        add additional ones with semi-colon ";" as seperator
#F#        e.g. ReplicaQualityStorageMB=[3100]path=/tmp/cache;[200]path=/scratch/cache
#F#
#F# OutputQualityStorageMB=[[OUTPUT_STORAGE_SIZE]]path=OUTPUT_STORAGE_PATH;
#F# NOTE: If you have more paths to support, 
#F#        add additional ones with semi-colon ";" as seperator
#F#        e.g. OutputQualityStorageMB=[3100]path=/tmp/cache/d;[200]path=/scratch/cache/d
#F#
#F# CustodialQualityStorageMB=[[CUSTODIAL_STORAGE_SIZE]]path=CUSTODIAL_STORAGE_PATH;
#F# NOTE: If you have more paths to support, 
#F#        add additional ones with semi-colon ";" as seperator
#F#        e.g. CustodialQualityStorageMB=[3100]path=/tmp/cache/p;[200]path=/scratch/cache/p
###########################################################
# Provide blocking for user access to the list of the local directories
## default: /;/etc;/var
## All definitions will include the default blocked list
## e.g. localPathListToBlock=/;/etc;/var 
### localPathListToBlock=BLOCKEDPATHS
###########################################################
# Provide permission for user access to the list of the local directories
## If a path is listed on both blocked and allowed list,
##           blocked is taken priority.
## e.g. localPathListAllowed=/home;/project 
###########################################################
### localPathListAllowed=
securePort=8443
CertFileName=/etc/grid-security/hostcert.pem
KeyFileName=/etc/grid-security/hostkey.pem
### ProxyFileName=PROXY_FILE_PATH
###########################################################
# supportedProtocolList can be defined
##      When different from the same hostname that BeStMan runs on
##      Or, multiple transfer servers are supported
## supportedProtocolList=gsiftp://host1.domain.tld;gsiftp://host2.domain.tld
### supportedProtocolList=
############################################################ 
# NOTE: Each plugin entry must define the class name, jar file name, 
# and the procotol name the policy applies to. Key values are class, 
# jarFile and name, and they are separated by &. 
# Multiple plugin entries for different protocols can be defined, and 
# must be seperated by ;.  When two or more plugin entries are defined 
# for the same protocol, the last entry will be used
# If key name= is missing, and there is only one plugin entry, then 
# the policy would be used on all protocols. 
# e.g., when two entries are defined for gsiftp and http respectively; 
# protocolSelectionPolicy=class=plugin.NotRoundRobin1&jarFile=p1.jar&name=gsiftp
############################################################ 
### protocolSelectionPolicy=PROTOCOL_POLICY
### pluginLib=PLUGIN_PATH
###########################################################
### For Xrootd, xrootdTokenCompName=oss.cgroup may be needed
############################################################ 
pathForToken=true
## fsConcurrency limits the number of filesystem-involved
## operations processing at a time to the defined number.
## Affected operations are PUT, GET, LS, MKDIR, RMDIR and RM.
## If not specified, there is no limit.
# fsConcurrency=40
checkSizeWithFS=true
checkSizeWithGsiftp=false
###########################################################
# Sudo to manage file system (mkdir, rmdir, rm, mv, cp, ls)
# /etc/sudoers needs the following modifications
#              when daemon account runs bestman server
#              check proper paths for commands if needed
#    Cmnd_Alias SRM_CMD = /bin/rm, /bin/mkdir, /bin/rmdir, /bin/mv, /bin/cp, /bin/ls
#    Runas_Alias SRM_USR = ALL, !root
#    daemon   ALL=(SRM_USR) NOPASSWD: SRM_CMD
# 
accessFileSysViaSudo=false
### noSudoOnLs=true
### accessFileSysViaGsiftp=false
###########################################################
### showChecksumWhenListingFile to return checksum in srmLs
### defaultChecksumType supports adler32, md5, crc32
###########################################################
### showChecksumWhenListingFile=false
### defaultChecksumType=adler32
###########################################################
### hexChecksumCommand to plugin checksum calculation callout
### defaultChecksumType should be adler32 in hex
### only valid when showChecksumWhenListingFile=true
### e.g. hexChecksumCommand=/usr/local/mychecksum -adler32
###########################################################
### hexChecksumCommand=CHECKSUM_CALLOUT
###########################################################
# MaxMappedIDCached limits how many mapped ids can be cached at a given time
##   LifetimeSecondsMappedIDCached limits how long mapped ids are cached
##   LifetimeSecondsMappedIDCached=0 : permanently cache
##   LifetimeSecondsMappedIDCached<0 : never cache
##   LifetimeSecondsMappedIDCached>0 : cache for the duration
###########################################################
MaxMappedIDCached=1000
LifetimeSecondsMappedIDCached=1800
###########################################################
## userSpaceKeywords is to define keys to shorten the path
## format: userSpaceKeywords=(key1=/path1)(key2=/path2)
## e.g.: userSpaceKeywords=(tic=/user/tic)(tac=/proj/tac)
##bestman will disable space management when this is defined
## and SFN=/tic/fpath1 in SURL will be translated to SFN=/user/tic/fpath1
## difference from staticTokens: space descriptions are not supported.
### userSpaceKeywords=
###########################################################
## e.g. SUMGserviceURL=https://gums.lbl.gov:8443/gums/services/GUMSAuthorizationServicePort
## e.g. GUMSCurrHostDN=/DC=org/DC=doegrids/OU=Services/CN=gumsclient.lbl.gov
## When DN is not provided, it could be extracted from the cert/key when running server from 
###########################################################
### GUMSserviceURL=
### GUMSCurrHostDN=
### Grid-mapfile will be used
GridMapFileName=/etc/grid-security/grid-mapfile
### validateVomsProxy=false
###########################################################
# Do Not edit below unless you really really understand the entries
###########################################################
# GATEWAY mode entries
###########################################################
disableSpaceMgt=true
useBerkeleyDB=false
noCacheLog=true
Concurrency=40
#F# MaxNumberOfUsers=100
#F# MaxNumberOfFileRequests=1000000
#F# MaxConcurrentFileTransfer=10
#F# GridFTPNumStreams=2
#F# GridFTPBufferSizeBytes=2
#F# DefaultFileSizeMB=500
#F# DefaultVolatileFileLifeTimeInSeconds=1800
#F# PublicTokenMaxFileLifetimeInSeconds=1800
#F# InactiveTxfTimeOutInSeconds=300
#F# PublicSpaceProportion=80
#F# PublicSpaceInMB=
#F# DefaultMBPerToken=1000
#F# CacheLogLocation=/var/log
#F# CacheLogLocation=/var/log/cache.bestman.log
#F# pathForToken=false
#F# srmcacheKeywordOn=false
#F# uploadQueueParameter=40:10
#F# NOTE: uploadQueueParameter will be used as the transfer queue management
###########################################################
FactoryID=srm/v2/server
noEventLog=false
###########################################################
###########################################################
# MSS related when supported 
###########################################################
#F# MaxMSSConnections=15
#F# mssTimeOutSeconds=600
EOF


# 5 - write grid-mapfile and save to $GridMapFileName (see above)

# 6 - start bestman
