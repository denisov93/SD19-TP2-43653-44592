FROM smduarte/sd19-services

MAINTAINER Sérgio Duarte <smd@fct.unl.pt>

WORKDIR /home/sd

COPY target/lib/*.jar /home/sd/ 

COPY target/*.jar /home/sd/

COPY sd2019-tp2.props /props/

COPY *.ks /home/sd/

ENV CLASSPATH /home/sd/*
