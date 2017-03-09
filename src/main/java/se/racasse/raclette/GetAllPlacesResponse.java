package se.racasse.raclette;

import java.util.Collection;

public class GetAllPlacesResponse extends ActionResponse {
    public Collection<Place> places;
    public GetAllPlacesResponse(boolean success) {
        super(success);
    }
}
