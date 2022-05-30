package org.example.movie.schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class Movie {
  private MovieDetails movieDetails;

  @JsonProperty("schedules")
  private ShowSchedule showSchedule;
}
