package org.example.movie.schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class MovieDetails {
  private String movieName;
  private String runTime;
  private String releaseDate;

  @JsonProperty("genre")
  private List<String> genre;

  private String language;
  private String certification;
}
