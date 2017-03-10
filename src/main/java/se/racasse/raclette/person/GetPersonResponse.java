package se.racasse.raclette.person;

import se.racasse.raclette.ActionResponse;

import java.util.Optional;

public class GetPersonResponse extends ActionResponse {
    public Optional<Person> person = Optional.empty();
    public GetPersonResponse(boolean success) {
        super(success);
    }
}
