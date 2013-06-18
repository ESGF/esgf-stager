/CustodialQuality.*cache\/p/ {
    print
    getline
    print "ReplicaQualityStorageMB=[20000]path=/lustre/esgfs/SRMTemp;"
    print "CustodialQualityStorageMB=[0]path=&type=gov.ornl.srm.hpss.OrnlHpss&jarFile=OrnlHpss.jar&host=esg.ccs.ornl.gov&conf=ornl.ini"
}

/^securePort/ {
    print "publicPort=46789"
    print "securePort=46790"
    getline
}

/fsConcurrency/ {
    print "fsConcurrency=40"
    getline
}

/^checkSizeWithFS/ {
    print "#G#", $0
    getline
}

/^checkSizeWithGsiftp/ {
    print "#G#", $0
    getline
}

/^disableSpaceMgt/ {
    getline
    print "disableSpaceMgt=false"
}

/^useBerkeleyDB/ {
    getline
    print "useBerkeleyDB=true"
}

/^noCacheLog/ {
    getline
    print "noCacheLog=false"
}

/MaxNumberOfUsers/ {
    getline
    print "MaxNumberOfUsers=100"
}

/MaxNumberOfFileRequests/ {
    getline
    print "MaxNumberOfFileRequests=1000000"
}

/MaxConcurrentFileTransfer/ {
    print "MaxConcurrentFileTransfer=10"
    getline
}

/GridFTPNumStreams/ {
    print "GridFTPNumStreams=1"
    getline
}

/GridFTPBufferSizeBytes/ {
    print "GridFTPBufferSizeBytes=1048576"
    getline
}

/DefaultFileSizeMB/ {
    print "DefaultFileSizeMB=500"
    getline
}

/DefaultVolatileFileLifeTimeInSeconds/ {
    print "DefaultVolatileFileLifeTimeInSeconds=1800"
    getline
}

/PublicTokenMaxFileLifetimeInSeconds/ {
    print "PublicTokenMaxFileLifetimeInSeconds=1800"
    getline
}

/InactiveTxfTimeOutInSeconds/ {
    print "InactiveTxfTimeOutInSeconds=300"
    getline
}

/PublicSpaceProportion/ {
    print "PublicSpaceProportion=80"
    getline
}

/DefaultMBPerToken/ {
    print "DefaultMBPerToken=1000"
    getline
}

/CacheLogLocation/ {
    print "CacheLogLocation=/var/log"
    getline
}

/^pathForToken=true/ {
    print "##", $0
    getline
}

/pathForToken=false/ {
    print "pathForToken=false"
    getline
}

/disableSpaceMgt/ {
    print "disableSpaceMgt=false"
    getline
}

/srmcacheKeywordOn/ {
    print "srmcacheKeywordOn=true"
    getline
}

/uploadQueueParameter/ {
    print "uploadQueueParameter=40:10"
    getline
}

/.*/ {
    print
}
