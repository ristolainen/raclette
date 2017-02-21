package se.racasse.raclette;

enum VoteType {
    UP, DOWN;

    static VoteType fromInitial(String initial) {
        switch (initial) {
            case "U":
                return VoteType.UP;
            case "D":
                return VoteType.DOWN;
        }
        throw new IllegalStateException();
    }
}
