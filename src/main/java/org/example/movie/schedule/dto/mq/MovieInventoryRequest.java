package org.example.movie.schedule.dto.mq;

import lombok.Data;
import lombok.experimental.Accessors;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class MovieInventoryRequest {
  private String cityId;
  private String scheduleDate;
}
