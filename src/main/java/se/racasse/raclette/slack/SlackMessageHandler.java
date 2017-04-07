package se.racasse.raclette.slack;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Profile("slack")
public class SlackMessageHandler {

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
        session.addMessagePostedListener((event, session) ->
                handleMessage(event.getSender(), event.getChannel(), event.getMessageContent()));
        session.addMessageUpdatedListener((event, session) -> {
                    throw new UnsupportedOperationException("Updated messages not supported");
                });
        lunchChannel = session.findChannelByName("lunch");
    }

    private void handleMessage(SlackUser sender, SlackChannel channel, String messageContent) {
        if (isMessageFromMe(sender)) {
            return;
        }
        if (isMessageForMe(channel, messageContent)) {
            try {
                final List<String> command = getCommand(messageContent);
                if (command.size() > 0) {
                    final String cmd = command.get(0).toLowerCase();
                    final ImmutableList<String> params = ImmutableList.copyOf(Iterables.skip(command, 1));
                    commandHandler.handleCommand(sender, channel, cmd, params);
                }
            } catch (Exception e) {
                LOG.error("Failed to handle slack event for ", e);
                sendMessage(channel, e.getMessage());
            }
        }
    }

    private List<String> getCommand(String message) {
        final String tag = "<@" + me().getId() + ">";
        String stripped = StringUtils.delete(message, tag + ":");
        stripped = StringUtils.delete(stripped, tag);
        return Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings().splitToList(stripped);
    }

    private boolean isMessageFromMe(SlackUser sender) {
        return sender.getId().equals(me().getId());
    }

    private boolean isMessageForMe(SlackChannel channel, String messageContent) {
        return channel.isDirect()
                || channel.getId().equals(lunchChannel.getId())
                || messageContent.contains(me().getId());
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

    @Configuration
    @Profile("slack")
    static class Config {

        @Value("${slack.token}")
        private String slackToken;

        @Bean(initMethod = "connect", destroyMethod = "disconnect")
        public SlackSession slackSession() {
            return SlackSessionFactory.createWebSocketSlackSession(slackToken);
        }
    }

}
