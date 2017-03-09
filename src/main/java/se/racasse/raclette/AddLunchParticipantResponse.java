package se.racasse.raclette;

import java.util.Optional;

public class AddLunchParticipantResponse extends ActionResponse {
    public Optional<Person> participant = Optional.empty();
    public AddLunchParticipantResponse(boolean success) {
        super(success);
    }
}
