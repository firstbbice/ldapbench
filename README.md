# LdapBench

A simple-minded LDAP benchmarking tool

## What is it?

This is a tool I wrote one weekend back in 2009 while working at SGI to get
benchmark numbers from an LDAP server. I wanted to push the LDAP servers to
their limits to find how many operations/second I could expect, what CPU
usages would be like (and what types of CPU usages they'd be). This allowed me
to find various bottlenecks. For instance, if I had logging enabled and going
to syslog (at least back then), I found slapd would bottleneck on logging
rather than on disk I/O or CPU. 

I have since tweaked it a bit to use the UnboundID LDAP SDK. That's
LdapBench3. So LdapBench2 uses the incredibly ancient novell LDAP SDK
that I used back in 2009 and LdapBench3 uses the more modern
UnboundID SDK that PingID maintains. I may eventually add a test
to LdapBench3 that uses LDAP Transactions (RFC 5805).

## How To Use It

You can use it direcly by downloading one or both of the required SDKs
and compiling one or both LdapBench programs. Or you can use docker to
spin up a container which will download the .jar files and compile
the programs. Then you can obtain a shell on the container and run the
commands.

Here's two example commands to start the container and get a shell:
```
docker run -d --net=bridge --name=ldapbench brentbice/ldapbench
docker exec -it ldapbench bash
```

This tool is very rudimentary. Given a hostname:port, a login DN and password,
and a base DN, it would create organizationalUnit objects for each thread, then
create N objects beneath that branch, then do queries for those objects, then
delete the objects, measuring how many operations per second it could do.
That's it.

If you run it inside the docker container, the environment should
already be setup. If you compile and run it natively,
your CLASSPATH must include the jldap jar file. For instance:
export CLASSPATH="jldap-2009-10-01.jar:."

Here's an example command:

**java LdapBench2 localhost:8389 "cn=admin,dc=tcn,dc=com" secret "ou=Bench,dc=tcn,dc=com" 3 200**

That command will use 3 threads, each creating 200 objects beneath ou=Thread X,ou=Bench,dc=tcn,dc=com.
Note that ou=Bench must already exist before you run it.
It should produce output like:
```
Total time: 4921.0 ms, Throughput: 121.92644 Adds/second
--------------------------------------------
Total time: 620.0 ms, Throughput: 967.74194 Queries/second
--------------------------------------------
Total time: 4547.0 ms, Throughput: 131.95514 Deletes/second
--------------------------------------------
```

The LdapBench3 program is almost identical. Here's a sample command to run it:

**java LdapBench3 localhost 8389 cn=admin,dc=tcn,dc=com secret ou=Bench,dc=tcn,dc=com 3 200**

## Warnings

There's little to no error-checking. It's got no polish whatsoever. It was just a tool I
banged out way back when because I needed something to stress some LDAP servers and
everything I found online was too complicated and fiddly to setup.

