package se.racasse.raclette.lunch;

import com.google.common.collect.ComparisonChain;
import se.racasse.raclette.place.Place;
import se.racasse.raclette.place.PlaceScore;
import se.racasse.raclette.place.PlaceScoringContext;

import java.util.HashMap;
import java.util.Optional;

class LunchSuggestor {

    private final LunchContext lunchContext;

    LunchSuggestor(LunchContext lunchContext) {
        this.lunchContext = lunchContext;
    }

    SuggestResult suggest() {
        return lunchContext.places.stream()
                .filter(place -> place.accepted(lunchContext.participants))
                .map(this::scorePlace)
                .sorted(this::compareScore)
                .collect(SuggestResult.collector());
    }

    private int compareScore(PlaceScore p1, PlaceScore p2) {
        return ComparisonChain.start()
                .compare(p2.score, p1.score)
                .compare(p1.place.name, p2.place.name)
                .result();
    }

    private PlaceScore scorePlace(Place place) {
        final PlaceScoringContext scoringContext = new PlaceScoringContext();
        scoringContext.persons = lunchContext.participants;
        scoringContext.lunchUpVotes = lunchContext.upVotes.get(place.id);
        scoringContext.lunchDownVotes = lunchContext.downVotes.get(place.id);
        scoringContext.personLunchVisits = new HashMap<>();
        lunchContext.participants.forEach(person -> {
            Optional<LunchVisit> placeVisit = person.latestVisits.stream().filter(v -> v.placeId == place.id).findFirst();
            scoringContext.personLunchVisits.put(person.id, placeVisit.orElse(null));
        });
        final float score = place.score(scoringContext);
        final PlaceScore placeScore = new PlaceScore();
        placeScore.place = place;
        placeScore.score = score;
        return placeScore;
    }

}
