#!/bin/bash
sudo apt-get update -o Dir::Etc::sourcelist="sources.list" -o Dir::Etc::sourceparts="-" -o APT::Get::List-Cleanup="0"
sudo apt-get install -y python3.7 python3-pip gcc build-essential libkrb5-dev python3-setuptools
sudo pip3 install virtualenv 

virtualenv --python=python3 .cekit
source .cekit/bin/activate
pip install -r requirements.txt      

# Re-claim some disk space
sudo swapoff -a
sudo rm -f /swapfile
sudo apt-get clean
docker rmi $(docker image ls -aq)
df -h

# Install s2i (needed for tests)
echo "Installing s2i"
mkdir s2i && cd s2i 
curl -s https://api.github.com/repos/openshift/source-to-image/releases/latest \
  | grep browser_download_url \
  | grep linux-amd64 \
  | cut -d '"' -f 4 \
  | wget -qi -
tar xvf source-to-image*.gz
chmod +s ./s2i
cd ..

# Install jbang
export JBANG_VERSION=0.39.0
wget https://github.com/jbangdev/jbang/releases/download/v${JBANG_VERSION}/jbang-${JBANG_VERSION}.tar
mkdir jbang
tar xvfv jbang-${JBANG_VERSION}.tar --strip-components=1 -C jbang