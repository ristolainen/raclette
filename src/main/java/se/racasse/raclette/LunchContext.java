package se.racasse.raclette;

import com.google.common.collect.Multimap;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

public class LunchContext {
    public Collection<Place> places;
    public Collection<Person> participants;
    public Multimap<Integer, Vote> upVotes;
    public Multimap<Integer, Vote> downVotes;
    public Map<Integer, LocalDate> latestLunches;
}
