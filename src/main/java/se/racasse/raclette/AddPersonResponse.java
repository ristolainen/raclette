package se.racasse.raclette;

public class AddPersonResponse extends ActionResponse {
    public Person person;

    public AddPersonResponse(boolean success) {
        super(success);
    }
}
