## Error Reporting Service

Build status: [![Build Status](https://travis-ci.org/bitcoin-solutions/error-reporting-service.png?branch=develop)](https://travis-ci.org/bitcoin-solutions/error-reporting-service)

This repo contains the source for the MultiBit HD Error Reporting Service.

From a technical point of view this project uses

* Java - Primary language of the app
* [Maven](http://maven.apache.org/) - Build system
* [Dropwizard](http://dropwizard.io) - Self-contained web server

## Branches

We follow the ["master-develop" branching strategy](http://nvie.com/posts/a-successful-git-branching-model/).

This means that the latest release is on the "master" branch (the default) and the latest release candidate is on the "develop" branch.
Any issues are addressed in feature branches from "develop" and merged in as required.

## Verify you have Maven 3+

Most IDEs (such as [Intellij Community Edition](http://www.jetbrains.com/idea/download/)) come with support for Maven built in,
but if not then you may need to [install it manually](http://maven.apache.org/download.cgi).

IDEs such as Eclipse may require the [m2eclipse plugin](http://www.sonatype.org/m2eclipse) to be configured.

To quickly check that you have Maven 3+ installed check on the command line:

    mvn --version

A quick way to install Maven on Mac is to use HomeBrew.

### Configuration

The Error Reporting Service uses a PGP key to authenticate the requests sent to it.

This is in a PGP secret keyring stored exactly here: `/var/error-reporting/gpg/secring.gpg`

You will need to open up the permissions on the folders in `/var/error-reporting` and its subdirectories using:

    sudo chmod a+wx -R /var/error-reporting

To get a developer environment up and running just copy these values from `src/test/resources/fixtures/gpg` into the
external location.

### Inside an IDE

Import the project as a Maven project in the usual manner.

To start the project you just need to execute `ErrorReportingService.main()` as a Java application. You'll need a runtime configuration
that passes in `server config.yml` as the Program Arguments.

Open a browser to [http://localhost:9191/error-reporting/public-key](http://localhost:9191/error-reporting/public-key) and you should 
see the error reporting service public key.

At this stage you can perform most development tasks and you won't be prompted for a password.

## Running the error reporting service in Production

To run up the Error Reporting Service for real you need to run it outside of an IDE which introduces some security issues.

Java Security Providers such as Bouncy Castle can only be loaded from a trusted source. In the case of a JAR this means
that it must be signed with a certificate that has in turn been signed by one of the trusted Certificate Authorities (CAs)
in the `cacerts` file of the executing JRE.

Fortunately the Bouncy Castle team have done this so the `bcprov-jdk16-1.46.jar` must be external to the server JAR.

This changes the  launch command line from a standard Dropwizard as follows:

    cd <project root>
    mvn clean install
    java -cp "bcprov-jdk16-1.46.jar:target/error-reporting-service-<version>.jar" org.multibit.hd.error_reporting.ErrorReportingService server config.yml

where `<project root>` is the root directory of the project as checked out through git and `<version>` is the version
as found in `pom.xml` (e.g. "develop-SNAPSHOT" or "1.0.0") but you'll see a `.jar` in the `target` directory so it'll be obvious.

The Bouncy Castle security provider library should be in the project root for development.

On startup you will need to provide the passphrase for the service key store. It is not persisted anywhere.

All commands will work on *nix without modification, use `\` instead of `/` for Windows.

## Test the Error Reporting Service using a browser REST plugin

First open a browser to [http://localhost:9191/error-reporting/public-key](http://localhost:9191/error-reporting/public-key) and you should see the
Error Reporting Service public key. Note it is port 9191 not the usual 8080.

If you are running Chrome and have the excellent Advanced REST Client extension installed then you can build a POST request for the
development environment as follows:

    Host: http://localhost:9191/error-reporting
    Content-Type: text/plain
    Accept: text/plain

    -----BEGIN PGP MESSAGE-----
    Version: BCPG v1.46

    hQEMA+aIld5YYUzuAQf9GkIWCi7FKON9JzdRzpWurjCTiqEizTxxL+Wu67D5eTMD
    MKm1Cz4pGjq5G9j0rtxBZCn7ua/qt6QWBlPFuYQWdbAN2gsLVUgcejHMjD2MCfZc
    eAAAi4moOZE4r22hKKIpvaj/4dMp8G7pBsHIKmMAJCWnUaPFB/FQJx6KQ4i8Hh+W
    OvE0Fi2CHNLf9zELSMN3IZT3lueuZzxmeg2VTNB6H3dVRvp+HiKTlJ4Mrz5iXx6s
    lh225PsprHWWY7sY74820sFjcrC3r7ITRmBHVk3uAUvlhLcE2Kfnvcsks/lylLSX
    Nqm8p2KGiji9FALeRbjEzNAZ1VNY9PMeSbbTkTg+YNKIAZwnU0uKwf78XbVLigNy
    YOSuwRiXU8HUIfe6hViawYvlAD/HsgIGi/5MMpcYu1Ehahjz4p4VLYJ37lHvMnHd
    d/0IjDb/jb1HYXqUbRyJeAlU89TMJMOxL7PnYvAnGZPZvb7wQMcf4WjvbjqIDJ+U
    Q5zVwa4UtipOlo7ItzOfzRTW5RHiu56ZIg==
    =twKa
    -----END PGP MESSAGE-----

If all goes well the response will be a `201_CREATED`. The service will have successfully decrypted the payload and then passed it
upstream for processing by the ELK stack (see later).

A `400_BAD_REQUEST` indicates that the Error Reporting Service is not able to decrypt the payload.

### How to set up the rest of the Error Reporting environment

The Error Reporting service acts as a bridge between the client application providing an occasional encrypted error report containing
a log file and an ELK stack. ELK is short for "Elastic Search, Logstash and Kibana" which is a powerful trio of open source applications
providing data visualisation.

Everyone's server environment is different so [here is a general guide based on Centos 7](https://www.digitalocean.com/community/tutorial_series/centralized-logging-with-logstash-and-kibana-on-centos-7).
There are versions covering different server operating systems on that site as well.

### Where does the ASCII art come from?

The ASCII art for the startup banner was created using the online tool available at
[TAAG](http://patorjk.com/software/taag/#p=display&f=Slant&t=E%20R%20Service)
