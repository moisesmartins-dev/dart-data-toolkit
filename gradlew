#!/bin/sh
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8"
exec "$GRADLE_HOME/bin/gradle" "$@"
