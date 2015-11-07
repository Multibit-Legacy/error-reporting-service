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

```
cd <project root>
mvn clean install
java -cp "bcprov-jdk16-1.46.jar:target/error-reporting-service-<version>.jar" org.multibit.hd.error_reporting.ErrorReportingService server config.yml
```    

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

```
Host: http://localhost:9191/error-reporting
Content-Type: text/plain
Accept: text/plain

-----BEGIN PGP MESSAGE-----
Version: BCPG v1.46

hQEMA+aIld5YYUzuAQf+OdhL70BbwtniZw2RNN+VNgpak/yWWWDy7uxuI0zVvZ/Z
xK7Oo8c7Reay2pauVeOKaFBaMT0OXjOD8QcvknKd6g7EWonLjD/dZTBme2M/rNHC
JU1JU35aHBtE1vI6fsmKmbP7y/6qxCpU5iMmVHxDx5PRtmKPBGMJ+pWcj/lWVM4T
ucTKomd0yJQWRT0itxEoHshB7B1tDqHOAtq5kbvNoPcUqZUC85qxpjxCW4G9UERZ
4WWXcLluFHcdwG0Wl3aLHFWlZavQd/hM7XumpSTQAQP3YjDlI24OnQBE96SFN2SK
6AX3bUwG7p8G2h4AwRv64jmvY6A4SSwjVRzN4UO6ndLB6AEG3d5I/qQykX5oacEa
QIJLyvB8piaKiD7zusz6Jrmo5JyWcxs1lgmhWXMvLpTBl191ohFIzd35+jLPND08
SHa0utmToHEieWfmFV8WaBasrqCtbV9MzS/HeyUjYVyR2ZAyIWL4CsH+IWJzaIAt
d095nRKAY7OLpoZ1iV/s3YjxnT31lPWpAWoPx98LSKeoyl51URpI+gnvY8zepDlw
ntMWpJy6MmVfdvOg+fYxYgXRSvNupK4wLXZvnyK6iPWaYh+1BIUaJT+5zpo25dLz
LbKZby4lRWC7T8zOTdsSsYmZgPRJIUGmo999sgEDIJ2EgmHPIkSagjVFEsbGY3A6
eDAQ/9cORjXZfcCZ2uOZeBbgCI4cMZNF8JxjUWxi3Uc/R1hcK3oyrKP36X8a0b0P
Rn/2t0OR2LOYk2x5lFwXbUzShI1RKXXLZyI9S0FUNO6Rqzu9YWXGO2Kp+1PVHUpF
NuCOAEtVoGcQVYZZfeLpMGzKE8nH/+J5T8n0ZUIq81dcaqwcC6X0EKIkyXcqAG5T
h/z6kk6j2i+0BX8nwElmht0A5y7Hp1sTpP/cV3lVDbObay91/7ypMyFj+rvb/VYC
Afan2KSHJKzdXaHz6Zq5bZXpjWcJDtZtaEP8mPQchhb3blQGMy9XNJhX2pEqR3mw
eBodn6xVqwfbhrAqNwLBnt86dtwt0yKCt+DRsgmoPdI8RE2KImdJKiEFZLeKBnOl
k3WFrBBpdkigjoimTu09eyJk4EfZjVIqbwzk0p0g4+DcH/L5v+scMgPufulDAhMK
n+EwKLL1LPTLjGMlELC1mbb2JJLoqukndGqGxwBfxiMF09iDXcEvESwxMlp3kOG3
Hy9XDaV6LP0XPjVZ/JDrjkHCbitcpu+vyn52nO9xinWsQZUbx+mt6X0W
=zl8v
-----END PGP MESSAGE-----
```

If all goes well the response will be a `201_CREATED`. The service will have successfully decrypted the payload and then passed it
upstream for processing by the ELK stack (see later).

A `400_BAD_REQUEST` indicates that the Error Reporting Service is not able to decrypt the payload. (Wrong password)

