package se.racasse.raclette.lunch;

import com.google.common.collect.Multimap;
import se.racasse.raclette.ActionResponse;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.place.Place;
import se.racasse.raclette.vote.Vote;

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
