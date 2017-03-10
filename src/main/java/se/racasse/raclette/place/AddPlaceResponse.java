package se.racasse.raclette.place;

import se.racasse.raclette.ActionResponse;

public class AddPlaceResponse extends ActionResponse {
    public Place place;
    public AddPlaceResponse(boolean success) {
        super(success);
    }
}
