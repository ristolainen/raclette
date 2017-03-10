package se.racasse.raclette.place;

import se.racasse.raclette.person.Person;
import se.racasse.raclette.vote.Vote;

import java.time.LocalDate;
import java.util.Collection;

public class PlaceScoringContext {
    public LocalDate latestLunch;
    public Collection<Person> persons;
    public Collection<Vote> lunchUpVotes;
    public Collection<Vote> lunchDownVotes;
}
