#!/bin/bash

# If, when setting the target platform, Eclipse gets a bunch of errors about being unable to parse
# manifests and it turns out that each of the jars it complains about is not a valid zip file, you
# can use this to download the correct jars (the .pack.gz versions of the jars weren't corrupted,
# in my experience).
# 
# Usage: cd to where the pde downloads its plugins, which is typically something like
#   ~/workspaces/kepler/.metadata/.plugins/org.eclipse.pde.core/.bundle_pool/plugins
# and then run this script from that directory.

set -e

#UPDATE_SITE_BASE_URL=http://download.eclipse.org/eclipse/updates/3.8/R-3.8.2-201301310800/plugins/
UPDATE_SITE_BASE_URL=http://download.eclipse.org/eclipse/updates/3.7/R-3.7.2-201202080800/plugins/

for f in *.jar; do
#    echo "Processing ${f}"
    if ! unzip -t ${f} 2>&1 > /dev/null ; then
        echo "*** ${f} appears corrupt, redownloading"
        wget "${UPDATE_SITE_BASE_URL}${f}.pack.gz"
        unpack200 -r "${f}.pack.gz" "${f}"
    fi
done
