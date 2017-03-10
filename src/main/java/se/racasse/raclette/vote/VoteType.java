package se.racasse.raclette.vote;

public enum VoteType {
    UP, DOWN;

    public static VoteType fromInitial(String initial) {
        switch (initial) {
            case "U":
                return VoteType.UP;
            case "D":
                return VoteType.DOWN;
        }
        throw new IllegalStateException();
    }
}
