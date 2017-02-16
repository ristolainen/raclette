package se.racasse.raclette;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
class LunchService {

    private final LunchDao lunchDao;
    private final PersonService personService;
    private final PlaceService placeService;

    private Optional<Place> latestSuggestedPlace = Optional.empty();

    @Autowired
    LunchService(LunchDao lunchDao, PersonService personService, PlaceService placeService) {
        this.lunchDao = lunchDao;
        this.personService = personService;
        this.placeService = placeService;
    }

    LocalDate getCurrentLunchTime() {
        return lunchDao.getLatestLunchTime();
    }

    void addLunchTime(LocalDate date) {
        lunchDao.insertLunchTime(date);
    }

    boolean isLunchTimeParticipant(LocalDate date, String name) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        return lunchDao.isParticipant(date, person.id);
    }

    Collection<Person> getLunchTimeParticipants(LocalDate date) {
        return lunchDao.getLunchParticipants(date).stream()
                .map(personId -> personService.getPerson(personId))
                .collect(toList());
    }

    Multimap<Integer, Vote> getLunchTimeVotesByPlace(LocalDate date) {
        final Collection<Integer> placeIds = placeService.getAllPlaces().stream().map(p -> p.id).collect(toList());
        return Multimaps.index(lunchDao.getLunchVotesByPlaces(date, placeIds), vote -> vote.placeId);
    }

    void addLunchTimeParticipant(LocalDate date, String name) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        if (!lunchDao.isParticipant(date, person.id)) {
            lunchDao.insertLunchParticipant(date, person.id);
        }
    }

    void removeLunchTimeParticipant(LocalDate date, String name) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        lunchDao.removeLunchParticipant(date, person.id);
        lunchDao.removeLunchVotes(date, person.id);
    }

    Optional<Place> suggestLunchPlace(LocalDate date) {
        final LunchContext lunchContext = new LunchContext();
        lunchContext.places = placeService.getAllPlaces();
        lunchContext.participants = personService.getParticipatingPersons(date);
        lunchContext.latestLunches = getLatestLunches(lunchContext.places);
        lunchContext.upVotes = getLunchVotesPerParticipant(date, VoteType.UP, lunchContext.participants);
        lunchContext.downVotes = getLunchVotesPerParticipant(date, VoteType.DOWN, lunchContext.participants);
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(lunchContext);
        final Optional<Place> place = lunchSuggestor.suggest();
        this.latestSuggestedPlace = place;
        return place;
    }

    private Multimap<Integer, Vote> getLunchVotesPerParticipant(LocalDate date, VoteType voteType, Collection<Person> participants) {
        final Collection<Integer> participantIds = participants.stream().map(p -> p.id).collect(toList());
        return Multimaps.index(lunchDao.getLunchVotesByPersons(date, voteType, participantIds), vote -> vote.placeId);
    }

    Optional<Place> getLatestSuggestedPlace() {
        return latestSuggestedPlace;
    }

    void setLunchPlace(LocalDate date, int placeId) {
        lunchDao.setLunch(date, placeId);
    }

    private Map<Integer, LocalDate> getLatestLunches(Collection<Place> places) {
        final Map<Integer, LocalDate> lunches = new HashMap<>();
        places.forEach(place -> {
            final Optional<LocalDate> latestLunch = lunchDao.getLatestLunchForPlace(place.id);
            latestLunch.ifPresent(date -> {
                lunches.put(place.id, date);
            });
        });
        return lunches;
    }

    void addLunchVote(String personName, LocalDate lunchTime, int placeId, VoteType type) {
        final Person person = personService.getPersonByName(personName)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", personName)));
        lunchDao.insertLunchVote(person.id, lunchTime, placeId, type);
    }
}
