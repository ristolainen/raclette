package se.racasse.raclette;

import java.util.Optional;

public class RemoveLunchParticipantResponse extends ActionResponse {
    public Optional<Person> participant = Optional.empty();
    public RemoveLunchParticipantResponse(boolean success) {
        super(success);
    }
}
