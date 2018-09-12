FROM anapsix/alpine-java
MAINTAINER toorap

COPY ./build/libs/swagger-wirmock.jar /home/

CMD ["java","-jar","/home/swagger-wirmock.jar"]