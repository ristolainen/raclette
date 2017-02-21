package se.racasse.raclette;

import com.google.common.collect.ComparisonChain;

import java.util.Optional;

class LunchSuggestor {

    private final LunchContext lunchContext;

    public LunchSuggestor(LunchContext lunchContext) {
        this.lunchContext = lunchContext;
    }

    public Optional<Place> suggest() {
        return lunchContext.places.stream()
                .filter(place -> place.accepted(lunchContext.participants))
                .sorted(this::compareScore).findFirst();
    }

    private int compareScore(Place p1, Place p2) {
        return ComparisonChain.start()
                .compare(scorePlace(p2), scorePlace(p1))
                .compare(p1.name, p2.name)
                .result();
    }

    private float scorePlace(Place place) {
        final PlaceScoringContext scoringContext = new PlaceScoringContext();
        scoringContext.latestLunch = lunchContext.latestLunches.get(place.id);
        scoringContext.persons = lunchContext.participants;
        scoringContext.lunchUpVotes = lunchContext.upVotes.get(place.id);
        scoringContext.lunchDownVotes = lunchContext.downVotes.get(place.id);
        return place.score(scoringContext);
    }

}
