FROM alpine:3.9

ENV CLASSPATH=jldap-2009-10-07.jar:/ldapbench
ENV THREADS=1
ENV OBJECTS=100
ENV SERVER="10.1.170.217:8389"
ENV TESTBASE="ou=Bench,dc=tcn,dc=com"
ENV AUTHDN="cn=admin,dc=tcn,dc=com"
ENV AUTHPASS="secret"

RUN apk update
RUN apk --update --no-cache add \
    openjdk8 bash

RUN mkdir /ldapbench

COPY LdapBench2.java /ldapbench
COPY *.jar /ldapbench
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

RUN cd /ldapbench && \
    /usr/lib/jvm/java-1.8-openjdk/bin/javac LdapBench2.java

# I prefer to run the ldapbench command manually
RUN touch /tmp/t.t
CMD [ "tail", "-f", "/tmp/t.t" ]

#CMD [ "/usr/local/bin/entrypoint.sh" ]

