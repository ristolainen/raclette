package se.racasse.raclette;

import java.util.Collection;
import java.util.Optional;

class LunchSuggestor {

    private final Collection<Place> places;
    private final Collection<Person> persons;

    public LunchSuggestor(Collection<Place> places, Collection<Person> persons) {
        this.places = places;
        this.persons = persons;
    }

    public Optional<Place> suggest() {
        return places.stream()
                .filter(place -> place.accepted(persons))
                .sorted(this::compareScore).findFirst();
    }

    private int compareScore(Place p1, Place p2) {
        return Integer.compare(p2.score(persons), p1.score(persons));
    }

}
