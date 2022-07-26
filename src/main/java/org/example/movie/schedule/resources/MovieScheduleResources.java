package org.example.movie.schedule.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.example.movie.core.common.schedule.MovieInventoryRequest;
import org.example.movie.schedule.dto.response.Movie;
import org.example.movie.schedule.service.MovieSchedulerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping(value = "/movies", produces = "application/json")
public class MovieScheduleResources {

  private final MovieSchedulerService movieSchedulerService;

  @GetMapping("/schedule/{cityId}")
  @Operation(summary = "This is to fetch Movie Schedule by City")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Schedule retrieved successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = Movie.class)))
            }),
        @ApiResponse(
            responseCode = "204",
            description = "No Content Found For Select Criteria",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "City Information Not Correct",
            content = @Content)
      })
  public ResponseEntity<List<Movie>> getMovieSchedule(
      @PathVariable(name = "cityId") String cityId,
      @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(name = "scheduleDate", required = false)
          LocalDate scheduleDate)
          throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
    DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String dateAsString =
        Optional.ofNullable(scheduleDate).orElseGet(LocalDate::now).format(formatters);
    return ResponseEntity.ok(
        movieSchedulerService.getMovieSchedule(
            MovieInventoryRequest.of().setCityId(cityId).setScheduleDate(dateAsString)));
  }
}
