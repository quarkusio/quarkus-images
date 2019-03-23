#! /bin/bash

set -e

export PATH=$PATH:$JAVA_HOME/bin

# Copy files from /usr/share/maven/ref into ${MAVEN_CONFIG}
# So the initial ~/.m2 is set with expected content.
# Don't override, as this is just a reference setup
copy_reference_file() {
  local root="${1}"
  local f="${2%/}"
  local logfile="${3}"
  local rel="${f/${root}/}" # path relative to /usr/share/maven/ref/
  echo "$f" >> "$logfile"
  echo " $f -> $rel" >> "$logfile"
  if [[ ! -e ${MAVEN_CONFIG}/${rel} || $f = *.override ]]
  then
    echo "copy $rel to ${MAVEN_CONFIG}" >> "$logfile"
    mkdir -p "${MAVEN_CONFIG}/$(dirname "${rel}")"
    cp -r "${f}" "${MAVEN_CONFIG}/${rel}";
  fi;
}

copy_reference_files() {
  local log="$MAVEN_CONFIG/copy_reference_file.log"

  if (sh -c "mkdir -p \"$MAVEN_CONFIG\" && touch \"${log}\"" > /dev/null 2>&1)
  then
      echo "--- Copying files at $(date)" >> "$log"
      find /usr/share/maven/ref/ -type f -exec bash -eu -c 'copy_reference_file /usr/share/maven/ref/ "$1" "$2"' _ {} "$log" \;
  else
    echo "Can not write to ${log}. Wrong volume permissions? Carrying on ..."
  fi
}

function configure_proxy() {
  echo "Checking and Setting Maven Proxies"
  if [ -n "$HTTP_PROXY_HOST" -a -n "$HTTP_PROXY_PORT" ]; then
    xml="<proxy>\
         <id>genproxy</id>\
         <active>true</active>\
         <protocol>http</protocol>\
         <host>$HTTP_PROXY_HOST</host>\
         <port>$HTTP_PROXY_PORT</port>"
    if [ -n "$HTTP_PROXY_USERNAME" -a -n "$HTTP_PROXY_PASSWORD" ]; then
      xml="$xml\
         <username>$HTTP_PROXY_USERNAME</username>\
         <password>$HTTP_PROXY_PASSWORD</password>"
    fi
    if [ -n "$HTTP_PROXY_NONPROXYHOSTS" ]; then
      xml="$xml\
         <nonProxyHosts>$HTTP_PROXY_NONPROXYHOSTS</nonProxyHosts>"
    fi
  xml="$xml\
       </proxy>"
    sed -i "s|<!-- ### configured http proxy ### -->|$xml|" $MAVEN_CONFIG/settings.xml
  fi
}

# insert settings for mirrors/repository managers into settings.xml if supplied
function configure_mirrors() {
  echo "Checking and Setting Maven mirrors "
  if [ -n "$MAVEN_MIRROR_URL" ]; then
    xml="    <mirror>\
      <id>mirror.default</id>\
      <url>$MAVEN_MIRROR_URL</url>\
      <mirrorOf>external:*</mirrorOf>\
    </mirror>"
    sed -i "s|<!-- ### configured mirrors ### -->|$xml|" $MAVEN_CONFIG/settings.xml
  fi
}

export -f copy_reference_file

copy_reference_files
configure_proxy
configure_mirrors
unset MAVEN_CONFIG 

exec "$@"
