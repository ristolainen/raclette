package se.racasse.raclette;

import java.util.Optional;

public class PersonTagResponse extends ActionResponse {
    public Optional<Person> person = Optional.empty();
    public PersonTagResponse(boolean success) {
        super(success);
    }
}
