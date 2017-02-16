package se.racasse.raclette;

import java.time.LocalDate;
import java.util.Collection;

public class PlaceScoringContext {
    public LocalDate latestLunch;
    public Collection<Person> persons;
    public Collection<Vote> lunchUpVotes;
    public Collection<Vote> lunchDownVotes;
}
