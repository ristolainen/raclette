package se.racasse.raclette.slack;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import se.racasse.raclette.Actions;
import se.racasse.raclette.lunch.AddLunchParticipantResponse;
import se.racasse.raclette.lunch.CreateLunchTimeResponse;
import se.racasse.raclette.lunch.GetLunchStatusResponse;
import se.racasse.raclette.lunch.LunchPlaceDecisionResponse;
import se.racasse.raclette.lunch.RemoveLunchParticipantResponse;
import se.racasse.raclette.lunch.SuggestResult;
import se.racasse.raclette.person.AddPersonResponse;
import se.racasse.raclette.person.GetPersonResponse;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.person.PersonTagResponse;
import se.racasse.raclette.place.AddPlaceResponse;
import se.racasse.raclette.place.GetAllPlacesResponse;
import se.racasse.raclette.place.GetPlaceResponse;
import se.racasse.raclette.place.Place;
import se.racasse.raclette.place.PlaceScore;
import se.racasse.raclette.place.PlaceTagResponse;
import se.racasse.raclette.tag.TagType;
import se.racasse.raclette.vote.AddVoteResponse;
import se.racasse.raclette.vote.Vote;
import se.racasse.raclette.vote.VoteType;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
@Profile("slack")
public class SlackCommandHandler {

    private final static Logger LOG = LoggerFactory.getLogger(SlackCommandHandler.class);

    private final SlackSession session;
    private final Actions actions;

    private SlackChannel lunchChannel;

    public SlackCommandHandler(SlackSession session, Actions actions) {
        this.session = session;
        this.actions = actions;
    }

    @PostConstruct
    public void init() {
        lunchChannel = session.findChannelByName("lunch");

        actions.setAutomaticParticipantProvider(new Actions.AutomaticParticipantProvider() {
            @Override
            public Collection<String> getParticipantsToBeAdded() {
                return session.getUsers()
                        .stream()
                        .filter(u -> u.getPresence().name().equals("ACTIVE"))
                        .map(SlackUser::getUserName)
                        .collect(toList());
            }

            @Override
            public void participantAdded(Person participant) {
                sendMessage(lunchChannel, String.format("%s is a member of today's lunch gang", participant.name));
            }
        });

        actions.setCreatedLunchTimeCallback(response -> {
            sendMessage(lunchChannel, "Creating new lunch time for " + response.lunchTime.format(DateTimeFormatter.ISO_DATE));
        });
    }

    void handleCommand(SlackUser sender, SlackChannel channel, String cmd, List<String> params) {
        LOG.debug(sender.getUserName() + ": " + cmd + " " + params);
        switch (cmd) {
            case "help":
                handleHelpCommand(sender, channel, params);
                return;
            case "get":
                handleGetCommand(sender, channel, params);
                return;
            case "add":
                handleAddCommand(sender, channel, params);
                return;
            case "lunch":
                handleLunchCommand(sender, channel, params);
                return;
            case "vote":
                handleVoteCommand(sender, channel, params);
                return;
            case "tag":
                handleTagCommand(sender, channel, params);
                return;
            case "untag":
                handleUntagCommand(sender, channel, params);
                return;
            case "test":
                handleTestCommand(sender, channel, params);
                return;
        }
    }

