package se.racasse.raclette.place;

import se.racasse.raclette.ActionResponse;

import java.util.Optional;

public class PlaceTagResponse extends ActionResponse {
    public Optional<Place> place = Optional.empty();
    public PlaceTagResponse(boolean success) {
        super(success);
    }
}
