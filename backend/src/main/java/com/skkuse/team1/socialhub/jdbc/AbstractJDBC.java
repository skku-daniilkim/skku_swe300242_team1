package com.skkuse.team1.socialhub.jdbc;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractJDBC <T> {
    private PgPool connectionPool;
    protected AbstractJDBC(Vertx vertx, Promise<Void> initResult) {
        ConfigRetriever.create(vertx).getConfig().compose(configRetrieved -> {
            PgConnectOptions options = new PgConnectOptions()
                    .setPort(configRetrieved.getInteger("DB_PORT", 5432))
                    .setHost(configRetrieved.getString("DB_HOST", "localhost"))
                    .setDatabase(configRetrieved.getString("DB_NAME", "sample"))
                    .setUser(configRetrieved.getString("DB_USER", "sample"))
                    .setPassword(configRetrieved.getString("DB_PASSWORD", "sample"))
                    .setReconnectAttempts(5)
                    .setReconnectInterval(15000);
            PoolOptions poolOptions = new PoolOptions().setName("pg-pool")
                    .setMaxSize(configRetrieved.getInteger("POOL_MAX_SIZE", 5))
                    .setShared(true); // Pool is shared.
            connectionPool = PgPool.pool(vertx, options, poolOptions);
            return connectionPool.withConnection(conn -> {
                System.out.printf("Log %s", DDL_QUERY());
                return conn.preparedQuery(DDL_QUERY()).execute();
            });
        }).<Void>mapEmpty().onComplete(initResult);
    }

    protected <R> Future<R> getConnection(boolean withTransaction, Function<SqlConnection, Future<R>> process) {
        if(withTransaction) return connectionPool.withTransaction(process);
        else return connectionPool.withConnection(process);
    }

    protected abstract String tableName();

    protected abstract T fromRow(Row row);

    protected final T fromRowSet(RowSet<Row> rowSet) {
        Iterator<Row> rowIterator = rowSet.iterator();
        if(rowIterator.hasNext())
            return fromRow(rowIterator.next());
        else return null;
    }

    protected final List<T> fillObjectList(RowSet<Row> rowSet) {
        List<T> list = new ArrayList<>();
        for(Row row: rowSet) list.add(fromRow(row));
        return list;
    }

    /**
     * Convert epochMillis to {@link LocalDateTime}.
     * @param timestamp epochMillis
     * @return dateTime
     */
    protected LocalDateTime localDateTime(long timestamp) {
        return LocalDateTime.from(Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC));
    }

    /**
     * Reversed operation for dateTime conversion.
     * @see #localDateTime(long)
     */
    protected Long localDateTime(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }


    /**
     * Query to create table
     * @return query
     */
    protected abstract String DDL_QUERY();
}
