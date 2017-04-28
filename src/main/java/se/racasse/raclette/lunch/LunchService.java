package se.racasse.raclette.lunch;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.person.PersonService;
import se.racasse.raclette.place.PlaceService;
import se.racasse.raclette.vote.Vote;
import se.racasse.raclette.vote.VoteType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class LunchService {

    private final LunchDao lunchDao;
    private final PersonService personService;
    private final PlaceService placeService;

    private Optional<SuggestResult> latestSuggestion = Optional.empty();

    @Autowired
    LunchService(LunchDao lunchDao, PersonService personService, PlaceService placeService) {
        this.lunchDao = lunchDao;
        this.personService = personService;
        this.placeService = placeService;
    }

    public LocalDate getCurrentLunchTime() {
        return lunchDao.getLatestLunchTime();
    }

    public void addLunchTime(LocalDate date) {
        lunchDao.insertLunchTime(date);
    }

    public boolean isLunchTimeParticipant(LocalDate date, String name) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        return lunchDao.isParticipant(date, person.id);
    }

    public Collection<Person> getLunchTimeParticipants(LocalDate date) {
        return lunchDao.getLunchParticipants(date).stream()
                .map(personService::getPerson)
                .collect(toList());
    }

    public Multimap<Integer, Vote> getLunchTimeVotesByPlace(LocalDate date) {
        final Collection<Integer> placeIds = placeService.getAllPlaces().stream().map(p -> p.id).collect(toList());
        return Multimaps.index(lunchDao.getLunchVotesByPlaces(date, placeIds), vote -> vote.placeId);
    }

    public void addLunchTimeParticipant(LocalDate date, String name) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        if (!lunchDao.isParticipant(date, person.id)) {
            lunchDao.insertLunchParticipant(date, person.id);
        }
    }

    public void removeLunchTimeParticipant(LocalDate date, String name) {
        final Person person = personService.getPersonByName(name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User '%s' not found", name)));
        lunchDao.removeLunchParticipant(date, person.id);
        lunchDao.removeLunchVotes(date, person.id);
    }

    public SuggestResult suggestLunchPlace(LocalDate date) {
        final LunchContext lunchContext = new LunchContext();
        lunchContext.places = placeService.getAllPlaces();
        lunchContext.participants = personService.getParticipatingPersons(date);
        lunchContext.upVotes = getLunchVotesPerParticipant(date, VoteType.UP, lunchContext.participants);
        lunchContext.downVotes = getLunchVotesPerParticipant(date, VoteType.DOWN, lunchContext.participants);
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(lunchContext);
        final SuggestResult place = lunchSuggestor.suggest();
        this.latestSuggestion = Optional.of(place);
        return place;
    }

    private Multimap<Integer, Vote> getLunchVotesPerParticipant(LocalDate date, VoteType voteType, Collection<Person> participants) {
        final Collection<Integer> participantIds = participants.stream().map(p -> p.id).collect(toList());
        if (participantIds.isEmpty()) {
            return ImmutableMultimap.of();
        }
        return Multimaps.index(lunchDao.getLunchVotesByPersons(date, voteType, participantIds), vote -> vote.placeId);
    }

    public Optional<SuggestResult> getLatestSuggestion() {
        return latestSuggestion;
    }

    public void setLunchPlace(LocalDate date, int placeId) {
        lunchDao.setLunch(date, placeId);
    }

    public boolean lunchVoteExists(int personId, LocalDate lunchTime, int placeId, VoteType type) {
        Optional<Vote> vote = lunchDao.getLunchVoteForPerson(personId, lunchTime, placeId);
        return vote.isPresent() && vote.get().type == type;
    }

    public void removeLunchVote(int personId, LocalDate lunchTime, int placeId) {
        lunchDao.removeLunchVote(personId, lunchTime, placeId);
    }

    public void addLunchVote(int personId, LocalDate lunchTime, int placeId, VoteType type) {
        lunchDao.insertLunchVote(personId, lunchTime, placeId, type);
    }
}
