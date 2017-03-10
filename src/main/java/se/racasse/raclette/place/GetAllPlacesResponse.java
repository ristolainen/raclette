package se.racasse.raclette.place;

import se.racasse.raclette.ActionResponse;

import java.util.Collection;

public class GetAllPlacesResponse extends ActionResponse {
    public Collection<Place> places;
    public GetAllPlacesResponse(boolean success) {
        super(success);
    }
}
