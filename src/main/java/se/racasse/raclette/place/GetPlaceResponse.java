package se.racasse.raclette.place;

import se.racasse.raclette.ActionResponse;

import java.util.Optional;

public class GetPlaceResponse extends ActionResponse {
    public Optional<Place> place = Optional.empty();

    public GetPlaceResponse(boolean success) {
        super(success);
    }
}
