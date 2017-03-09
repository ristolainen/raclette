package se.racasse.raclette;

import java.util.Optional;

public class LunchPlaceDecisionResponse extends ActionResponse {
    public Optional<Place> decidedPlace = Optional.empty();
    public LunchPlaceDecisionResponse(boolean success) {
        super(success);
    }
}
