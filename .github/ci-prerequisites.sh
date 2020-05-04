#!/bin/bash
sudo apt-get update -o Dir::Etc::sourcelist="sources.list" -o Dir::Etc::sourceparts="-" -o APT::Get::List-Cleanup="0"
sudo apt-get install -y python3.7 python3-pip gcc build-essential libkrb5-dev python3-setuptools
sudo pip3 install virtualenv 

virtualenv --python=python3 ~/cekit
source ~/cekit/bin/activate
pip install -U cekit
pip install odcs
pip install docker
pip install docker_squash
pip install behave
pip install lxml      

# Re-claim some disk space
sudo swapoff -a
sudo rm -f /swapfile
sudo apt-get clean
docker rmi $(docker image ls -aq)
df -h