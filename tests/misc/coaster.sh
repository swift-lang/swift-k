#!/bin/bash

# test coasters locally, using a lame credential

export X509_USER_PROXY=$(pwd)/coaster-security/proxy.pem
export X509_CERT_DIR=$(pwd)/coaster-security/
pushd coaster-security
grid-proxy-init -cert ./cert.pem -key ./key.pem -hours 100 -out proxy.pem
popd


cd ../sites

./run-site coaster/coaster-local.xml
