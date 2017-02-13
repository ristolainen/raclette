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
        return dao.getAllPlaces().stream().map(place -> {
            place.tags = dao.getPlaceTags(place.id);
            place.upVotes = dao.getPlaceUpVotes(place.id);
            place.downVotes = dao.getPlaceDownVotes(place.id);
            return place;
        }).collect(Collectors.toSet());
    }

    Optional<Place> getPlaceByName(String name) {
        return dao.getPlaceByName(name).flatMap(place -> {
            place.tags = dao.getPlaceTags(place.id);
            place.upVotes = dao.getPlaceUpVotes(place.id);
            place.downVotes = dao.getPlaceDownVotes(place.id);
            return Optional.of(place);
        });
    }

    int addPlace(Place place) {
        return dao.insertPlace(place);
    }

    void addPlaceTag(int placeId, String tag) {
        if (!dao.getPlaceTags(placeId).contains(new Tag(tag))) {
            dao.insertPlaceTag(placeId, tag);
        }
    }
}
