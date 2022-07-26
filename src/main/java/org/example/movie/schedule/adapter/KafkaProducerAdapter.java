package org.example.movie.schedule.adapter;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.movie.core.common.schedule.MovieInventoryRequest;
import org.example.movie.core.common.schedule.MovieInventoryResponse;
import org.example.movie.core.common.schedule.MovieScheduleRequest;
import org.example.movie.core.common.schedule.MovieScheduleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaProducerAdapter {

    private ReplyingKafkaTemplate<String, MovieScheduleRequest, MovieScheduleResponse> kafkaMovieScheduleReplyTemplate;
    private ReplyingKafkaTemplate<String, MovieInventoryRequest, MovieInventoryResponse> kafkaMovieInventoryReplyTemplate;
    @Value("${kafka.movieBookingApi.movieInventory.topic.request}")
    private String movieInventoryTopicName;

    @Value("${kafka.movieBookingApi.movieSchedule.topic.request}")
    private String movieScheduleTopicName;

    @Autowired(required = true)
    public void setKafkaMovieScheduleReplyTemplate(ReplyingKafkaTemplate<String, MovieScheduleRequest,
            MovieScheduleResponse> kafkaMovieScheduleReplyTemplate) {
        this.kafkaMovieScheduleReplyTemplate = kafkaMovieScheduleReplyTemplate;
    }

    @Autowired(required = true)
    public void setKafkaMovieInventoryReplyTemplate(ReplyingKafkaTemplate<String, MovieInventoryRequest,
            MovieInventoryResponse> kafkaMovieInventoryReplyTemplate) {
        this.kafkaMovieInventoryReplyTemplate = kafkaMovieInventoryReplyTemplate;
    }

    public MovieScheduleResponse kafkaMovieScheduleRequestReplyObject(String uniqueId,
            MovieScheduleRequest movieScheduleRequest) throws ExecutionException, InterruptedException, TimeoutException {
        ProducerRecord<String, MovieScheduleRequest> record =
                new ProducerRecord<>(movieScheduleTopicName, uniqueId,movieScheduleRequest);
        RequestReplyFuture<String, MovieScheduleRequest, MovieScheduleResponse> replyFuture =
                kafkaMovieScheduleReplyTemplate.sendAndReceive(record);
        SendResult<String, MovieScheduleRequest> sendResult =
                replyFuture.getSendFuture().get(10, TimeUnit.SECONDS);
        ConsumerRecord<String, MovieScheduleResponse> consumerRecord =
                replyFuture.get(10, TimeUnit.SECONDS);
        return consumerRecord.value();
    }

    public MovieInventoryResponse kafkaMovieInventoryRequestReplyObject(String uniqueId,
            MovieInventoryRequest movieInventoryRequest) throws ExecutionException, InterruptedException, TimeoutException {
        ProducerRecord<String, MovieInventoryRequest> record =
                new ProducerRecord<>(movieInventoryTopicName, uniqueId, movieInventoryRequest);
        RequestReplyFuture<String, MovieInventoryRequest, MovieInventoryResponse> replyFuture =
                kafkaMovieInventoryReplyTemplate.sendAndReceive(record);
        SendResult<String, MovieInventoryRequest> sendResult =
                replyFuture.getSendFuture().get(10, TimeUnit.SECONDS);
        ConsumerRecord<String, MovieInventoryResponse> consumerRecord =
                replyFuture.get(10, TimeUnit.SECONDS);
        return consumerRecord.value();
    }
}

