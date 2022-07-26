package org.example.movie.schedule.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.movie.core.common.schedule.MovieDetailsResponse;
import org.example.movie.core.common.schedule.MovieInventoryRequest;
import org.example.movie.core.common.schedule.MovieInventoryResponse;
import org.example.movie.core.common.schedule.MovieScheduleRequest;
import org.example.movie.core.common.schedule.MovieScheduleResponse;
import org.example.movie.core.common.schedule.MovieScheduleTheatre;
import org.example.movie.schedule.adapter.KafkaProducerAdapter;
import org.example.movie.schedule.dto.response.Movie;
import org.example.movie.schedule.dto.response.MovieDetails;
import org.example.movie.schedule.dto.response.ShowSchedule;
import org.example.movie.schedule.dto.response.ShowTime;
import org.example.movie.schedule.dto.response.Theatre;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Data
@AllArgsConstructor
@Service
public class MovieSchedulerService {

  private final KafkaProducerAdapter kafkaProducerAdapter;

  public List<Movie> getMovieSchedule(final MovieInventoryRequest movieInventoryRequest)
          throws ExecutionException, InterruptedException, TimeoutException {
    String correlationData = UUID.randomUUID().toString();
    log.info("Starting Transaction with : {}",correlationData);
    MovieInventoryResponse movieInventoryResponse = ofNullable(kafkaProducerAdapter
            .kafkaMovieInventoryRequestReplyObject(correlationData,movieInventoryRequest))
                    .orElseGet(MovieInventoryResponse::of);
    List<MovieDetailsResponse> movieDetailsResponseList =
            ofNullable(movieInventoryResponse
                    .getMovieDetailsResponseList())
            .orElseGet(ArrayList::new);
    Map<String, List<MovieScheduleTheatre>> showScheduleMap=
            getShowScheduleMap(correlationData,
            movieInventoryRequest.getScheduleDate(),
                    movieDetailsResponseList.stream()
                    .map(MovieDetailsResponse::getMovieCityIdMapping)
                    .collect(Collectors.toList()));
    return movieDetailsResponseList
            .stream()
            .map(movieDetailsResponse ->
                    Movie
                            .of()
                            .setMovieDetails(convertToMovieDetails(movieDetailsResponse))
                            .setShowSchedule(
                                    ShowSchedule
                                            .of()
                                            .setShowDate(movieInventoryRequest.getScheduleDate())
                                            .setTheatreList(
                                                    showScheduleMap.get(movieDetailsResponse.getMovieCityIdMapping())
                                                            .stream()
                                                            .map(this::getTheatreShowTimeFromTheatreDetailsResponse)
                                                            .collect(Collectors.toList())
                                            )
                            )
            ).collect(Collectors.toList());
  }


  private Map<String, List<MovieScheduleTheatre>> getShowScheduleMap(String correlationData,
                                                                     String scheduleDate,
                                                                     List<String> movieCityMappingList)
          throws ExecutionException, InterruptedException, TimeoutException {
    return ofNullable(ofNullable(kafkaProducerAdapter.kafkaMovieScheduleRequestReplyObject(
            correlationData,
            MovieScheduleRequest.of()
                    .setScheduleDate(scheduleDate)
                    .setMovieCityMappingIdList(movieCityMappingList)))
            .orElseGet(MovieScheduleResponse::of)
            .getMovieScheduleMap())
            .orElseGet(HashMap::new);
  }

  private Theatre getTheatreShowTimeFromTheatreDetailsResponse(
          MovieScheduleTheatre movieScheduleTheatre) {
    return Theatre.of()
            .setTheatreName(movieScheduleTheatre.getTheatreDetails().getTheatreName())
            .setShowTimeList(movieScheduleTheatre
                    .getMovieShowList()
                    .stream()
                    .map(movieShow ->
                            ShowTime
                                    .of()
                                    .setShowtime(movieShow.getShowtime())
                                    .setSeatCount(movieShow.getSeatCount()))
                    .collect(Collectors.toList())
            );
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
}
