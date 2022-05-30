package org.example.movie.schedule.dto.mq;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MovieInventoryResponse {
  List<MovieDetailsResponse> movieDetailsResponseList;
}
