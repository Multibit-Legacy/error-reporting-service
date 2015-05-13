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
external location (e.g. `/var/error-reporting/gpg/...`)

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
    
    hQEMA+aIld5YYUzuAQf9H8h/Wrsnlh90KEDjOI3Z4FaCgO/FZm0niBTlI2jk+oYX
    oSjx18Zc8eb8pKw4kGDJx8o1ezbQxBafzx4H0gHiva8R+sMqaciXXcyebTyIPG7s
    gx1S2xAgDxD5JlHMudTzpI2ZhdpZfE9e0ElSfOB9ltMqKGenBcMjwxibXw1BSrcV
    sW0LrG6EeHx5RCWA3qqLG8/aelReBLyA7pVHb3X2eJT3LmAOjGe/oUjqNLjMZxFk
    4pZQvr32DDXEuTKA5Rn0BhZtSflQuD2L/v6O9sDr6m/x1xKeiJZ4jzfe4RS9Atpl
    xq53zFyivmitJoJtkwNkEqyWEET+yZ169JDiuFXjOtLBMwGi+F6qbbPBcXqbdGZ7
    e9huU3VAl6F57yJKr6UVq7mBk8KwSDWDjUm/tnt3nJnIPK79ewmfN/LU4Fj1xFrf
    QZpQAYTU+ZR9QiaWCjPRXpcAdcxk4UpclccUX/3F4wD4eN0x+2O/Q2OBY1e8+cjF
    ZxyWJAyTtkyOZ9Icuk+PfMfQS4VnQ7HLEfp40PLD9OhMzSwp8UdEPNGP1bfCpOus
    iH1Nlq8L2qIliXVyOKQ9CfPITNiQdwBKwKwLDwQfofJ6pReaNlVrF8kqhKKNyKdx
    Lep/CIM/XG2SG4HNCjwszyOziGI4lONE1yFjIZugvibckw6BVgGmrcFdHr31fJ63
    u5tttus4QSQtFyk5ms6SI3MvApBKM3sR9hbQT+M3mJyHaBknNWFUD6I/O+LtmIkx
    K0XFxkyguhKqbJfWio00IU00i2aMSUuKStAjlMryMQQ2BffxXo1tQahSaizdGs1L
    def2Nrpx93OoR8ny498AC9uTmbZ1OmqF+unoCyfX+JiP/xpZeZLExonoyALwFSXV
    QIugqv3e8ys2nU3HDaQsLTwtacObDwX4AbbWIvW0z/5uQC7iYnn7vsFwsC6xlnpA
    JT3LwzksOeCULD/aC6rQKnNoYaOZOC+BPBC+3Rg/r2wKwgBONVJ/oC2LJ7a+kY36
    Im43xJ8=
    =m+b9
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

### Installing ELK on a Mac
Here is a basic set of instructions to get an ELK stack in place on a Mac. It's only a quick intro to get started.

1. Install Homebrew

http://brew.sh/

2. Install Elasticsearch

$ brew update
$ brew install elasticsearch && brew info elasticsearch
$ elasticsearch

3. Verify Elasticsearch is working by visiting

http://localhost:9200

and see a block of JSON

{
  "status" : 200,
  "name" : "Cold War",
  "cluster_name" : "elasticsearch_brew",
  "version" : {
    "number" : "1.5.1",
    "build_hash" : "5e38401bc4e4388537a615569ac60925788e1cf4",
    "build_timestamp" : "2015-04-09T13:41:35Z",
    "build_snapshot" : false,
    "lucene_version" : "4.10.4"
  },
  "tagline" : "You Know, for Search"
}

4. Install Logstash using a new Terminal tab

$ brew install logstash

5. Verify Logstash (make sure you have JSON logs in place)

$ logstash -e 'input { file { path => "/Users/<username>/Library/Application\ Support/MultiBitHD/logs/multibit-hd.log" type => "nginx" codec => "json" } } output { elasticsearch { host => localhost protocol => "http" port => "9200" } }'

Wait for "using Milestone 2 input plugin..." text to appear

6. Open a new Terminal tab and install Kibana (use their binary install since it's quicker) from https://www.elastic.co/downloads/kibana (select MAC and unzip somewhere). 

$ cd <kibana install>/bin
$ ./kibana

7. Verify Kibana by visiting:

http://localhost:5601

8. Configure Settings (first screen you'll see) to contain

Check: Index contains time-based events
Uncheck: Use event times...
Index name or pattern: logstash-*
Time-field name: @timestamp

9. Click Discover tab

See a bar graph attempting to count the number of log messages occurring at a particular time step.

So what we now have is a tool that can monitor a log file, and present the data contained within in a variety of ways. 

Our mission now is to come up with various useful queries to help us filter out the crap and identify useful messages that we need to know the frequency of, e.g. "ERROR" and "WARN" counts.

10. Observe @timestamp is selected in "Popular fields" on the left and that the main detail area is showing "Time, _source" which is basically a big mess

11. On the left hand panel, click "level" then Add

12. In the Search field across the top, type including quotes "WARN" then press Enter

13. Observe "Time, level" in the detail area and some colouring for the level

14. On the left hand panel, click "message" then Add

15. Observe the addition information in the detail area


### Where does the ASCII art come from?

The ASCII art for the startup banner was created using the online tool available at
[TAAG](http://patorjk.com/software/taag/#p=display&f=Slant&t=E%20R%20Service)
