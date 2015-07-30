# Pillar

Pillar manages migrations for your [Cassandra](<http://cassandra.apache.org>)
data stores.

Pillar grew from a desire to automatically manage Cassandra schema as code.
Managing schema as code enables automated build and deployment, a foundational
practice for an organization striving to achieve [Continuous
Delivery](<http://en.wikipedia.org/wiki/Continuous_delivery>).

Pillar is to Cassandra what [Rails
ActiveRecord](<https://github.com/rails/rails/tree/master/activerecord>)
migrations or [Play
Evolutions](<http://www.playframework.com/documentation/2.0/Evolutions>) are to
relational databases with one key difference: Pillar is completely independent
from any application development framework.

## Installation

### Prerequisites

1.  Java SE 6 runtime environment

2.  Cassandra 2.0 with the native CQL protocol enabled

### From Source

This method requires [Simple Build Tool (sbt)](<http://www.scala-sbt.org>).
Building an RPM also requires [Effing Package Management
(fpm)](<https://github.com/jordansissel/fpm>).

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
% sbt assembly   # builds just the jar file in the target/ directory

% sbt rh-package # builds the jar and the RPM in the target/ directory
% sudo rpm -i target/pillar-1.0.0-DEV.noarch.rpm
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The RPM installs Pillar to /opt/pillar.

### Packages

Pillar is available at Maven Central under the GroupId com.chrisomeara and
ArtifactId pillar\_2.10 or pillar\_2.11. The current version is 2.0.1.

#### sbt

libraryDependencies += "com.chrisomeara" % "pillar\_2.10" % "2.0.1"

#### Gradle

compile 'com.chrisomeara:pillar\_2.10:2.0.1'

## Usage

### Terminology

Data Store

~   A logical grouping of environments. You will likely have one data store per
    application.

Environment

~   A context or grouping of settings for a single data store. You will likely
    have at least development and production environments for each data store.

Migration

~   A single change to a data store. Migrations have a description and a time
    stamp indicating the time at which it was authored. Migrations are applied
    in ascending order and reversed in descending order.

### Command Line

Here's the short version:

1.  Write migrations, place them in conf/pillar/migrations/myapp.

2.  Add pillar settings to conf/application.conf.

3.  % pillar initialize myapp

4.  % pillar migrate myapp

#### Migration Syntax

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- description: <description>
-- authoredAt: <time since epoch in millisecods>
-- authoredAtDate: <yyyy-mm-dd hh:mm:ss>
-- comment: <additional comments>
-- up: 
<cqlsh script without semicolon at the end>
-- down: 
<cqlsh script without semicolon at the end>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

#### Stage Syntax (1 or more stages)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- up: 
-- stage: 1
<cqlsh script without semicolon at the end>
-- stage: 2
<cqlsh script without semicolon at the end>
-- stage: 3
<cqlsh script without semicolon at the end>

-- down: 
-- stage: 1
<cqlsh script without semicolon at the end>
-- stage: 2
<cqlsh script without semicolon at the end>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

#### Batch Syntax 

The up command fails if you have more than one command separated by semicolons.
However, the workaround is to group the commands in CQL batch command and then
you can have as many as you like:

 

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
BEGIN BATCH
<cqlsh script with semicolon at the end>
<cqlsh script with semicolon at the end>
<cqlsh script with semicolon at the end>
APPLY BATCH
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

#### Migration Files

Migration files contain metadata about the migration, a
[CQL](<http://cassandra.apache.org/doc/cql3/CQL.html>) statement used to apply
the migration and, optionally, a
[CQL](<http://cassandra.apache.org/doc/cql3/CQL.html>) statement used to reverse
the migration. Each file describes one migration. You probably want to name your
files according to time stamp and description,
1370028263\_creates\_views\_table.cql, for example. Pillar reads and parses all
files in the migrations directory, regardless of file name.

Pillar supports reversible, irreversible and reversible with a no-op down
statement migrations. Here are examples of each:

Reversible migrations have up and down properties.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- description: creates views table
-- authoredAt: 1370028263000
-- up:

CREATE TABLE views (
  id uuid PRIMARY KEY,
  url text,
  person_id int,
  viewed_at timestamp
)

-- down:

DROP TABLE views
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Irreversible migrations have an up property but no down property.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- description: creates events table
-- authoredAt: 1370023262000
-- up:

CREATE TABLE events (
  batch_id text,
  occurred_at uuid,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id, occurred_at, event_type)
)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Reversible migrations with no-op down statements have an up property and an
empty down property.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- description: adds user_agent to views table
-- authoredAt: 1370028264000
-- up:

ALTER TABLE views
ADD user_agent text

-- down:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The Pillar command line interface expects to find migrations in
conf/pillar/migrations unless overriden by the -d command-line option.

#### Configuration

Pillar uses the [Typesafe Config](<https://github.com/typesafehub/config>)
library for configuration. The Pillar command-line interface expects to find an
application.conf file in ./conf or ./src/main/resources. Given a data store
called faker, the application.conf might look like the following:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
pillar.faker {
    development {
        cassandra-seed-address: "127.0.0.1"
        cassandra-keyspace-name: "pillar_development"
    }
    test {
        cassandra-seed-address: "127.0.0.1"
        cassandra-keyspace-name: "pillar_test"
    }
    acceptance_test {
        cassandra-seed-address: "127.0.0.1"
        cassandra-keyspace-name: "pillar_acceptance_test"
    }
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Reference the acceptance spec suite for details.

#### The pillar Executable

The package installs to /opt/pillar by default. The /opt/pillar/bin/pillar
executable usage looks like this:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Usage: pillar [OPTIONS] command data-store

OPTIONS

-d directory
--migrations-directory directory  The directory containing migrations

-e env
--environment env                 environment

-t time
--time-stamp time                 The migration time stamp

PARAMETERS

command     migrate or initialize

data-store  The target data store, as defined in application.conf
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#### Examples

Initialize the faker datastore development environment

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
% pillar -e development initialize faker
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Apply all migrations to the faker datastore development environment

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
% pillar -e development migrate faker
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Library

You can also integrate Pillar directly into your application as a library.
Reference the acceptance spec suite for details.

### Release Notes

#### 1.0.1

-   Add a "destroy" method to drop a keyspace (iamsteveholmes)

#### 1.0.3

-   Clarify documentation (pvenable)

-   Update Datastax Cassandra driver to version 2.0.2 (magro)

-   Update Scala to version 2.10.4 (magro)

-   Add cross-compilation to Scala version 2.11.1 (magro)

-   Shutdown cluster in migrate & initialize (magro)

-   Transition support from StreamSend to Chris O'Meara (comeara)

#### 2.0.0

-   Allow configuration of Cassandra port (fkoehler)

-   Rework Migrator interface to allow passing a Session object when integrating
    Pillar as a library (magro, comeara)

#### 2.0.1

-   Update a argot dependency to version 1.0.3 (magro)
