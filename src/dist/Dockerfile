# the base image is a trusted ubuntu build with java 8 (https://index.docker.io/u/dockerfile/java/)
FROM java:8

# that's me!
MAINTAINER Tim Whelan, timwhelan@electrum.co.za

# we need this because the workdir is modified in dockerfile/java -> now java:8
WORKDIR /

RUN curl -sL https://deb.nodesource.com/setup_8.x | bash -

RUN apt-get install -y nodejs

RUN npm install -g newman

# run the (java) server as the daemon user
USER daemon

# copy the locally built fat-jar to the image
ADD ${project.artifactId}-${project.version}-jar-with-dependencies.jar ${project.artifactId}-${project.version}-jar-with-dependencies.jar

ADD postman test

# the server binds to 8080 - expose that port
EXPOSE 	8080

# run the server when a container based on this image is being run
CMD ["java", "-jar", "${project.artifactId}-${project.version}-jar-with-dependencies.jar", "8080" ]
