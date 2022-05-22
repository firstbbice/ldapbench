FROM alpine:3.9

ENV UNBOUND_VER=6.0.5
ENV UNBOUND_URL="https://github.com/pingidentity/ldapsdk/releases/download/$UNBOUND_VER/unboundid-ldapsdk-$UNBOUND_VER.zip"
ENV CLASSPATH=/ldapbench/unboundid-ldapsdk.jar:/ldapbench/jldap-2009-10-07.jar:/ldapbench
ENV PATH=/ldapbench:/usr/lib/jvm/java-1.8-openjdk/bin:$PATH
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
COPY LdapBench3.java /ldapbench
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

RUN cd /ldapbench && \
    wget 'https://download.jar-download.com/cache_jars/com.novell.ldap/jldap/2009-10-07/jar_files.zip' && \
    unzip jar_files.zip && \
    rm jar_files.zip

RUN cd /ldapbench && \
    wget "$UNBOUND_URL" && \
    unzip unboundid-ldapsdk-$UNBOUND_VER.zip unboundid-ldapsdk-$UNBOUND_VER/unboundid-ldapsdk.jar && \
    mv unboundid-ldapsdk*/*.jar . && \
    rm -rf unboundid-ldapsdk-$UNBOUND_VER && \
    rm unboundid-ldapsdk-$UNBOUND_VER.zip

RUN cd /ldapbench && \
    wget "$UNBOUND_URL"

RUN cd /ldapbench && \
    javac LdapBench2.java

RUN cd /ldapbench && \
    javac LdapBench3.java

RUN touch /tmp/t.t
CMD [ "tail", "-f", "/tmp/t.t" ]

#CMD [ "/usr/local/bin/entrypoint.sh" ]