    private void handleHelpCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add("`get` get information about stuff");
        msg.add("• `get places` list all places");
        msg.add("• `get place [name]` get info about a place");
        msg.add("• `get me` get info about me");
        msg.add("• `get person [name]` get info about someone");
        msg.add("`lunch` about todays lunch");
        msg.add("• `lunch status` current status for today's lunch");
        msg.add("• `lunch add me` include me in today's lunch gang");
        msg.add("• `lunch add [name]` include someone in today's lunch gang");
        msg.add("• `lunch remove me` exclude me from today's lunch gang");
        msg.add("• `lunch remove [name]` exclude someone from today's lunch gang");
        msg.add("• `lunch vote [place] up` give a place an up-vote for this lunch");
        msg.add("• `lunch vote [place] down` give a place a down-vote for this lunch");
        msg.add("• `lunch decide` decide the latest suggested place as today's lunch place");
        msg.add("• `lunch decide [name]` decide a specific place as today's lunch place");
        msg.add("`vote` vote for places");
        msg.add("• `vote [place] up` give a place an up-vote");
        msg.add("• `vote [place] down` give a place a down-vote");
        msg.add("`tag` do some tagging");
        msg.add("• `tag place [name] [tag]` tag a place, e.g: `tag place guldfisken sockerchock`");
        msg.add("• `tag prefer [tag]` add a prefer tag to me, e.g: `tag prefer fredagsburgare`");
        msg.add("• `tag require [tag]` add a require tag to me, e.g: `tag require vegetariskt`");
        msg.add("`untag` remove tags");
        msg.add("• `untag place [name] [tag]` remove a tag from a place");
        msg.add("• `untag prefer [tag]` remove a prefer tag to me");
        msg.add("• `untag require [tag]` remove a require tag to me");
        msg.add("`add` add stuff");
        msg.add("• `add place [name]` add a place, keep it to one lower case word for simplicity");
        msg.add("• `add me` add me as a person @raclette knows about");
        sendMultilineMessage(channel, msg.build());
    }

    private void handleGetCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "me":
                handleGetUser(channel, sender.getUserName());
                return;
            case "place":
                handleGetPlace(sender, channel, params.get(1));
                return;
            case "places":
                handleGetPlaces(sender, channel);
                return;
            case "person":
                handleGetUser(channel, params.get(1));
                return;
        }
        sendMessage(channel, "What?");
    }

    private void handleGetPlaces(SlackUser sender, SlackChannel channel) {
        final GetAllPlacesResponse response = actions.getAllPlaces();
        if (response.successful()) {
            sendMultilineMessage(channel,
                    response.places.stream()
                            .map(p -> String.format("• %s - [%s]", p.name, Joiner.on(',').join(p.tags)))
                            .collect(toList()));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleGetPlace(SlackUser sender, SlackChannel channel, String name) {
        final GetPlaceResponse response = actions.getPlace(name);
        if (!response.successful()) {
            sendMessage(channel, response.errorMessage);
            return;
        }
        final Place p = response.place.get();
        final List<String> tags = p.tags.stream().map(t -> "• " + t.name).collect(toList());

        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add(String.format("*%s*", p.name));
        msg.add(">>>");
        msg.add("Tags");
        msg.addAll(tags);
        msg.add("Votes");
        if (p.upVotes.size() > 0) {
            msg.add(voteTypeToEmoji(VoteType.UP) + " " + p.upVotes.stream()
                    .map(v -> v.personName)
                    .collect(toList()));
        }
        if (p.downVotes.size() > 0) {
            msg.add(voteTypeToEmoji(VoteType.DOWN) + " " + p.downVotes.stream()
                    .map(v -> v.personName)
                    .collect(toList()));
        }
        sendMultilineMessage(channel, msg.build());
    }

    private void handleGetUser(SlackChannel channel, String name) {
        final GetPersonResponse response = actions.getPerson(name);
        if (!response.successful()) {
            sendMessage(channel, response.errorMessage);
            return;
        }
        final Person p = response.person.get();
        final List<String> prefers = p.preferredTags.stream().map(t -> "• " + t.name).collect(toList());
        final List<String> requires = p.requiredTags.stream().map(t -> "• " + t.name).collect(toList());

        final Multimap<String, Vote> placeVotesPerPlace = Multimaps.index(p.placeVotes, v -> v.placeName);

        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add(String.format("*%s*", p.name));
        msg.add(">>>");
        if (prefers.size() > 0) {
            msg.add("Prefers");
            msg.addAll(prefers);
        }
        msg.add("");
        if (requires.size() > 0) {
            msg.add("Requires");
            msg.addAll(requires);
        }
        if (p.placeVotes.size() > 0) {
            msg.add("Votes");
            placeVotesPerPlace.keySet().forEach(placeName -> {
                final Collection<Vote> votes = placeVotesPerPlace.get(placeName);
                final long upvotes = votes.stream().filter(v -> v.type == VoteType.UP).count();
                final long downvotes = votes.stream().filter(v -> v.type == VoteType.DOWN).count();
                String s = "• " + placeName + ": ";
                if (upvotes > 0) {
                    s += String.format("%d :thumbsup: ", upvotes);
                }
                if (downvotes > 0) {
                    s += String.format("%d :thumbsdown: ", downvotes);
                }
                msg.add(s);
            });
        }
        sendMultilineMessage(channel, msg.build());
    }

    private void handleVoteCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        final String placeName = params.get(0);
        final VoteType type = VoteType.valueOf(params.get(1).toUpperCase());
        final String me = sender.getUserName();
        final AddVoteResponse response = actions.addVote(me, placeName, type);
        if (response.successful()) {
            sendMessage(channel, String.format("Added %s-vote for %s", type.name().toLowerCase(), response.place.get().name));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleTagCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "place":
                handlePlaceTag(sender, channel, params);
                return;
            case "prefer":
                handlePersonTag(sender, channel, params, TagType.PREFER);
                return;
            case "require":
                handlePersonTag(sender, channel, params, TagType.REQUIRE);
                return;
        }
        sendMessage(channel, "What?");
    }

    private void handleUntagCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "place":
                handlePlaceUntag(sender, channel, params);
                return;
            case "prefer":
                handlePersonUntag(sender, channel, params, TagType.PREFER);
                return;
            case "require":
                handlePersonUntag(sender, channel, params, TagType.REQUIRE);
                return;
        }
        sendMessage(channel, "What?");
    }

    private void handlePlaceTag(SlackUser sender, SlackChannel channel, List<String> params) {
        final String name = params.get(1);
        final String tag = params.get(2);
        final PlaceTagResponse response = actions.addPlaceTag(name, tag);
        if (response.successful()) {
            sendMessage(channel, String.format("%s is now tagged with '%s'", name, tag));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handlePersonTag(SlackUser sender, SlackChannel channel, List<String> params, TagType type) {
        final String tag = params.get(1);
        String me = sender.getUserName();
        final PersonTagResponse response = actions.addPersonTag(me, tag, type);
        if (response.successful()) {
            sendMessage(channel, String.format("You now %s %s", type.name().toLowerCase(), tag));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handlePlaceUntag(SlackUser sender, SlackChannel channel, List<String> params) {
        final String name = params.get(1);
        final String tag = params.get(2);
        final PlaceTagResponse response = actions.removePlaceTag(name, tag);
        if (response.successful()) {
            sendMessage(channel, String.format("Tag '%s' was removed from %s", tag, name));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handlePersonUntag(SlackUser sender, SlackChannel channel, List<String> params, TagType type) {
        final String tag = params.get(1);
        String me = sender.getUserName();
        final PersonTagResponse response = actions.removePersonTag(me, tag, type);
        if (response.successful()) {
            sendMessage(channel, String.format("You no longer %s %s", type.name().toLowerCase(), tag));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleDecideCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        final LunchPlaceDecisionResponse response;
        if (params.size() > 1) {
            final String name = params.get(1);
            response = actions.decideSpecificLunchPlace(name);
        } else {
            response = actions.decideSuggestedLunchPlace();
        }
        if (response.successful()) {
            sendMessage(channel, String.format("Today's lunch will be at *%s*", response.decidedPlace.get().name));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleLunchCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "status":
                handleLunchStatus(sender, channel);
                return;
            case "add":
                handleLunchAdd(sender, channel, params);
                return;
            case "remove":
                handleLunchRemove(sender, channel, params);
                return;
            case "vote":
                handleLunchVote(sender, channel, params);
                return;
            case "decide":
                handleDecideCommand(sender, channel, params);
                return;
        }
        sendMessage(channel, "What?");
    }

    private void handleLunchVote(SlackUser sender, SlackChannel channel, List<String> params) {
        final String me = sender.getUserName();
        final String placeName = params.get(1);
        final VoteType type = VoteType.valueOf(params.get(2).toUpperCase());
        final AddVoteResponse response = actions.addLunchVote(me, placeName, type);
        if (response.successful()) {
            sendMessage(channel, String.format("Added lunch %s-vote for %s", type.name().toLowerCase(), placeName));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleLunchAdd(SlackUser sender, SlackChannel channel, List<String> params) {
        final String name;
        if (params.get(1).equals("me")) {
            addParticipant(channel, sender.getUserName());
        } else {
            addParticipant(channel, params.get(1));
        }
    }

    private void addParticipant(SlackChannel channel, String name) {
        final AddLunchParticipantResponse response = actions.addLunchParticipant(name);
        if (response.successful()) {
            sendMessage(channel, String.format("%s is a member of today's lunch gang", name));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void removeParticipant(SlackChannel channel, String userName) {
        final RemoveLunchParticipantResponse response = actions.removeLunchParticipant(userName);
        if (response.successful()) {
            sendMessage(channel, String.format("%s is not a member of today's lunch gang", userName));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleLunchRemove(SlackUser sender, SlackChannel channel, List<String> params) {
        if (params.get(1).equals("me")) {
            removeParticipant(channel, sender.getUserName());
        } else {
            final String name = params.get(1);
            removeParticipant(channel, name);
        }
    }

    private void handleLunchStatus(SlackUser sender, SlackChannel channel) {
        final GetLunchStatusResponse response = actions.getLunchStatus();
        final Map<Integer, Person> participants = Maps.uniqueIndex(response.participants, p -> p.id);

        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add(String.format("Lunch status for *%s*", response.lunchTime.format(DateTimeFormatter.ISO_DATE)));
        final SuggestResult suggestion = response.suggestResult;
        suggestion.top().ifPresent(score -> msg.add(String.format("SUGGESTED PLACE: *%s* (score %.2f)",
                score.place.name, score.score)));
        if (participants.size() > 0) {
            msg.add("*Participants*");
            msg.addAll(participants.values().stream().map(person -> "• " + person.name).collect(toList()));
        } else {
            msg.add("*No participants*");
        }
        if (response.votesByPlace.size() > 0) {
            msg.add("*Votes*");
            response.places.forEach(place -> {
                final Collection<Vote> votes = response.votesByPlace.get(place.id);
                if (votes.size() > 0) {
                    msg.add("• " + place.name);
                    votes.forEach(vote -> {
                        final Person person = participants.get(vote.personId);
                        msg.add("    • " + person.name + " " + voteTypeToEmoji(vote.type));
                    });
                }
            });
        }
        if (suggestion.scores.size() > 0) {
            msg.add("*Scores*");
            for (int i = 0; i < Math.min(suggestion.scores.size(), 10); i++) {
                final PlaceScore score = suggestion.scores.get(i);
                msg.add(String.format("%d. %s (%.2f)", i + 1, score.place.name, score.score));
            }
        }
        sendMultilineMessage(channel, msg.build());
    }

    private String voteTypeToEmoji(VoteType voteType) {
        switch (voteType) {
            case UP:
                return ":thumbsup:";
            case DOWN:
                return ":thumbsdown:";
        }
        throw new IllegalStateException();
    }

    private void handleAddCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "place":
                handleAddPlace(sender, channel, params);
                return;
            case "me":
                handleAddMe(sender, channel);
                return;
            case "lunchtime":
                createLunchTimeForToday();
                return;
            case "participants":
                addParticipants();
                return;
        }
        sendMessage(channel, "What?");
    }

    private void handleAddMe(SlackUser sender, SlackChannel channel) {
        String name = sender.getUserName();
        final AddPersonResponse response = actions.addPerson(name);
        if (response.successful()) {
            sendMessage(channel, String.format("Welcome %s!", response.person.name));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleAddPlace(SlackUser sender, SlackChannel channel, List<String> params) {
        final AddPlaceResponse response = actions.addPlace(params.get(1));
        if (response.successful()) {
            sendMessage(channel, String.format("Place '%s' is now created", response.place.name));
        } else {
            sendMessage(channel, response.errorMessage);
        }
    }

    private void handleTestCommand(SlackUser sender, SlackChannel channel, List<String> params) {
        session.refetchUsers();
        session.getUsers().forEach(u -> {
            String s = u.getUserName() + ": " + u.getPresence().name();
            sendMessage(channel, s);
        });
    }

    private void addParticipants() {
        session.refetchUsers();
        session.getUsers().forEach(u -> {
            if (u.getPresence().name().equals("ACTIVE")) {
                if (actions.getPerson(u.getUserName()).person.isPresent()) {
                    addParticipant(lunchChannel, u.getUserName());
                }
            }
        });
    }

    private void createLunchTimeForToday() {
        final CreateLunchTimeResponse response = actions.createLunchTimeForToday();
        if (response.successful()) {
            sendMessage(lunchChannel, "Creating new lunch time for " + response.lunchTime.format(DateTimeFormatter.ISO_DATE));
        } else {
            sendMessage(lunchChannel, response.errorMessage);
        }
    }

    private void sendLunchMessage(String msg) {
        sendMessage(lunchChannel, msg);
    }

    private void sendMessage(SlackChannel channel, String msg) {
        session.sendMessage(channel, msg);
    }

    private void sendMultilineMessage(SlackChannel channel, Iterable<String> msg) {
        session.sendMessage(channel, Joiner.on('\n').join(msg));
    }
}
