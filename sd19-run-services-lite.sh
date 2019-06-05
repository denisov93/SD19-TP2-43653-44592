#!/bin/bash

# Esta versão lança apenas 1 servidor mongo (mongo1)
# Destina-se a quem tem máquinas com poucos recursos

export MONGO_REPLICATED=0

#update the images, in particular the client
docker pull smduarte/sd19-services-lite:latest

#execute the servers each in its container
docker run -e MONGO_REPLICATED -v /var/run/docker.sock:/var/run/docker.sock smduarte/sd19-services-lite:latest
