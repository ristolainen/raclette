package se.racasse.raclette;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Component
class LunchService {

    private final LunchDao lunchDao;
    private final PersonService personService;
    private final PlaceService placeService;

    @Autowired
    LunchService(LunchDao lunchDao, PersonService personService, PlaceService placeService) {
        this.lunchDao = lunchDao;
        this.personService = personService;
        this.placeService = placeService;
    }

    void addLunchTime(LocalDate date) {
        lunchDao.insertLunchTime(date);
    }

    void addLunchTimeParticipant(LocalDate date, String name) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        lunchDao.insertLunchParticipant(date, person.id);
    }

    Optional<Place> resolveLunch(LocalDate date) {
        final Collection<Place> places = placeService.getAllPlaces();
        final Collection<Person> persons = personService.getParticipatingPersons(date);
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(places, persons);
        final Optional<Place> place = lunchSuggestor.suggest();
        place.ifPresent(p -> {
            lunchDao.setLunch(date, p);
        });
        return place;
    }
}
