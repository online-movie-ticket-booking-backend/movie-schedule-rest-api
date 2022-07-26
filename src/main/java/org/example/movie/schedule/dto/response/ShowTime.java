package org.example.movie.schedule.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class ShowTime {
    private String showtime;
    private int seatCount;
}
