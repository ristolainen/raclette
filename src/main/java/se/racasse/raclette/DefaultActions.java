package se.racasse.raclette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class DefaultActions implements Actions {
    private final static Logger LOG = LoggerFactory.getLogger(DefaultActions.class);

    private final LunchService lunchService;
    private final PersonService personService;
    private final PlaceService placeService;

    private AutomaticParticipantProvider automaticParticipantProvider;
    private Consumer<CreateLunchTimeResponse> createdLunchTimeCallback;

    public DefaultActions(LunchService lunchService, PersonService personService, PlaceService placeService) {
        this.lunchService = lunchService;
        this.personService = personService;
        this.placeService = placeService;
    }

    @Override
    public void setAutomaticParticipantProvider(AutomaticParticipantProvider automaticParticipantProvider) {
        this.automaticParticipantProvider = automaticParticipantProvider;
    }

    @Override
    public void setCreatedLunchTimeCallback(Consumer<CreateLunchTimeResponse> createdLunchTimeCallback) {
        this.createdLunchTimeCallback = createdLunchTimeCallback;
    }

    @Scheduled(cron = "0 0 10 * * MON-FRI")
    public void addAutomaticParticipants() {
        if (automaticParticipantProvider != null) {
            LOG.info("Adding automatic participants");
            automaticParticipantProvider.getParticipantsToBeAdded().forEach(name -> {
                final AddLunchParticipantResponse response = addLunchParticipant(name);
                if (response.successful()) {
                    automaticParticipantProvider.participantAdded(response.participant.get());
                }
            });
        } else {
            LOG.info("No automatic participant provider registered");
        }
    }

    @Scheduled(cron = "0 0 1 * * MON-FRI")
    public void automaticLunchTimeCreation() {
        LOG.info("Creating today's lunch time");
        final CreateLunchTimeResponse response = createLunchTimeForToday();
        if (response.successful() && createdLunchTimeCallback != null) {
            createdLunchTimeCallback.accept(response);
        }
    }

    @Override
    public GetPlaceResponse getPlace(String name) {
        final Optional<Place> place = placeService.getPlaceByName(name);
        final GetPlaceResponse response = new GetPlaceResponse(place.isPresent());
        if (!place.isPresent()) {
            response.errorMessage = String.format("I known no place called '%s'", name);
        }
        response.place = place;
        return response;
    }

    @Override
    public GetAllPlacesResponse getAllPlaces() {
        final Collection<Place> places = placeService.getAllPlaces();
        final GetAllPlacesResponse response = new GetAllPlacesResponse(true);
        response.places = places;
        return response;
    }

    @Override
    public AddPlaceResponse addPlace(String name) {
        final Optional<Place> existingPlace = placeService.getPlaceByName(name);
        if (existingPlace.isPresent()) {
            final AddPlaceResponse response = new AddPlaceResponse(false);
            response.errorMessage = String.format("There is already a place called '%s'", name);
            return response;
        }
        final Place place = new Place();
        place.name = name;
        place.id = placeService.addPlace(place);
        final AddPlaceResponse response = new AddPlaceResponse(true);
        response.place = place;
        return response;
    }

    @Override
    public GetPersonResponse getPerson(String name) {
        final Optional<Person> person = personService.getPersonByName(name);
        final GetPersonResponse response = new GetPersonResponse(person.isPresent());
        if (!person.isPresent()) {
            response.errorMessage = String.format("I know no person called '%s'", name);
        }
        response.person = person;
        return response;
    }

    @Override
    public AddPersonResponse addPerson(String name) {
        final Optional<Person> person = personService.getPersonByName(name);
        if (person.isPresent()) {
            final AddPersonResponse response = new AddPersonResponse(false);
            response.errorMessage = String.format("'%s' is already added", name);
            return response;
        }
        final AddPersonResponse response = new AddPersonResponse(true);
        response.person = personService.addPerson(name);
        return response;
    }

    @Override
    public CreateLunchTimeResponse createLunchTimeForToday() {
        final LocalDate date = LocalDate.now();
        final LocalDate currentLunchTime = lunchService.getCurrentLunchTime();
        if (date.equals(currentLunchTime)) {
            final CreateLunchTimeResponse response = new CreateLunchTimeResponse(false);
            response.errorMessage = String.format("There is already a lunch time for today (%s)", date.format(DateTimeFormatter.ISO_DATE));
            return response;
        }
        lunchService.addLunchTime(date);
        final CreateLunchTimeResponse response = new CreateLunchTimeResponse(true);
        response.lunchTime = date;
        return response;
    }

    @Override
    public AddLunchParticipantResponse addLunchParticipant(String name) {
        final Optional<Person> person = personService.getPersonByName(name);
        final AddLunchParticipantResponse response = new AddLunchParticipantResponse(person.isPresent());
        if (person.isPresent()) {
            final LocalDate lunchTime = lunchService.getCurrentLunchTime();
            lunchService.addLunchTimeParticipant(lunchTime, name);
        } else {
            response.errorMessage = String.format("I know no person called '%s'", name);
        }
        response.participant = person;
        return response;
    }

    @Override
    public RemoveLunchParticipantResponse removeLunchParticipant(String name) {
        final Optional<Person> person = personService.getPersonByName(name);
        final RemoveLunchParticipantResponse response = new RemoveLunchParticipantResponse(person.isPresent());
        if (person.isPresent()) {
            final LocalDate lunchTime = lunchService.getCurrentLunchTime();
            lunchService.removeLunchTimeParticipant(lunchTime, name);
        } else {
            response.errorMessage = String.format("I know no person called '%s'", name);
        }
        response.participant = person;
        return response;
    }

    @Override
    public GetLunchStatusResponse getLunchStatus() {
        final GetLunchStatusResponse response = new GetLunchStatusResponse(true);
        final LocalDate lunchTime = lunchService.getCurrentLunchTime();
        response.lunchTime = lunchTime;
        response.participants = lunchService.getLunchTimeParticipants(lunchTime);
        response.places = placeService.getAllPlaces();
        response.votesByPlace = lunchService.getLunchTimeVotesByPlace(lunchTime);
        response.suggestResult = lunchService.suggestLunchPlace(lunchTime);
        return response;
    }

    @Override
    public LunchPlaceDecisionResponse decideSuggestedLunchPlace() {
        final Optional<SuggestResult> suggestion = lunchService.getLatestSuggestion();
        if (!suggestion.isPresent() || !suggestion.get().top().isPresent()) {
            final LunchPlaceDecisionResponse response = new LunchPlaceDecisionResponse(false);
            response.errorMessage = "No place is suggested";
            return response;
        }
        final Place place = suggestion.get().top().get().place;
        return decideSpecificLunchPlace(place.name);
    }

    @Override
    public LunchPlaceDecisionResponse decideSpecificLunchPlace(String name) {
        final Optional<Place> place = placeService.getPlaceByName(name);
        final LunchPlaceDecisionResponse response = new LunchPlaceDecisionResponse(place.isPresent());
        if (place.isPresent()) {
            final LocalDate lunchTime = lunchService.getCurrentLunchTime();
            lunchService.setLunchPlace(lunchTime, place.get().id);
        } else {
            response.errorMessage = String.format("I know no place called '%s'", name);
        }
        response.decidedPlace = place;
        return response;
    }

    @Override
    public AddVoteResponse addVote(String personName, String placeName, VoteType type) {
        final Optional<Person> person = personService.getPersonByName(personName);
        if (!person.isPresent()) {
            final AddVoteResponse response = new AddVoteResponse(false);
            response.errorMessage = String.format("I know no person called '%s'", personName);
            return response;
        }
        final Optional<Place> place = placeService.getPlaceByName(placeName);
        if (!place.isPresent()) {
            final AddVoteResponse response = new AddVoteResponse(false);
            response.errorMessage = String.format("I know no place called '%s'", placeName);
            return response;
        }
        placeService.addVote(person.get().id, place.get().id, type);
        final AddVoteResponse response = new AddVoteResponse(true);
        response.person = person;
        response.place = place;
        return response;
    }

    @Override
    public AddVoteResponse addLunchVote(String personName, String placeName, VoteType type) {
        final Optional<Person> person = personService.getPersonByName(personName);
        if (!person.isPresent()) {
            final AddVoteResponse response = new AddVoteResponse(false);
            response.errorMessage = String.format("I know no person called '%s'", personName);
            return response;
        }
        final LocalDate lunchTime = lunchService.getCurrentLunchTime();
        if (!lunchService.isLunchTimeParticipant(lunchTime, personName)) {
            final AddVoteResponse response = new AddVoteResponse(false);
            response.errorMessage = String.format("%s must be a lunch participant to do lunch voting", personName);
            return response;
        }
        final Optional<Place> place = placeService.getPlaceByName(placeName);
        if (!place.isPresent()) {
            final AddVoteResponse response = new AddVoteResponse(false);
            response.errorMessage = String.format("I know no place called '%s'", placeName);
            return response;
        }
        lunchService.addLunchVote(person.get().id, lunchTime, place.get().id, type);
        final AddVoteResponse response = new AddVoteResponse(true);
        response.person = person;
        response.place = place;
        return response;
    }

    @Override
    public PlaceTagResponse addPlaceTag(String name, String tag) {
        final Optional<Place> place = placeService.getPlaceByName(name);
        final PlaceTagResponse response = new PlaceTagResponse(place.isPresent());
        if (place.isPresent()) {
            placeService.addPlaceTag(place.get().id, tag);
        } else {
            response.errorMessage = String.format("I know no place called '%s'", name);
        }
        response.place = place;
        return response;
    }

    @Override
    public PlaceTagResponse removePlaceTag(String name, String tag) {
        final Optional<Place> place = placeService.getPlaceByName(name);
        final PlaceTagResponse response = new PlaceTagResponse(place.isPresent());
        if (place.isPresent()) {
            placeService.removePlaceTag(place.get().id, tag);
        } else {
            response.errorMessage = String.format("I know no place called '%s'", name);
        }
        response.place = place;
        return response;
    }

    @Override
    public PersonTagResponse addPersonTag(String name, String tag, TagType type) {
        final Optional<Person> person = personService.getPersonByName(name);
        final PersonTagResponse response = new PersonTagResponse(person.isPresent());
        if (person.isPresent()) {
            personService.addTag(person.get().id, tag, type);
        } else {
            response.errorMessage = String.format("I know no person called '%s'", name);
        }
        response.person = person;
        return response;
    }

    @Override
    public PersonTagResponse removePersonTag(String name, String tag, TagType type) {
        final Optional<Person> person = personService.getPersonByName(name);
        final PersonTagResponse response = new PersonTagResponse(person.isPresent());
        if (person.isPresent()) {
            personService.removeTag(person.get().id, tag, type);
        } else {
            response.errorMessage = String.format("I know no person called '%s'", name);
        }
        response.person = person;
        return response;
    }

}
