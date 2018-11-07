# Sample App using Separate RDBMS DB

This sample Lagom service demonstrates how to use different RDBMS databases for Write-Side and Read-Side of the Lagom Persistence Layer.

## Setup

It is recommended to start a MySQL server locally using docker:

`docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=rootpwd -p3306:3306 -d mysql:5`

Then, you have to manually create the tables on `writeside`:

```sql
DROP TABLE IF EXISTS journal;

CREATE TABLE IF NOT EXISTS journal (
  ordering SERIAL,
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  deleted BOOLEAN DEFAULT FALSE,
  tags VARCHAR(255) DEFAULT NULL,
  message BLOB NOT NULL,
  PRIMARY KEY(persistence_id, sequence_number)
);

CREATE UNIQUE INDEX journal_ordering_idx ON journal(ordering);

DROP TABLE IF EXISTS snapshot;

CREATE TABLE IF NOT EXISTS snapshot (
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  created BIGINT NOT NULL,
  snapshot BLOB NOT NULL,
  PRIMARY KEY (persistence_id, sequence_number)
);

```

Read side tables are created automatically (see `application.conf`)
