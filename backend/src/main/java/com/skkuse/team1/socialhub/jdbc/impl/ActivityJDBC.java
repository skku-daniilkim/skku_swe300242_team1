package com.skkuse.team1.socialhub.jdbc.impl;

import com.skkuse.team1.socialhub.jdbc.AbstractJDBC;
import com.skkuse.team1.socialhub.model.Activity;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static io.vertx.core.Promise.promise;

public class ActivityJDBC extends AbstractJDBC<Activity> {

    // region singleton
    private static ActivityJDBC instance;

    public static ActivityJDBC instance() {
        return instance;
    }

    private static final ReentrantLock lock = new ReentrantLock();

    public static Future<Void> init(Vertx vertx) {
        try {
            if (!lock.tryLock(30, TimeUnit.SECONDS)) throw new Exception("Cannot acquire lock!");
            if (instance != null) return Future.succeededFuture();
            Promise<Void> initPromise = promise();
            instance = new ActivityJDBC(vertx, initPromise);
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

    protected ActivityJDBC(Vertx vertx, Promise<Void> initResult) {
        super(vertx, initResult);
    }

    @Override
    protected String tableName() {
        return "activities";
    }

    public Future<List<Activity>> create(List<Activity> activityList) {
        return getConnection(true, conn -> {
            return conn.preparedQuery("INSERT INTO %s (id_user_create, participant_count, title, description, location, date_time, id_participant_list) VALUES ($1, $2, $3, $4, $5, $6, $7)  RETURNING *".formatted(tableName()))
                    .executeBatch(activityList.stream().map(activity -> {
                        JsonArray idParticipantArray = activity.getIdParticipantList().stream().reduce(new JsonArray(), JsonArray::add, (a, b) -> new JsonArray());
                        return Tuple.of(activity.getIdUserCreate(), activity.getParticipantCount(), activity.getTitle(), activity.getDescription(), activity.getLocation(), localDateTime(activity.getDateTime()), idParticipantArray);
                    }).toList()).map(this::fillObjectList);
        });
    }

    public Future<Void> update(Activity activity) {
        return getConnection(true, conn -> {
            return conn.preparedQuery("UPDATE %s SET participant_count = $1, title = $2, description = $3, location = $4, date_time = $5, id_participant_list = $6".formatted(tableName()))
                    .execute(Tuple.of(activity.getIdParticipantList().size(), activity.getTitle(), activity.getDescription(), activity.getLocation(), localDateTime(activity.getDateTime()), activity.getIdParticipantList().stream().reduce(new JsonArray(), JsonArray::add, (a, b) -> new JsonArray()).encode()))
                    .mapEmpty();
        });
    }

    public Future<Void> delete(Long id) {
        return getConnection(true, conn -> {
            return conn.preparedQuery("DELETE FROM %s WHERE id = $1".formatted(tableName()))
                    .execute(Tuple.of(id))
                    .mapEmpty();
        });
    }

    public Future<List<Activity>> get(Long idUser) {
        return getConnection(false, conn -> {
            return conn.preparedQuery("SELECT * FROM %s WHERE id_participant_list @> $1 OR id_user_create = $2".formatted(tableName()))
                    .execute(Tuple.of(idUser, idUser))
                    .map(this::fillObjectList);
        });
    }

    public Future<Activity> getById(Long id) {
        return getConnection(false, conn -> {
            return conn.preparedQuery("SELECT * FROM %s WHERE id = $1".formatted(tableName()))
                    .execute(Tuple.of(id))
                    .map(this::fromRowSet);
        });
    }

    @Override
    protected Activity fromRow(Row row) {
        Activity activity = new Activity(
                row.getLong("id_user_create"),
                row.getInteger("participant_count"),
                row.getString("title"),
                row.getString("description"),
                localDateTime(row.getLong("date_time")),
                row.getString("location")
        ).setId(row.getLong("id"));
        activity.getIdParticipantList().addAll(row.getJsonArray("id_participant_list").stream().map(o -> Long.valueOf(o.toString())).toList());
        return activity;
    }

    @Override
    protected String DDL_QUERY() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id BIGSERIAL PRIMARY KEY,
                    id_user_create BIGINT NOT NULL REFERENCES users (id),
                    participant_count INT,
                    title VARCHAR NOT NULL,
                    description VARCHAR NOT NULL,
                    location VARCHAR NOT NULL,
                    date_time BIGINT NOT NULL,
                    id_participant_list jsonb NOT NULL DEFAULT '[]'
                );
                """.formatted(tableName());
    }
}
