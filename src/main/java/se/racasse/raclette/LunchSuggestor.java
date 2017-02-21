package se.racasse.raclette;

import com.google.common.collect.ComparisonChain;

class LunchSuggestor {

    private final LunchContext lunchContext;

    public LunchSuggestor(LunchContext lunchContext) {
        this.lunchContext = lunchContext;
    }

    public SuggestResult suggest() {
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
        scoringContext.latestLunch = lunchContext.latestLunches.get(place.id);
        scoringContext.persons = lunchContext.participants;
        scoringContext.lunchUpVotes = lunchContext.upVotes.get(place.id);
        scoringContext.lunchDownVotes = lunchContext.downVotes.get(place.id);
        final float score = place.score(scoringContext);
        final PlaceScore placeScore = new PlaceScore();
        placeScore.place = place;
        placeScore.score = score;
        return placeScore;
    }

}
