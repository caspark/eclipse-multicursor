#!/bin/bash

# See RELEASING

if [ "$#" -ne 3 ]; then
    echo "Expected: 3 parameters:"
    echo "- release version"
    echo "- development version"
    echo "- release artifact destination directory"
    exit 1
fi

function fail_with_msg {
	echo $1
	exit 1
}

function set_maven_version {
	NEW_MAVEN_VERSION=$1

	echo "Using maven to update pom.xml versions to ${NEW_MAVEN_VERSION}"
	mvn org.codehaus.mojo:versions-maven-plugin:2.1:set -DnewVersion=${NEW_MAVEN_VERSION} > /dev/null || fail_with_msg "Setting versions failed; aborting"
	find . -name 'pom.xml.versionsBackup' -delete || fail_with_msg "Deleting temp files failed; aborting"
}

function set_osgi_version {
	OLD_OSGI_VERSION=$1
	NEW_OSGI_VERSION=$2

	echo "Updating OSGI manifest versions from ${OLD_OSGI_VERSION} to ${NEW_OSGI_VERSION}"
	find . -name "MANIFEST.MF" -exec sed -i "s/Bundle-Version: ${OLD_OSGI_VERSION}/Bundle-Version: ${NEW_OSGI_VERSION}/g" '{}' \; || fail_with_msg "Error; aborting"

	echo "Updating Eclipse feature version and referenced plugin version in the P2 feature definition from ${OLD_OSGI_VERSION} to ${NEW_OSGI_VERSION}"
	# using /g to make update both occurrences as per message above
	sed -i "s/version=\"${OLD_OSGI_VERSION}\"/version=\"${NEW_OSGI_VERSION}\"/g" com.asparck.eclipse.multicursor.feature/feature.xml || fail_with_msg "Error; aborting"

	echo "Updating Eclipse feature version and filename in the P2 update site category definition from ${OLD_OSGI_VERSION} to ${NEW_OSGI_VERSION}"
	sed -i "s/com.asparck.eclipse.multicursor.feature_${OLD_OSGI_VERSION}.jar/com.asparck.eclipse.multicursor.feature_${NEW_OSGI_VERSION}.jar/" com.asparck.eclipse.multicursor.p2updatesite/category.xml || fail_with_msg "Error; aborting"
	sed -i "s/version=\"${OLD_OSGI_VERSION}\">/version=\"${NEW_OSGI_VERSION}\">/" com.asparck.eclipse.multicursor.p2updatesite/category.xml || fail_with_msg "Error; aborting"
}

function verify_only_expected_files_are_dirty {
	UNEXPECTED_DIRTY=$(git status --porcelain | grep -v pom.xml | grep -v category.xml | grep -v MANIFEST.MF | grep -v feature.xml)
	if [[ ! -z ${UNEXPECTED_DIRTY} ]]; then
		echo "Unexpected files are dirty:"
		echo ${UNEXPECTED_DIRTY}
		exit 1
	fi
}

function git_commit_all_with_msg {
	echo "*** Committing with message '${1}'"
	git commit -a -m "${1}" || fail_with_msg "Error committing; aborting"
}


echo "Determining project development version from maven"
CURRENT_DEVELOPMENT_VERSION_MAVEN=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev '(^\[|Download\w+:)')
#CURRENT_DEVELOPMENT_VERSION_MAVEN="0.1.1-SNAPSHOT"
EXPECTED_CURRENT_DEVELOPMENT_VERSION_MAVEN_RE="([0-9]+\.[0-9]+\.[0-9]+)-SNAPSHOT"

if [[ ${CURRENT_DEVELOPMENT_VERSION_MAVEN} =~ ${EXPECTED_CURRENT_DEVELOPMENT_VERSION_MAVEN_RE} ]]; then
	CURRENT_DEVELOPMENT_VERSION=${BASH_REMATCH[1]}
else
	fail_with_msg "Maven development version of ${CURRENT_DEVELOPMENT_VERSION_MAVEN} does not match expected format of ${EXPECTED_CURRENT_DEVELOPMENT_VERSION_MAVEN_RE}"
fi

