package se.racasse.raclette;

import org.springframework.stereotype.Component;

import java.util.Collection;
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

    int addPlace(Place place) {
        return dao.insertPlace(place);
    }
}
