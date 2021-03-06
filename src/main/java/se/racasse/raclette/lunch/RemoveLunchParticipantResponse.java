package se.racasse.raclette.lunch;

import se.racasse.raclette.ActionResponse;
import se.racasse.raclette.person.Person;

import java.util.Optional;

public class RemoveLunchParticipantResponse extends ActionResponse {
    public Optional<Person> participant = Optional.empty();
    public RemoveLunchParticipantResponse(boolean success) {
        super(success);
    }
}
