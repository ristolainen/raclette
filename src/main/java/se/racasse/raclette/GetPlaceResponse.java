package se.racasse.raclette;

import java.util.Optional;

public class GetPlaceResponse extends ActionResponse {
    public Optional<Place> place = Optional.empty();

    public GetPlaceResponse(boolean success) {
        super(success);
    }
}
