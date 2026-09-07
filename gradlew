#!/bin/sh
# Minimal Gradle wrapper launcher. The JAR is fetched if missing.
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -s "$JAR" ]; then
  mkdir -p "$DIR/gradle/wrapper"
  curl -fsSL -o "$JAR" https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar
fi
exec java -Dorg.gradle.appname=gradlew -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