### How to set up the rest of the Error Reporting environment

The Error Reporting service acts as a bridge between the client application providing an occasional encrypted error report containing
a log file and an ELK stack. ELK is short for "Elastic Search, Logstash and Kibana" which is a powerful trio of open source applications
providing data visualisation.

Everyone's server environment is different so [here is a general guide based on Centos 7](https://www.digitalocean.com/community/tutorial_series/centralized-logging-with-logstash-and-kibana-on-centos-7).
There are versions covering different server operating systems on that site as well.

### Installing ELK on a Mac
Here is a basic set of instructions to get an ELK stack in place on a Mac. It's only a quick intro to get started.

1. Install [Homebrew](http://brew.sh/)

2. Install Elasticsearch
```shell
$ brew update
$ brew install elasticsearch && brew info elasticsearch
$ elasticsearch
```
3. Verify Elasticsearch is working by visiting [http://localhost:9200](http://localhost:9200)
You should see a block of JSON similar to this:
```json
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
```
4. Install Logstash using a new Terminal tab
```shell
$ brew install logstash
```
5. Verify Logstash (make sure you have JSON logs in place)
```
$ logstash -e 'input { file { path => "/Users/<username>/Library/Application\ Support/MultiBitHD/logs/multibit-hd.log" type => "nginx" codec => "json" } } output { elasticsearch { host => localhost protocol => "http" port => "9200" } }'
```
Wait for "using Milestone 2 input plugin..." text to appear

6. Open a new Terminal tab and install Kibana (use their binary install since it's quicker) from [https://www.elastic.co/downloads/kibana](https://www.elastic.co/downloads/kibana) (select MAC and unzip somewhere). 
```shell
$ cd <kibana install>/bin
$ ./kibana
```
7. Verify Kibana by visiting [http://localhost:5601](http://localhost:5601)

### How are error reports ingested?

1. The encrypted error report yields a JSON structure containing a few descriptive fields (OS info, user notes etc) and a list of log entry objects.
2. This is pushed to Elasticsearch as its own index (`error-report-abc123`) containing an `error-report-summary` and a collection of `log-entry` items.
3. This structure allows for a combination of aggregate views over time and drilling down to an individual situation in an anonymous manner.
4. If Elasticsearch is not running the encrypted data is written to disk for later ingestion through an admin task

To force ingestion of the "dead letter queue" do the following on the same machine as the error reporting service is running:
```
curl -X POST http://localhost:9192/tasks/ingest
```
Note the use of the admin port (this is set in `config.yml` and will be different on Live).

### How are error reports converted to HTML for easy offline reading?

Rather than rely on Kibana and other online tools, the Error Reporting Service can export reports as HTML into a ZIP archive contained in the response. 

Do the following on the same machine as the error reporting service is running:
```
curl -X POST http://localhost:9192/tasks/export
```
Note the use of the admin port (this is set in `config.yml` and will be different on Live).

### How are error reports purged?

Rather than leave error reports in Elasticsearch it may be useful to purge them on a regular basis. This should be done as part of an export so that
data is not lost.

Do the following on the same machine as the error reporting service is running:
```
curl -X POST http://localhost:9192/tasks/export?purge=true
```
Note the use of the admin port (this is set in `config.yml` and will be different on Live).

### Connect over an SSH tunnel

Some users may find it convenient to reach Elasticsearch using an SSH tunnel. If the Elasticsearch instance is running with its default REST port (9200) then a local Kibana instance can be connected to a remote Elasticsearch instance as follows:

```
ssh example@example.org -L 9200:localhost:9200 -N &
curl 'localhost:9200/_cat/indices?v'
cd <kibana root>/bin
./kibana &
```
Connect your browser to [http://localhost:5601](http://localhost:5601) as normal.

### Where does the ASCII art come from?

The ASCII art for the startup banner was created using the online tool available at
[TAAG](http://patorjk.com/software/taag/#p=display&f=Slant&t=E%20R%20Service)
