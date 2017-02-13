package se.racasse.raclette;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SlackMessageHandler implements SlackMessagePostedListener {

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
        lunchChannel = session.findChannelByName("raclette-dev");
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
                sendMessage(event.getChannel(), Throwables.getStackTraceAsString(e));
            }
        }
    }

    private void handleCommand(SlackMessagePosted event, String cmd, List<String> params) {
        switch (cmd) {
            case "users":
                session.getUsers().forEach(u -> {
                    sendMessage(event.getChannel(), u.getUserName() + ": " + u.getPresence().name());
                });
                return;
            case "add":
                handleAddCommand(event, params);
                return;
            case "+":
                addParticipant(event.getChannel(), event.getSender().getUserName());
                return;
            case "-":
                removeParticipant(event.getChannel(), event.getSender().getUserName());
                return;
        }
        sendMessage(event.getChannel(), "What?");
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
    }

    private List<String> getCommand(String message) {
        final String stripped = StringUtils.delete(message, "<@" + me().getId() + ">");
        return Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings().splitToList(stripped);
    }

    private boolean isMessageFromMe(SlackMessagePosted message) {
        return message.getSender().getId().equals(me().getId());
    }

    private boolean isMessageForMe(SlackMessagePosted message) {
        return message.getMessageContent().contains(me().getId());
    }

    private SlackPersona me() {
        return session.sessionPersona();
    }

    private void sendMessage(SlackChannel channel, String msg) {
        session.sendMessage(channel, msg);
    }

    private void sendLunchMessage(String msg) {
        session.sendMessage(lunchChannel, msg);
    }

}
