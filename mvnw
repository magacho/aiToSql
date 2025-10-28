#!/bin/sh
# Maven Wrapper script

MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$PWD"}
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper..."
    curl -o "$MAVEN_WRAPPER_JAR" https://repo1.maven.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
fi

exec java -jar "$MAVEN_WRAPPER_JAR" "$@"
