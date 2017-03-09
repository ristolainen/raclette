package se.racasse.raclette;

import com.google.common.collect.Multimap;

import java.time.LocalDate;
import java.util.Collection;

public class GetLunchStatusResponse extends ActionResponse {
    public LocalDate lunchTime;
    public Collection<Person> participants;
    public Collection<Place> places;
    public Multimap<Integer, Vote> votesByPlace;
    public SuggestResult suggestResult;
    public GetLunchStatusResponse(boolean success) {
        super(success);
    }
}
