#!/bin/sh
# This removes the signing configuration from GraphView, which fixes build
# issues with Travis.
#
# Error message:
#
# Could not evaluate onlyIf predicate for task ':GraphView:signArchives'.
# > Unable to retrieve secret key from key ring file '/Users/jonas/.gnupg/secring.gpg' as it does not exist

GPDIR="$(dirname $0)/GraphView"
rm -f "${GPDIR}/maven_push.gradle"
sed -i "/maven_push.gradle/d" "${GPDIR}/build.gradle"
