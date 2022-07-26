package org.example.movie.schedule.configuration;

import org.example.movie.core.common.schedule.MovieInventoryRequest;
import org.example.movie.core.common.schedule.MovieInventoryResponse;
import org.example.movie.core.common.schedule.MovieScheduleRequest;
import org.example.movie.core.common.schedule.MovieScheduleResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaTemplateConfiguration {

    @Bean
    public ReplyingKafkaTemplate<String, MovieScheduleRequest, MovieScheduleResponse> kafkaMovieScheduleReplyTemplate(
            ProducerFactory<String, MovieScheduleRequest> producerFactoryMovieScheduleRequest,
            ConcurrentMessageListenerContainer<String, MovieScheduleResponse> movieScheduleResponseListenerContainer) {
        return new ReplyingKafkaTemplate<>(producerFactoryMovieScheduleRequest,movieScheduleResponseListenerContainer);
    }

    @Bean
    public ReplyingKafkaTemplate<String, MovieInventoryRequest, MovieInventoryResponse> kafkaMovieInventoryReplyTemplate(
            ProducerFactory<String, MovieInventoryRequest> producerFactoryMovieInventoryRequest,
            ConcurrentMessageListenerContainer<String, MovieInventoryResponse> movieMovieInventoryResponseListenerContainer) {
        return new ReplyingKafkaTemplate<>(producerFactoryMovieInventoryRequest,movieMovieInventoryResponseListenerContainer);
    }
}
