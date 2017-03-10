package se.racasse.raclette.vote;

import se.racasse.raclette.ActionResponse;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.place.Place;

import java.util.Optional;

public class AddVoteResponse extends ActionResponse {
    public Optional<Person> person = Optional.empty();
    public Optional<Place> place = Optional.empty();
    public AddVoteResponse(boolean success) {
        super(success);
    }
}
