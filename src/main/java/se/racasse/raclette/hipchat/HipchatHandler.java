package se.racasse.raclette.hipchat;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import se.racasse.raclette.Actions;
import se.racasse.raclette.person.Person;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;

@Component
@Profile("hipchat")
class HipchatHandler {

    // This is the interface for all raclette operations
    private final Actions actions;

    HipchatHandler(Actions actions) {
        this.actions = actions;
    }

    @PostConstruct
    void initialize() {
        actions.setAutomaticParticipantProvider(new Actions.AutomaticParticipantProvider() {
            @Override
            public Collection<String> getParticipantsToBeAdded() {
                // This should return the names of all persons that should be
                // automatically added as lunch participants
                return Collections.emptySet();
            }

            @Override
            public void participantAdded(Person participant) {
                // This is called when a person is added as a participant by the automatic routine.
                // This event can, for example, be broadcasted on hipchat.
            }
        });

        actions.setCreatedLunchTimeCallback(createLunchTimeResponse -> {
            // This is called when a new lunch time is created. This event can, for example, be broadcasted on hipchat
        });
    }

    @Configuration
    @Profile("hipchat")
    static class Config {
        // Hipchat-specific beans go here
    }

}
