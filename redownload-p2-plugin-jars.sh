#!/bin/bash

# If, when setting the target platform, Eclipse gets a bunch of errors about being unable to parse
# manifests and it turns out that each of the jars it complains about is not a valid zip file, you
# can use this to download the correct jars in the pack.gz forms (and then unpacking them; the
# .pack.gz versions of the jars aren't corrupted, in my experience).
#
# This script will download the jars from the 3.7.2 update site; you can plug in another update
# site by editing the script below (we're defaulting to 3.7.2 because at time of writing, that's
# the lowest version of Eclipse that we support).
# 
# Usage: cd to your Eclipse workspace (containing the .metadata directory), and then run this
# script from there.

set -e

#UPDATE_SITE_BASE_URL=http://download.eclipse.org/eclipse/updates/3.8/R-3.8.2-201301310800/plugins/
#UPDATE_SITE_BASE_URL=http://download.eclipse.org/eclipse/updates/3.7/R-3.7.2-201202080800/plugins/
UPDATE_SITE_BASE_URL=http://download.eclipse.org/releases/indigo/201202240900/aggregate/plugins/

# PDE downloads its plugins to here
cd .metadata/.plugins/org.eclipse.pde.core/.bundle_pool/plugins

for f in *.jar; do
#    echo "Processing ${f}"
    echo -n .
    if ! unzip -t ${f} 2>&1 > /dev/null ; then
        echo "*** ${f} appears corrupt, redownloading"
        curl -Ov "${UPDATE_SITE_BASE_URL}${f}.pack.gz"
        unpack200 -r "${f}.pack.gz" "${f}"
    fi
done
