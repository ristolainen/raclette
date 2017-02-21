package se.racasse.raclette;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class PlaceService {

    private final PlaceDao dao;

    PlaceService(PlaceDao dao) {
        this.dao = dao;
    }

    Collection<Place> getAllPlaces() {
        return dao.getAllPlaces().stream()
                .map(this::populatePlace)
                .collect(Collectors.toSet());
    }

    Place getPlace(int placeId) {
        return populatePlace(dao.getPlace(placeId));
    }

    Optional<Place> getPlaceByName(String name) {
        return dao.getPlaceByName(name)
                .flatMap(place -> Optional.of(populatePlace(place)));
    }

    private Place populatePlace(Place place) {
        place.tags = dao.getPlaceTags(place.id);
        place.upVotes = dao.getPlaceUpVotes(place.id);
        place.downVotes = dao.getPlaceDownVotes(place.id);
        return place;
    }

    int addPlace(Place place) {
        return dao.insertPlace(place);
    }

    void addPlaceTag(int placeId, String tag) {
        if (!dao.getPlaceTags(placeId).contains(new Tag(tag))) {
            dao.insertPlaceTag(placeId, tag);
        }
    }

    void removePlaceTag(int placeId, String tag) {
        dao.deletePlaceTag(placeId, tag);
    }

    void addVote(int personId, int placeId, VoteType type) {
        dao.insertVote(personId, placeId, type);
    }
}
