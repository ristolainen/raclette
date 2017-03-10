package se.racasse.raclette.slack;

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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class SlackMessageHandler implements SlackMessagePostedListener {

    private final static Logger LOG = LoggerFactory.getLogger(SlackMessageHandler.class);

    private final SlackSession session;
    private final SlackCommandHandler commandHandler;

    private SlackChannel lunchChannel;

    @Autowired
    public SlackMessageHandler(SlackSession session, SlackCommandHandler commandHandler) {
        this.session = session;
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void init() {
        session.addMessagePostedListener(this);
        lunchChannel = session.findChannelByName("lunch");
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
                    commandHandler.handleCommand(event, cmd, params);
                }
            } catch (Exception e) {
                LOG.error("Failed to handle slack event for ", e);
                sendMessage(event.getChannel(), e.getMessage());
            }
        }
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
        return message.getChannel().isDirect()
                || message.getChannel().getId().equals(lunchChannel.getId())
                || message.getMessageContent().contains(me().getId());
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
