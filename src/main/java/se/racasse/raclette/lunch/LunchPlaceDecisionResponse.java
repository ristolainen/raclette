package se.racasse.raclette.lunch;

import se.racasse.raclette.ActionResponse;
import se.racasse.raclette.place.Place;

import java.util.Optional;

public class LunchPlaceDecisionResponse extends ActionResponse {
    public Optional<Place> decidedPlace = Optional.empty();
    public LunchPlaceDecisionResponse(boolean success) {
        super(success);
    }
}
