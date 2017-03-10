package se.racasse.raclette.person;

import se.racasse.raclette.ActionResponse;

public class AddPersonResponse extends ActionResponse {
    public Person person;

    public AddPersonResponse(boolean success) {
        super(success);
    }
}
