package se.racasse.raclette;

import java.time.LocalDate;

public class CreateLunchTimeResponse extends ActionResponse {
    public LocalDate lunchTime;
    public CreateLunchTimeResponse(boolean success) {
        super(success);
    }
}
