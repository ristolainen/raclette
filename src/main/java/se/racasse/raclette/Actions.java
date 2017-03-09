package se.racasse.raclette;

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
