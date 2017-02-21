package se.racasse.raclette;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
class RestApi {

    private final PlaceService placeService;
    private final LunchService lunchService;
    private final PersonService personService;

    @Autowired
    RestApi(PlaceService placeService, LunchService lunchService, PersonService personService) {
        this.placeService = placeService;
        this.lunchService = lunchService;
        this.personService = personService;
    }

    @GetMapping("places")
    public List<Place> getAllPlaces() {
        return ImmutableList.copyOf(placeService.getAllPlaces());
    }

    @PostMapping("places")
    public int addPlace(@RequestBody Place place) {
        return placeService.addPlace(place);
    }

    @PostMapping("persons")
    public int addPerson(@RequestBody String name) {
        return personService.addPerson(name);
    }

    @PostMapping("lunchtimes")
    public void addLunchTime(@RequestBody String date) {
        lunchService.addLunchTime(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @PutMapping("lunchtimes/{date}/participants/{name}")
    public void addLunchTimeParticipant(@PathVariable String date, @PathVariable String name) {
        final LocalDate lunchTimeDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        lunchService.addLunchTimeParticipant(lunchTimeDate, name);
    }

    @PutMapping("lunches/{date}")
    public int setLunch(@PathVariable String date) {
        final LocalDate lunchDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        final Optional<PlaceScore> placeScore = lunchService.suggestLunchPlace(lunchDate);
        placeScore.ifPresent(p -> lunchService.setLunchPlace(lunchDate, p.place.id));
        if (placeScore.isPresent()) {
            return placeScore.get().place.id;
        }
        return 0;
    }
}
