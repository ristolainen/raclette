package se.racasse.raclette;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

class LunchSuggestor {

    private final Collection<Place> places;
    private final Collection<Person> persons;
    private final Map<Integer, LocalDate> latestLunches;

    public LunchSuggestor(Collection<Place> places, Collection<Person> persons, Map<Integer, LocalDate> latestLunches) {
        this.places = places;
        this.persons = persons;
        this.latestLunches = latestLunches;
    }

    public Optional<Place> suggest() {
        return places.stream()
                .filter(place -> place.accepted(persons))
                .sorted(this::compareScore).findFirst();
    }

    private int compareScore(Place p1, Place p2) {
        final float p1Score = p1.score(persons, latestLunches.get(p1.id));
        final float p2Score = p2.score(persons, latestLunches.get(p2.id));
        return Float.compare(p2Score, p1Score);
    }

}
