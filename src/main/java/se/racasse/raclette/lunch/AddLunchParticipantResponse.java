package se.racasse.raclette.lunch;

import se.racasse.raclette.ActionResponse;
import se.racasse.raclette.person.Person;

import java.util.Optional;

public class AddLunchParticipantResponse extends ActionResponse {
    public Optional<Person> participant = Optional.empty();
    public AddLunchParticipantResponse(boolean success) {
        super(success);
    }
}
