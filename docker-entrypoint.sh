#!/bin/sh
set -e

# Railway provides a single DATABASE_URL (postgresql://user:pass@host:port/db).
# Quarkus's JDBC datasource needs a jdbc: URL plus separate credentials, so derive
# them here into Quarkus's own env-var names. This runs only inside the container;
# local dev uses the SQLite qa profile and never needs this conversion.
if [ -n "$DATABASE_URL" ]; then
  no_proto="${DATABASE_URL#*://}"   # user:pass@host:port/db
  creds="${no_proto%%@*}"           # user:pass
  host_db="${no_proto#*@}"          # host:port/db(?params)
  export QUARKUS_DATASOURCE_USERNAME="${creds%%:*}"
  export QUARKUS_DATASOURCE_PASSWORD="${creds#*:}"
  export QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://${host_db}"
fi

exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port="${PORT:-8080}" -jar quarkus-run.jar
