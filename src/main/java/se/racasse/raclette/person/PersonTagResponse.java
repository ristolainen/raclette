package se.racasse.raclette.person;

import se.racasse.raclette.ActionResponse;

import java.util.Optional;

public class PersonTagResponse extends ActionResponse {
    public Optional<Person> person = Optional.empty();
    public PersonTagResponse(boolean success) {
        super(success);
    }
}
