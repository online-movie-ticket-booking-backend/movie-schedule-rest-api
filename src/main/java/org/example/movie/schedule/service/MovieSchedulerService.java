package org.example.movie.schedule.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.movie.schedule.configuration.ExchangeConfiguration;
import org.example.movie.schedule.dto.mq.MovieDetailsResponse;
import org.example.movie.schedule.dto.mq.MovieInventoryRequest;
import org.example.movie.schedule.dto.mq.MovieInventoryResponse;
import org.example.movie.schedule.dto.mq.MovieScheduleRequest;
import org.example.movie.schedule.dto.mq.MovieScheduleResponse;
import org.example.movie.schedule.dto.mq.TheatreDetailsResponse;
import org.example.movie.schedule.dto.response.Movie;
import org.example.movie.schedule.dto.response.MovieDetails;
import org.example.movie.schedule.dto.response.ShowSchedule;
import org.example.movie.schedule.dto.response.Theatre;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Data
@AllArgsConstructor
@Service
public class MovieSchedulerService {

  private final RabbitTemplate rabbitTemplate;
  private final ExchangeConfiguration movieInventoryExchange;
  private final ExchangeConfiguration movieScheduleExchange;

  public List<Movie> getMovieSchedule(final MovieInventoryRequest movieInventoryRequest) {
    CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
    return ofNullable(
            ofNullable(sendMovieInventoryMessageToExchange(movieInventoryRequest, correlationData))
                .orElseGet(MovieInventoryResponse::new)
                .getMovieDetailsResponseList())
        .orElseGet(ArrayList::new)
        .stream()
        .map(
            movieDetailsResponse ->
                Movie.of()
                    .setMovieDetails(convertToMovieDetails(movieDetailsResponse))
                    .setShowSchedule(
                        getShowSchedule(
                            movieDetailsResponse.getMovieCityIdMapping(),
                            movieInventoryRequest.getScheduleDate(),
                            correlationData)))
        .collect(Collectors.toUnmodifiableList());
  }

  private ShowSchedule getShowSchedule(
      String movieCityMapping, String scheduleDate, CorrelationData correlationData) {
    return ShowSchedule.of()
        .setShowDate(scheduleDate)
        .setTheatreList(
            ofNullable(
                    sendMovieScheduleMessageToExchange(
                            MovieScheduleRequest.of()
                                .setScheduleDate(scheduleDate)
                                .setMovieCityMappingId(movieCityMapping),
                            correlationData)
                        .getTheatreDetailsResponseList())
                .orElseGet(ArrayList::new)
                .stream()
                .map(this::getTheatreShowTimeFromTheatreDetailsResponse)
                .collect(Collectors.toList()));
  }

  private Theatre getTheatreShowTimeFromTheatreDetailsResponse(
      TheatreDetailsResponse theatreDetailsResponse) {
    return Theatre.of()
        .setTheatreName(theatreDetailsResponse.getTheatreName())
        .setShowTime(theatreDetailsResponse.getShowtime());
  }

  private MovieDetails convertToMovieDetails(MovieDetailsResponse movieDetailsResponse) {
    return MovieDetails.of()
        .setMovieName(movieDetailsResponse.getMovieName())
        .setGenre(
            Arrays.asList(StringUtils.trimToEmpty(movieDetailsResponse.getGenre()).split(",")))
        .setCertification(movieDetailsResponse.getMovieCertificationType())
        .setLanguage(movieDetailsResponse.getLanguage())
        .setRunTime(movieDetailsResponse.getMovieRunTime())
        .setReleaseDate(movieDetailsResponse.getMovieReleaseDate());
  }

  private MovieInventoryResponse sendMovieInventoryMessageToExchange(
      MovieInventoryRequest message, CorrelationData correlationData) {
    return rabbitTemplate.convertSendAndReceiveAsType(
        movieInventoryExchange.getExchange(),
        movieInventoryExchange.getRoutingKey(),
        message,
        messageProperties -> {
          messageProperties.getMessageProperties().setContentType("application/json");
          messageProperties.getMessageProperties().setReplyTo("movieInventoryQueue");
          return messageProperties;
        },
        correlationData,
        ParameterizedTypeReference.forType(MovieInventoryResponse.class));
  }

  private MovieScheduleResponse sendMovieScheduleMessageToExchange(
      MovieScheduleRequest message, CorrelationData correlationData) {
    return rabbitTemplate.convertSendAndReceiveAsType(
        movieScheduleExchange.getExchange(),
        movieScheduleExchange.getRoutingKey(),
        message,
        messageProperties -> {
          messageProperties.getMessageProperties().setContentType("application/json");
          messageProperties.getMessageProperties().setReplyTo("movieScheduleQueue");
          return messageProperties;
        },
        correlationData,
        ParameterizedTypeReference.forType(MovieScheduleResponse.class));
  }
}
