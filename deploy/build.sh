#!/bin/bash
# Builds the application image and pushes it to the registry.
# Requires `docker login` against $REGISTRY_HOST to already be done.
#
#   ./deploy/build.sh                  # tag with the current short commit
#   TAG=v1.2.3 ./deploy/build.sh       # tag a release
#   REGISTRY=docker.jojoaddison.net/ns ./deploy/build.sh   # push somewhere else
#
# One image, not two: this is a JHipster monolith, and `-Pprod` compiles the Angular bundle into the
# jar's static resources. There is no separate web image to build.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

# Namespace prefix, not just the host — the image name is ${REGISTRY}/ankobra-web.
# Matches deploy/prod-server/.env.example so a hand-built image and a deploy.sh-built one land in
# the same place.
REGISTRY="${REGISTRY:-docker.jojoaddison.net}"
REGISTRY_HOST="${REGISTRY%%/*}"
IMAGE="${REGISTRY}/ankobra-web"

# Defaults to the current commit so every pushed image is traceable back to source.
TAG="${TAG:-$(git rev-parse --short HEAD)}"

# Both toolchains are pinned, and neither pin is cosmetic:
#   Java 25 — <java.version> in pom.xml. The box may default to JDK 26, which JHipster's
#             maven-enforcer rejects ("JHipster supports JDK 21 to 25"), failing before it compiles.
#             The full Oracle JDK 25 is the intended one; java-25-openjdk-amd64 is a JRE with no
#             javac (see CLAUDE.md), so Maven would fall through PATH to the ambient JDK and fail.
#   Node    — frontend-maven-plugin downloads <node.version> itself during -Pprod; nothing to pin here.
export JAVA_HOME="${JAVA_HOME_25:-/usr/lib/jvm/jdk-25.0.2-oracle-x64}"
if [[ ! -x "$JAVA_HOME/bin/javac" ]]; then
  echo "error: no JDK (with javac) at $JAVA_HOME. Set JAVA_HOME_25 to a full JDK 25." >&2
  exit 1
fi

# `[^"]*` not `.*` before the quote: BRE is greedy, so `.*"` would run to the CLOSING quote of
# "25.0.2" and capture an empty string, then reject a perfectly good JDK.
java_major="$("$JAVA_HOME/bin/java" -version 2>&1 | sed -n '1s/[^"]*"\([0-9]*\).*/\1/p')"
if [[ "$java_major" -lt 21 || "$java_major" -gt 25 ]]; then
  echo "error: JDK $java_major is outside the 21-25 range JHipster's enforcer accepts." >&2
  exit 1
fi

if ! grep -q "$REGISTRY_HOST" ~/.docker/config.json 2>/dev/null; then
  echo "warning: no stored credential for $REGISTRY_HOST - the push will fail." >&2
  echo "         run: docker login $REGISTRY_HOST" >&2
fi

echo "Building ${IMAGE}:${TAG} (jib, -Pprod)"

# jib:build pushes straight to the registry without a local Docker daemon build — JHipster generates
# no Dockerfile, and the jib plugin configuration in pom.xml is the image definition.
#
# `verify` before it, not `package`: this is the last gate before an image goes to production, and
# skipping the tests here is how an image nobody ran the suite against ends up deployed.
./mvnw -ntp --batch-mode -Pprod verify jib:build \
  -Djib.to.image="${IMAGE}:${TAG}" \
  -Djib.to.tags=latest \
  -Djib.console=plain

echo "Done. Pushed:"
echo "  ${IMAGE}:${TAG}"
echo "  ${IMAGE}:latest"
