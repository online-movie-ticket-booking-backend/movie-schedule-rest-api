package org.example.movie.schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class MovieResponseDTO {

  @JsonProperty("movies")
  List<Movie> movieList;
}
