package com.skkuse.team1.socialhub.jdbc.impl;

import com.skkuse.team1.socialhub.ExceptionWithHttpCode;
import com.skkuse.team1.socialhub.jdbc.AbstractJDBC;
import com.skkuse.team1.socialhub.model.User;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class UserJDBC extends AbstractJDBC<User> {
    // region singleton
    private static UserJDBC instance;
    private static MessageDigest digest;

    public static UserJDBC instance() {
        return instance;
    }

    private static final ReentrantLock lock = new ReentrantLock();

    public static Future<Void> init(Vertx vertx) {
        try {
            if (!lock.tryLock(30, TimeUnit.SECONDS)) throw new Exception("Cannot acquire lock!");
            if (instance != null) return Future.succeededFuture();
            Promise<Void> initPromise = Promise.promise();
            instance = new UserJDBC(vertx, initPromise);
            digest = MessageDigest.getInstance("MD5");
            return initPromise.future().compose(aVoid -> {
                lock.unlock();
                return Future.succeededFuture();
            });
        } catch (Exception ex) {
            // No need to worry about lock, because system will shut down on error.
            return Future.failedFuture(ex);
        }
    }
    // endregion

    protected UserJDBC(Vertx vertx, Promise<Void> initResult) {
        super(vertx, initResult);
    }

    @Override
    protected String tableName() {
        return "users";
    }

    public Future<User> getById(Long id) {
        return getConnection(false, (conn) -> {
            return conn.preparedQuery("SELECT * FROM %s WHERE id = $1".formatted(tableName()))
                    .execute(Tuple.of(id))
                    .map(this::fromRowSet);
        });
    }

    public Future<User> login(String login, String password) {
        return getConnection(false, (conn) -> {
            return conn.preparedQuery("SELECT salt FROM %s WHERE username = $1".formatted(tableName())).execute(Tuple.of(login)).compose(rowSet -> {
                if(rowSet.rowCount() == 0) return Future.failedFuture(new ExceptionWithHttpCode(HttpResponseStatus.UNAUTHORIZED.code(), "Wrong login or password"));
                String salt = rowSet.iterator().next().getString("salt");
                return conn.preparedQuery("SELECT * FROM %s WHERE username = $1 AND password = $2".formatted(tableName()))
                        .execute(Tuple.of(login, preprocessPassword(salt, password)));
            }).compose(rowSet -> {
                if(rowSet.size() == 0) return Future.failedFuture(new ExceptionWithHttpCode(HttpResponseStatus.UNAUTHORIZED.code(), "Wrong login or password"));
                return Future.succeededFuture(fillObjectList(rowSet));
            });
        }).map(list -> list.get(0));
    }

    public Future<Void> changePassword(Long id, String password, String oldPassword) {
        return getConnection(true, (conn) -> {
            return conn.preparedQuery("SELECT salt FROM %s WHERE id = $1".formatted(tableName())).execute(Tuple.of(id)).compose(rowSet -> {
                if(rowSet.rowCount() == 0) return Future.failedFuture(new ExceptionWithHttpCode(HttpResponseStatus.UNAUTHORIZED.code(), "Wrong login or password"));
                String salt = rowSet.iterator().next().getString("salt");
                String newSalt = UUID.randomUUID().toString();
                return conn.preparedQuery("UPDATE %s SET password = $1, salt = $2 WHERE id = $3 AND password = $4".formatted(tableName()))
                        .execute(Tuple.of(preprocessPassword(newSalt, password), newSalt, id, preprocessPassword(salt, oldPassword)));
            }).compose(rowSet -> {
                if(rowSet.rowCount() == 0) return Future.failedFuture(new ExceptionWithHttpCode(HttpResponseStatus.BAD_REQUEST.code(), "Wrong password!"));
                else return Future.succeededFuture();
            });
        });
    }

    public Future<User> create(User user) {
        return getConnection(true, (conn) -> {
            String salt = UUID.randomUUID().toString();
            return conn.preparedQuery("INSERT INTO %s (username, password, salt, id_security_question, security_answer) VALUES($1, $2, $3, $4, $5) RETURNING *".formatted(tableName()))
                    .execute(Tuple.of(user.getUsername(), preprocessPassword(salt, user.getPassword()), salt, user.getIdSecurityQuestion(), user.getSecurityAnswer()))
                    .map(this::fillObjectList);
        }).map(list -> list.get(0));
    }

    private String preprocessPassword(String salt, String password) {
        byte[] digested = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(digested);
    }

    @Override
    protected User fromRow(Row row) {
        // Fill only username and id, because other fields are private.
        return new User()
                .setId(row.getLong("id"))
                .setUsername(row.getString("username"));
    }

    @Override
    protected String DDL_QUERY() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id BIGSERIAL PRIMARY KEY,
                    username VARCHAR NOT NULL UNIQUE,
                    password VARCHAR NOT NULL,
                    salt VARCHAR NOT NULL,
                    id_security_question integer NOT NULL,
                    security_answer VARCHAR NOT NULL
                );
                """.formatted(tableName());
    }
}
