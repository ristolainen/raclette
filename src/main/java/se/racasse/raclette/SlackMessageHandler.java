package se.racasse.raclette;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class SlackMessageHandler implements SlackMessagePostedListener {

    private final static Logger LOG = LoggerFactory.getLogger(SlackMessageHandler.class);

    private final SlackSession session;
    private final PlaceService placeService;
    private final PersonService personService;
    private final LunchService lunchService;

    private SlackChannel lunchChannel;

    @Autowired
    public SlackMessageHandler(SlackSession session, PlaceService placeService, PersonService personService, LunchService lunchService) {
        this.session = session;
        this.placeService = placeService;
        this.personService = personService;
        this.lunchService = lunchService;
    }

    @PostConstruct
    public void init() {
        session.addMessagePostedListener(this);
        lunchChannel = session.findChannelByName("random");
    }

    @Scheduled(cron = "0 0 1 * * MON-FRI")
    public void createLunchTimeForToday() {
        final LocalDate date = LocalDate.now();
        final LocalDate currentLunchTime = lunchService.getCurrentLunchTime();
        if (date.equals(currentLunchTime)) {
            sendLunchMessage("There is already a lunch time for today");
            return;
        }
        lunchService.addLunchTime(date);
        sendLunchMessage("Creating new lunch time for " + date.format(DateTimeFormatter.ISO_DATE));
    }

    @Scheduled(cron = "0 0 10 * * MON-FRI")
    public void addParticipants() {
        session.getUsers().forEach(u -> {
            if (u.getPresence().name().equals("ACTIVE")) {
                if (personService.getPersonByName(u.getUserName()).isPresent()) {
                    addParticipant(lunchChannel, u.getUserName());
                }
            }
        });
    }

    private void addParticipant(SlackChannel channel, String userName) {
        final LocalDate lunchTime = lunchService.getCurrentLunchTime();
        lunchService.addLunchTimeParticipant(lunchTime, userName);
        sendMessage(channel, String.format("%s is a member of today's lunch gang", userName));
    }

    private void removeParticipant(SlackChannel channel, String userName) {
        final LocalDate lunchTime = lunchService.getCurrentLunchTime();
        lunchService.removeLunchTimeParticipant(lunchTime, userName);
        sendMessage(channel, String.format("%s is not a member of today's lunch gang", userName));
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        if (isMessageFromMe(event)) {
            return;
        }
        if (isMessageForMe(event)) {
            try {
                final List<String> command = getCommand(event.getMessageContent());
                if (command.size() > 0) {
                    final String cmd = command.get(0).toLowerCase();
                    final ImmutableList<String> params = ImmutableList.copyOf(Iterables.skip(command, 1));
                    handleCommand(event, cmd, params);
                }
            } catch (Exception e) {
                LOG.error("Failed to handle slack event", e);
                sendMessage(event.getChannel(), e.getMessage());
            }
        }
    }

    private void handleCommand(SlackMessagePosted event, String cmd, List<String> params) {
        switch (cmd) {
            case "help":
                handleHelpCommand(event, params);
                return;
            case "get":
                handleGetCommand(event, params);
                return;
            case "add":
                handleAddCommand(event, params);
                return;
            case "lunch":
                handleLunchCommand(event, params);
                return;
            case "tag":
                handleTagCommand(event, params);
                return;
        }
        sendMessage(event.getChannel(), "What?");
    }

    private void handleHelpCommand(SlackMessagePosted event, List<String> params) {
        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add("`get` get information about stuff");
        msg.add("• `get places` list all places");
        msg.add("• `get place [name]` get info about a place");
        msg.add("• `get me` get info about me");
        msg.add("• `get person [name]` get info about a person");
        msg.add("`lunch` about todays lunch");
        msg.add("• `lunch status` current status for today's lunch");
        msg.add("• `lunch +` include me in today's lunch gang");
        msg.add("• `lunch -` exclude me from today's lunch gang");
        msg.add("• `lunch decide` decide the latest suggested place as today's lunch place");
        msg.add("• `lunch decide [name]` decide a specific place as today's lunch place");
        msg.add("`tag` do some tagging");
        msg.add("• `tag place [name] [tag]` tag a place, e.g: `tag place guldfisken sockerchock`");
        msg.add("• `tag prefer [tag]` add a prefer tag to me, e.g: `tag prefer fredagsburgare`");
        msg.add("• `tag dislike [tag]` add a dislike tag to me, e.g: `tag dislike skaldjursplatå`");
        msg.add("• `tag require [tag]` add a require tag to me, e.g: `tag require vegetariskt`");
        msg.add("`add` add stuff");
        msg.add("• `add place [name]` add a place, keep it to one lower case word for simplicity");
        msg.add("• `add me` add me as a person @raclette knows about");
        sendMultilineMessage(event.getChannel(), msg.build());
    }

    private void handleGetCommand(SlackMessagePosted event, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "me":
                handleGetUser(event, event.getSender().getUserName());
                return;
            case "place":
                handleGetPlace(event, params.get(1));
                return;
            case "places":
                handleGetPlaces(event);
                return;
        }
        sendMessage(event.getChannel(), "What?");
    }

    private void handleGetPlaces(SlackMessagePosted event) {
        final Collection<Place> places = placeService.getAllPlaces();
        sendMultilineMessage(event.getChannel(),
                places.stream().map(p -> "• " + p.name).collect(toList()));
    }

    private void handleGetPlace(SlackMessagePosted event, String name) {
        final Optional<Place> place = placeService.getPlaceByName(name);
        if (!place.isPresent()) {
            sendMessage(event.getChannel(), String.format("I don't know any place called '%s'", name));
            return;
        }
        final Place p = place.get();
        final List<String> tags = p.tags.stream().map(t -> "• " + t.name).collect(toList());
        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add(String.format("*%s*", p.name));
        msg.add(">>>");
        msg.add("Tags");
        msg.addAll(tags);
        sendMultilineMessage(event.getChannel(), msg.build());
    }

    private void handleGetUser(SlackMessagePosted event, String name) {
        final Optional<Person> person = personService.getPersonByName(name);
        if (!person.isPresent()) {
            sendMessage(event.getChannel(), String.format("I don't know a '%s'", name));
            return;
        }
        final Person p = person.get();
        final List<String> prefers = p.preferredTags.stream().map(t -> "• " + t.name).collect(toList());
        final List<String> dislikes = p.dislikedTags.stream().map(t -> "• " + t.name).collect(toList());
        final List<String> requires = p.requiredTags.stream().map(t -> "• " + t.name).collect(toList());
        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add(String.format("*%s*", p.name));
        msg.add(">>>");
        msg.add("Prefers");
        msg.addAll(prefers);
        msg.add("");
        msg.add("Dislikes");
        msg.addAll(dislikes);
        msg.add("");
        msg.add("Requires");
        msg.addAll(requires);
        sendMultilineMessage(event.getChannel(), msg.build());
    }

    private void handleTagCommand(SlackMessagePosted event, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "place":
                handlePlaceTag(event, params);
                return;
            case "prefer":
                handlePersonTag(event, params, TagType.PREFER);
                return;
            case "dislike":
                handlePersonTag(event, params, TagType.DISLIKE);
                return;
            case "require":
                handlePersonTag(event, params, TagType.REQUIRE);
                return;
        }
        sendMessage(event.getChannel(), "What?");
    }

    private void handlePersonTag(SlackMessagePosted event, List<String> params, TagType type) {
        final String tag = params.get(1);
        String name = event.getSender().getUserName();
        final Optional<Person> me = personService.getPersonByName(name);
        if (!me.isPresent()) {
            sendMessage(event.getChannel(), "I don't know you");
            return;
        }
        personService.addTag(me.get().id, tag, type);
        sendMessage(event.getChannel(), String.format("You now %s %s", type.name().toLowerCase(), tag));
    }

    private void handlePlaceTag(SlackMessagePosted event, List<String> params) {
        final String name = params.get(1);
        final String tag = params.get(2);
        final Optional<Place> place = placeService.getPlaceByName(name);
        if (place.isPresent()) {
            placeService.addPlaceTag(place.get().id, tag);
            sendMessage(event.getChannel(), String.format("%s is now tagged with '%s'", name, tag));
        } else {
            sendMessage(event.getChannel(), String.format("I know no place called '%s'", name));
        }
    }

    private void handleDecideCommand(SlackMessagePosted event, List<String> params) {
        final LocalDate lunchTime = lunchService.getCurrentLunchTime();
        Optional<Place> place;
        if (params.size() > 1) {
            place = placeService.getPlaceByName(params.get(1));
            if (!place.isPresent()) {
                sendMessage(event.getChannel(), String.format("I know no place called '%s'", params.get(1)));
                return;
            }
        } else {
            place = lunchService.getLatestSuggestedPlace();
            if (!place.isPresent()) {
                sendMessage(event.getChannel(), "No place is suggested");
                return;
            }
        }
        lunchService.setLunchPlace(lunchTime, place.get().id);
        sendMessage(event.getChannel(), String.format("Today's lunch will be at *%s*", place.get().name));
    }

    private void handleLunchCommand(SlackMessagePosted event, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "status":
                handleLunchStatus(event);
                return;
            case "+":
                addParticipant(event.getChannel(), event.getSender().getUserName());
                return;
            case "-":
                removeParticipant(event.getChannel(), event.getSender().getUserName());
                return;
            case "decide":
                handleDecideCommand(event, params);
                return;
        }
        sendMessage(event.getChannel(), "What?");
    }

    private void handleLunchStatus(SlackMessagePosted event) {
        final LocalDate currentLunchTime = lunchService.getCurrentLunchTime();
        final Collection<String> participants = lunchService.getLunchTimeParticipants(currentLunchTime);
        final Optional<Place> suggestedPlace = lunchService.suggestLunchPlace(currentLunchTime);
        final ImmutableList.Builder<String> msg = ImmutableList.builder();
        msg.add(String.format("Lunch status for *%s*", currentLunchTime.format(DateTimeFormatter.ISO_DATE)));
        msg.add("Participants");
        msg.addAll(participants.stream().map(name -> "• " + name).collect(toList()));
        suggestedPlace.ifPresent(place -> msg.add(String.format("Suggested place: *%s*", suggestedPlace.get().name)));
        sendMultilineMessage(event.getChannel(), msg.build());
    }

    private void handleAddCommand(SlackMessagePosted event, List<String> params) {
        switch (params.get(0).toLowerCase()) {
            case "place":
                final Place place = new Place();
                place.name = params.get(1);
                placeService.addPlace(place);
                sendMessage(event.getChannel(), String.format("Place '%s' is now created", place.name));
                return;
            case "me":
                String name = event.getSender().getUserName();
                if (personService.getPersonByName(name).isPresent()) {
                    sendMessage(event.getChannel(), "You are already added");
                    return;
                }
                personService.addPerson(name);
                sendMessage(event.getChannel(), "Welcome!");
                return;
            case "lunchtime":
                createLunchTimeForToday();
                return;
            case "participants":
                addParticipants();
                return;
        }
        sendMessage(event.getChannel(), "What?");
    }

    private List<String> getCommand(String message) {
        final String tag = "<@" + me().getId() + ">";
        String stripped = StringUtils.delete(message, tag + ":");
        stripped = StringUtils.delete(stripped, tag);
        return Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings().splitToList(stripped);
    }

    private boolean isMessageFromMe(SlackMessagePosted message) {
        return message.getSender().getId().equals(me().getId());
    }

    private boolean isMessageForMe(SlackMessagePosted message) {
        return message.getChannel().isDirect() || message.getMessageContent().contains(me().getId());
    }

    private SlackPersona me() {
        return session.sessionPersona();
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
