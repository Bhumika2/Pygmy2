FROM maven:3.6.3-jdk-11-slim
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
ARG PORT
ENV PORT $PORT
CMD mvn clean install && mvn ninja:run -Dninja.port=$PORT -Dninja.jvmArgs="-Dninja.external.configuration=conf/hostname.conf"

