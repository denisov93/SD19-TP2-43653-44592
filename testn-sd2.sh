#!/bin/bash

[ ! "$(docker network ls | grep sd-net )" ] && \
	docker network create --driver=bridge --subnet=172.20.0.0/16 sd-net


if [  $# -le 1 ] 
then 
		echo "usage: $0 -image <img> [ -test <num> ] [ -log OFF|ALL|FINE ] [ -sleep <seconds> ]"
		exit 1
fi 

docker volume rm $(docker volume ls -qf dangling=true) 2> /dev/null > /dev/null

#docker pull smduarte/sd19-tp2-tester

#execute the client with the given command line parameters
docker run --rm --network=sd-net -it -v /var/run/docker.sock:/var/run/docker.sock smduarte/sd19-tp2-tester:latest $*

