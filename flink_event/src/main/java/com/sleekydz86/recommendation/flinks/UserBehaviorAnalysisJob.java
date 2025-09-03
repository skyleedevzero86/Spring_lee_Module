package com.sleekydz86.recommendation.flinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.recommendation.model.UserBehaviorEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;

public class UserBehaviorAnalysisJob {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);
        env.enableCheckpointing(5000);

        ObjectMapper objectMapper = new ObjectMapper();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("user-behavior-events")
                .setGroupId("flink-behavior-analysis")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> kafkaStream = env.fromSource(source,
                WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20)),
                "Kafka Source");

        DataStream<UserBehaviorEvent> behaviorStream = kafkaStream
                .map(new MapFunction<String, UserBehaviorEvent>() {
                    @Override
                    public UserBehaviorEvent map(String jsonString) throws Exception {
                        try {
                            return objectMapper.readValue(jsonString, UserBehaviorEvent.class);
                        } catch (Exception e) {
                            System.err.println("JSON 파싱 실패: " + jsonString);
                            return null;
                        }
                    }
                })
                .filter(event -> event != null);

        DataStream<Tuple3<String, String, Long>> userActionStats = behaviorStream
                .map(event -> Tuple2.of(event.getUserId() + "_" + event.getActionType(), 1L))
                .keyBy(tuple -> tuple.f0)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
                .aggregate(new AggregateFunction<Tuple2<String, Long>, Long, Long>() {
                    @Override
                    public Long createAccumulator() {
                        return 0L;
                    }

                    @Override
                    public Long add(Tuple2<String, Long> value, Long accumulator) {
                        return accumulator + value.f1;
                    }

                    @Override
                    public Long getResult(Long accumulator) {
                        return accumulator;
                    }

                    @Override
                    public Long merge(Long a, Long b) {
                        return a + b;
                    }
                }, (key, window, input, out) -> {
                    String[] parts = key.split("_", 2);
                    Long count = input.iterator().next();
                    out.collect(Tuple3.of(parts[0], parts[1], count));
                });

        DataStream<Tuple3<String, String, Double>> itemPopularity = behaviorStream
                .map(event -> {
                    double score = getActionScore(event.getActionType());
                    return Tuple3.of(event.getItemId(), event.getCategory(), score);
                })
                .keyBy(tuple -> tuple.f0)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(10)))
                .aggregate(
                        new AggregateFunction<Tuple3<String, String, Double>, Tuple3<String, String, Double>, Tuple3<String, String, Double>>() {

                            @Override
                            public Tuple3<String, String, Double> createAccumulator() {
                                return Tuple3.of("", "", 0.0);
                            }

                            @Override
                            public Tuple3<String, String, Double> add(Tuple3<String, String, Double> value,
                                    Tuple3<String, String, Double> accumulator) {
                                return Tuple3.of(value.f0, value.f1, accumulator.f2 + value.f2);
                            }

                            @Override
                            public Tuple3<String, String, Double> getResult(
                                    Tuple3<String, String, Double> accumulator) {
                                return accumulator;
                            }

                            @Override
                            public Tuple3<String, String, Double> merge(Tuple3<String, String, Double> a,
                                    Tuple3<String, String, Double> b) {
                                return Tuple3.of(a.f0, a.f1, a.f2 + b.f2);
                            }
                        });

        userActionStats.addSink(JdbcSink.sink(
                "INSERT INTO user_behavior_stats (user_id, action_type, count, window_time) " +
                        "VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE count = VALUES(count)",
                new JdbcStatementBuilder<Tuple3<String, String, Long>>() {
                    @Override
                    public void accept(PreparedStatement statement,
                            Tuple3<String, String, Long> tuple) throws SQLException {
                        statement.setString(1, tuple.f0);
                        statement.setString(2, tuple.f1);
                        statement.setLong(3, tuple.f2);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/recommendation")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("password")
                        .build()));

        itemPopularity.addSink(JdbcSink.sink(
                "INSERT INTO item_popularity (item_id, category, popularity_score, updated_at) " +
                        "VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE " +
                        "popularity_score = VALUES(popularity_score), updated_at = VALUES(updated_at)",
                new JdbcStatementBuilder<Tuple3<String, String, Double>>() {
                    @Override
                    public void accept(PreparedStatement statement,
                            Tuple3<String, String, Double> tuple) throws SQLException {
                        statement.setString(1, tuple.f0);
                        statement.setString(2, tuple.f1);
                        statement.setDouble(3, tuple.f2);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/recommendation")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("password")
                        .build()));

        behaviorStream.addSink(JdbcSink.sink(
                "INSERT INTO user_behavior_events (user_id, item_id, action_type, category, " +
                        "rating, duration, timestamp, session_id, device_type, location) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new JdbcStatementBuilder<UserBehaviorEvent>() {
                    @Override
                    public void accept(PreparedStatement statement, UserBehaviorEvent event) throws SQLException {
                        statement.setString(1, event.getUserId());
                        statement.setString(2, event.getItemId());
                        statement.setString(3, event.getActionType());
                        statement.setString(4, event.getCategory());
                        statement.setObject(5, event.getRating());
                        statement.setObject(6, event.getDuration());
                        statement.setObject(7, java.sql.Timestamp.valueOf(event.getTimestamp()));
                        statement.setString(8, event.getSessionId());
                        statement.setString(9, event.getDeviceType());
                        statement.setString(10, event.getLocation());
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/recommendation")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("password")
                        .build()));

        env.execute("User Behavior Analysis Job");
    }

    private static double getActionScore(String actionType) {
        switch (actionType.toUpperCase()) {
            case "VIEW":
                return 1.0;
            case "CLICK":
                return 2.0;
            case "LIKE":
                return 3.0;
            case "SHARE":
                return 4.0;
            case "PURCHASE":
                return 10.0;
            default:
                return 0.5;
        }
    }
}