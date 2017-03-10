package se.racasse.raclette.place;

import org.springframework.stereotype.Component;
import se.racasse.raclette.tag.Tag;
import se.racasse.raclette.vote.VoteType;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PlaceService {

    private final PlaceDao dao;

    PlaceService(PlaceDao dao) {
        this.dao = dao;
    }

    public Collection<Place> getAllPlaces() {
        return dao.getAllPlaces().stream()
                .map(this::populatePlace)
                .collect(Collectors.toSet());
    }

    public Optional<Place> getPlaceByName(String name) {
        return dao.getPlaceByName(name)
                .flatMap(place -> Optional.of(populatePlace(place)));
    }

    private Place populatePlace(Place place) {
        place.tags = dao.getPlaceTags(place.id);
        place.upVotes = dao.getPlaceUpVotes(place.id);
        place.downVotes = dao.getPlaceDownVotes(place.id);
        return place;
    }

    public int addPlace(Place place) {
        return dao.insertPlace(place);
    }

    public void addPlaceTag(int placeId, String tag) {
        if (!dao.getPlaceTags(placeId).contains(new Tag(tag))) {
            dao.insertPlaceTag(placeId, tag);
        }
    }

    public void removePlaceTag(int placeId, String tag) {
        dao.deletePlaceTag(placeId, tag);
    }

    public void addVote(int personId, int placeId, VoteType type) {
        dao.insertVote(personId, placeId, type);
    }
}
