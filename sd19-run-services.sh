#!/bin/bash

export MONGO_REPLICATED=1

#update the images, in particular the client
docker pull smduarte/sd19-services:latest

docker volume rm $(docker volume ls -qf dangling=true) 2> /dev/null > /dev/null


#execute the servers each in its container
docker run -e MONGO_REPLICATED -v /var/run/docker.sock:/var/run/docker.sock smduarte/sd19-services:latest
