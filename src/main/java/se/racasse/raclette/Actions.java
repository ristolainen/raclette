package se.racasse.raclette;

import se.racasse.raclette.lunch.AddLunchParticipantResponse;
import se.racasse.raclette.lunch.CreateLunchTimeResponse;
import se.racasse.raclette.lunch.GetLunchStatusResponse;
import se.racasse.raclette.lunch.LunchPlaceDecisionResponse;
import se.racasse.raclette.lunch.RemoveLunchParticipantResponse;
import se.racasse.raclette.person.AddPersonResponse;
import se.racasse.raclette.person.GetPersonResponse;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.person.PersonTagResponse;
import se.racasse.raclette.place.AddPlaceResponse;
import se.racasse.raclette.place.GetAllPlacesResponse;
import se.racasse.raclette.place.GetPlaceResponse;
import se.racasse.raclette.place.PlaceTagResponse;
import se.racasse.raclette.tag.TagType;
import se.racasse.raclette.vote.AddVoteResponse;
import se.racasse.raclette.vote.VoteType;

import java.util.Collection;
import java.util.function.Consumer;

public interface Actions {

    public interface AutomaticParticipantProvider {
        Collection<String> getParticipantsToBeAdded();
        void participantAdded(Person participant);
    }

    void setAutomaticParticipantProvider(DefaultActions.AutomaticParticipantProvider automaticParticipantProvider);

    void setCreatedLunchTimeCallback(Consumer<CreateLunchTimeResponse> createdLunchTimeCallback);

    GetPlaceResponse getPlace(String name);

    GetAllPlacesResponse getAllPlaces();

    AddPlaceResponse addPlace(String name);

    GetPersonResponse getPerson(String name);

    AddPersonResponse addPerson(String name);

    CreateLunchTimeResponse createLunchTimeForToday();

    AddLunchParticipantResponse addLunchParticipant(String name);

    RemoveLunchParticipantResponse removeLunchParticipant(String name);

    GetLunchStatusResponse getLunchStatus();

    LunchPlaceDecisionResponse decideSuggestedLunchPlace();

    LunchPlaceDecisionResponse decideSpecificLunchPlace(String name);

    AddVoteResponse addVote(String personName, String placeName, VoteType type);

    AddVoteResponse addLunchVote(String personName, String placeName, VoteType type);

    PlaceTagResponse addPlaceTag(String name, String tag);

    PlaceTagResponse removePlaceTag(String name, String tag);

    PersonTagResponse addPersonTag(String name, String tag, TagType type);

    PersonTagResponse removePersonTag(String name, String tag, TagType type);

}
