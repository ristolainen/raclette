package se.racasse.raclette.lunch;

import com.google.common.collect.Multimap;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.place.Place;
import se.racasse.raclette.vote.Vote;

import java.util.Collection;

class LunchContext {
    Collection<Place> places;
    Collection<Person> participants;
    Multimap<Integer, Vote> upVotes;
    Multimap<Integer, Vote> downVotes;
}
