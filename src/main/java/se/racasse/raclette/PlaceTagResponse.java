package se.racasse.raclette;

import java.util.Optional;

public class PlaceTagResponse extends ActionResponse {
    public Optional<Place> place = Optional.empty();
    public PlaceTagResponse(boolean success) {
        super(success);
    }
}
