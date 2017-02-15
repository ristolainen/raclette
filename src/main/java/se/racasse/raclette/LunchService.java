package se.racasse.raclette;

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

    Collection<String> getLunchTimeParticipants(LocalDate date) {
        return lunchDao.getLunchParticipants(date).stream()
                .map(personId -> personService.getPerson(personId).name)
                .collect(toList());
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
    }

    Optional<Place> suggestLunchPlace(LocalDate date) {
        final Collection<Place> places = placeService.getAllPlaces();
        final Collection<Person> persons = personService.getParticipatingPersons(date);
        final Map<Integer, LocalDate> latestLunches = getLatestLunches(places);
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(places, persons, latestLunches);
        final Optional<Place> place = lunchSuggestor.suggest();
        this.latestSuggestedPlace = place;
        return place;
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

    void addLunchVote(String name, LocalDate lunchTime, int placeId, VoteType type) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        lunchDao.insertLunchVote(person.id, lunchTime, placeId, type);
    }
}
