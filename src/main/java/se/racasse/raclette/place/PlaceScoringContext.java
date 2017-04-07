package se.racasse.raclette.place;

import se.racasse.raclette.lunch.LunchVisit;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.vote.Vote;

import java.util.Collection;
import java.util.Map;

public class PlaceScoringContext {
    public Collection<Person> persons;
    public Collection<Vote> lunchUpVotes;
    public Collection<Vote> lunchDownVotes;
    public Map<Integer, LunchVisit> personLunchVisits;
}
