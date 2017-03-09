package se.racasse.raclette;

public class AddPlaceResponse extends ActionResponse {
    public Place place;
    public AddPlaceResponse(boolean success) {
        super(success);
    }
}
