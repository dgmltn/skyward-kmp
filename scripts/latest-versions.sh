#!/usr/bin/env bash
# Prints the latest stable version of every entry in gradle/libs.versions.toml (plus Gradle itself),
# read from live registry metadata. Pre-releases (alpha, beta, rc, dev, eap, M) are skipped.
set -euo pipefail

CENTRAL="https://repo1.maven.org/maven2"
GOOGLE="https://dl.google.com/android/maven2"
PLUGINS="https://plugins.gradle.org/m2"

stable() {
  grep -viE '(alpha|beta|rc|dev|eap|snapshot|-m[0-9]|preview|pre|compat)' | sort -V | tail -n 1
}

central() { # group artifact [repo]
  local repo="${3:-$CENTRAL}"
  curl -fsSL "$repo/${1//.//}/$2/maven-metadata.xml" |
    sed -n 's:.*<version>\(.*\)</version>.*:\1:p' | stable
}

central_line() { # group artifact version-prefix
  curl -fsSL "$CENTRAL/${1//.//}/$2/maven-metadata.xml" |
    sed -n 's:.*<version>\(.*\)</version>.*:\1:p' | grep "^$3" | stable
}

google() { # group artifact
  curl -fsSL "$GOOGLE/${1//.//}/group-index.xml" |
    sed -n "s:.*<$2 versions=\"\([^\"]*\)\".*:\1:p" | tr ',' '\n' | stable
}

row() { printf '%-28s %s\n' "$1" "$2"; }

row gradle "$(curl -fsSL https://services.gradle.org/versions/current | sed -n 's/.*"version" *: *"\([^"]*\)".*/\1/p')"
row kotlin "$(central org.jetbrains.kotlin kotlin-gradle-plugin)"
row agp "$(google com.android.tools.build gradle)"
row compose-multiplatform "$(central org.jetbrains.compose compose-gradle-plugin)"
row kastro "$(central dev.jamesyox kastro)"
row koin "$(central io.insert-koin koin-core)"
row kermit "$(central co.touchlab kermit)"
row kotlinx-datetime "$(central org.jetbrains.kotlinx kotlinx-datetime)"
row kotlinx-coroutines "$(central org.jetbrains.kotlinx kotlinx-coroutines-core)"
row kotlinx-collections-immutable "$(central org.jetbrains.kotlinx kotlinx-collections-immutable)"
row lifecycle-viewmodel-kmp "$(central org.jetbrains.androidx.lifecycle lifecycle-viewmodel-compose)"
row datastore "$(google androidx.datastore datastore-preferences)"
row androidx-core "$(google androidx.core core-ktx)"
row activity-compose "$(google androidx.activity activity-compose)"
row compose-material3-kmp "$(central org.jetbrains.compose.material3 material3)"
row ktfmt-gradle "$(central com.ncorti.ktfmt.gradle plugin "$PLUGINS")"
row detekt "$(central io.gitlab.arturbosch.detekt detekt-gradle-plugin "$PLUGINS")"
# compose-rules 0.5+ targets detekt 2.x (still pre-release); 0.4.x is the line for detekt 1.23.
row compose-rules "$(central_line io.nlopez.compose.rules detekt 0.4.)"
