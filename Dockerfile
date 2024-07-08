FROM openjdk:17
MAINTAINER protege.stanford.edu

ARG JAR_FILE
COPY target/${JAR_FILE} webprotege-linearization-service.jar
ENTRYPOINT ["java","-jar","/webprotege-linearization-service.jar"]