CURRENT_DEVELOPMENT_VERSION_OSGI=${CURRENT_DEVELOPMENT_VERSION}.qualifier
RELEASE_VERSION=$1
RELEASE_GIT_COMMIT_MSG="Release: v${RELEASE_VERSION}"
RELEASE_GIT_TAG="v${RELEASE_VERSION}"
NEXT_DEVELOPMENT_VERSION=${2}
NEXT_DEVELOPMENT_VERSION_MAVEN=${NEXT_DEVELOPMENT_VERSION}-SNAPSHOT
NEXT_DEVELOPMENT_VERSION_OSGI=${NEXT_DEVELOPMENT_VERSION}.qualifier
NEXT_DEVELOPMENT_GIT_COMMIT_MSG="Release: prepare for next development cycle"
RELEASE_ARTIFACT_DIR=$(readlink -m $3)

echo "CURRENT_DEVELOPMENT_VERSION_MAVEN=${CURRENT_DEVELOPMENT_VERSION_MAVEN}"
echo "CURRENT_DEVELOPMENT_VERSION_OSGI=${CURRENT_DEVELOPMENT_VERSION_OSGI} (assumed from Maven version)"
echo "RELEASE_VERSION=${RELEASE_VERSION}"
echo "RELEASE_GIT_COMMIT_MSG=${RELEASE_GIT_COMMIT_MSG}"
echo "RELEASE_GIT_TAG=${RELEASE_GIT_TAG}"
echo "NEXT_DEVELOPMENT_VERSION_MAVEN=${NEXT_DEVELOPMENT_VERSION_MAVEN}"
echo "NEXT_DEVELOPMENT_VERSION_OSGI=${NEXT_DEVELOPMENT_VERSION_OSGI}"
echo "NEXT_DEVELOPMENT_GIT_COMMIT_MSG=${NEXT_DEVELOPMENT_GIT_COMMIT_MSG}"
echo "RELEASE_ARTIFACT_DIR=${RELEASE_ARTIFACT_DIR}"

read -p "Continue with release? [y/n] " -n 1 -r
echo # move to a new line
if [[ ! ${REPLY} =~ ^[Yy]$ ]]
then
   echo "Release cancelled; aborting."
   exit 1
fi

echo "*** Preparing for release by setting versions to ${RELEASE_VERSION}"
set_maven_version ${RELEASE_VERSION}
set_osgi_version ${CURRENT_DEVELOPMENT_VERSION_OSGI} ${RELEASE_VERSION}

echo "*** Building release"
mvn clean verify || fail_with_msg "Error building release; aborting"

echo "*** Copying release artifacts to ${RELEASE_ARTIFACT_DIR}"
cp com.asparck.eclipse.multicursor.plugin/target/com.asparck.eclipse.multicursor.plugin-${RELEASE_VERSION}.jar \
   com.asparck.eclipse.multicursor.p2updatesite/target/com.asparck.eclipse.multicursor.p2updatesite-${RELEASE_VERSION}.zip \
   "${RELEASE_ARTIFACT_DIR}"

verify_only_expected_files_are_dirty
git_commit_all_with_msg "${RELEASE_GIT_COMMIT_MSG}"

echo "*** Tagging release with tag '${RELEASE_GIT_TAG}'"
git tag "${RELEASE_GIT_TAG}" || fail_with_msg "Error tagging; aborting"

echo "*** Preparing for next development version by setting versions to ${NEXT_DEVELOPMENT_VERSION}"
set_maven_version ${NEXT_DEVELOPMENT_VERSION_MAVEN}
set_osgi_version ${RELEASE_VERSION} ${NEXT_DEVELOPMENT_VERSION_OSGI}

echo "*** Building next development version to make sure it isn't broken"
mvn clean verify || fail_with_msg "Error building next development version; aborting"

verify_only_expected_files_are_dirty
git_commit_all_with_msg "${NEXT_DEVELOPMENT_GIT_COMMIT_MSG}"

echo "*** Release completed; next steps:"
echo "- Check the git commit history and then 'git push --tags'"
echo "- Upload artifacts from ${RELEASE_ARTIFACT_DIR} to a new github release: https://github.com/caspark/eclipse-multicursor/releases/new"
