package se.racasse.raclette;

import java.util.Optional;

public class AddVoteResponse extends ActionResponse {
    public Optional<Person> person = Optional.empty();
    public Optional<Place> place = Optional.empty();
    public AddVoteResponse(boolean success) {
        super(success);
    }
}
