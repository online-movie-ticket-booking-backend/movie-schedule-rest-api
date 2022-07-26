package org.example.movie.schedule.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class Theatre {
  private String theatreName;
  private List<ShowTime> showTimeList;
}
