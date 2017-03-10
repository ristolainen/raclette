package se.racasse.raclette.lunch;

import se.racasse.raclette.ActionResponse;

import java.time.LocalDate;

public class CreateLunchTimeResponse extends ActionResponse {
    public LocalDate lunchTime;
    public CreateLunchTimeResponse(boolean success) {
        super(success);
    }
}
