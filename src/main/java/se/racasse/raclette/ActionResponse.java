package se.racasse.raclette;

public class ActionResponse {

    private final boolean success;
    public String errorMessage;

    public ActionResponse(boolean success) {
        this.success = success;
    }

    public boolean successful() {
        return success;
    }

}
