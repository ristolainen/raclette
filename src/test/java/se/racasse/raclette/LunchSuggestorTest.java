package se.racasse.raclette;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LunchSuggestorTest {

    @Test
    public void suggestWithOnePlaceAndOnePersonAndOnePreferredTag() throws Exception {
        final Place place = createPlace("place1", "burger", "buffe");
        final Person person = createPerson("person1");
        person.preferredTags.add(new Tag("burger"));
        final LunchContext lunchContext = new LunchContext();
        lunchContext.places = Collections.singleton(place);
        lunchContext.participants = Collections.singleton(person);
        lunchContext.latestLunches = Collections.emptyMap();
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(lunchContext);
        final Optional<Place> suggestedPlace = lunchSuggestor.suggest();

        assertTrue(suggestedPlace.isPresent());
        assertThat(suggestedPlace.get(), sameInstance(place));
    }

    @Test
    public void suggestWithTwoPlacesAndOnePersonAndOnePreferredTagAndOneUpvote() throws Exception {
        final Place place1 = createPlace("place1", "burger", "buffe");
        place1.upVotes.add(new Vote());
        final Place place2 = createPlace("place2", "buffe", "pizza");
        final Person person = createPerson("person1");
        person.preferredTags.add(new Tag("buffe"));
        final LunchContext lunchContext = new LunchContext();
        lunchContext.places = ImmutableSet.of(place1, place2);
        lunchContext.participants = Collections.singleton(person);
        lunchContext.latestLunches = Collections.emptyMap();
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(lunchContext);
        final Optional<Place> suggestedPlace = lunchSuggestor.suggest();

        assertTrue(suggestedPlace.isPresent());
        assertThat(suggestedPlace.get(), sameInstance(place1));
    }

    @Test
    public void suggestWithTwoPlacesAndTwoPersonsAndOneDemandedTag() throws Exception {
        final Place place1 = createPlace("place1", "burger", "buffe", "pizza", "beef");
        final Place place2 = createPlace("place2", "close");
        place1.upVotes.add(new Vote());
        final Person person1 = createPerson("person1");
        person1.preferredTags.add(new Tag("buffe"));
        person1.preferredTags.add(new Tag("pizza"));
        person1.preferredTags.add(new Tag("beef"));
        final Person person2 = createPerson("person2");
        person2.requiredTags.add(new Tag("close"));
        final LunchContext lunchContext = new LunchContext();
        lunchContext.places = ImmutableSet.of(place1, place2);
        lunchContext.participants = ImmutableSet.of(person1, person2);
        lunchContext.latestLunches = Collections.emptyMap();
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(lunchContext);
        final Optional<Place> suggestedPlace = lunchSuggestor.suggest();

        assertTrue(suggestedPlace.isPresent());
        assertThat(suggestedPlace.get(), sameInstance(place2));
    }

    @Test
    public void suggestWithTwoPlacesAndOnePersonAndLatestDates() throws Exception {
        final Place place1 = createPlace(1,"place1", "burger");
        final Place place2 = createPlace(2,"place2", "burger");
        place1.upVotes.add(new Vote());
        final Person person1 = createPerson("person1");
        person1.preferredTags.add(new Tag("buffe"));
        final LunchContext lunchContext = new LunchContext();
        lunchContext.places = ImmutableSet.of(place1, place2);
        lunchContext.participants = ImmutableSet.of(person1);
        lunchContext.latestLunches = ImmutableMap.of(place1.id, LocalDate.now().minusDays(4), place2.id, LocalDate.now().minusDays(2));
        final LunchSuggestor lunchSuggestor = new LunchSuggestor(lunchContext);
        final Optional<Place> suggestedPlace = lunchSuggestor.suggest();

        assertTrue(suggestedPlace.isPresent());
        assertThat(suggestedPlace.get(), sameInstance(place1));
    }

    private Place createPlace(String name, String... tags) {
        return createPlace(0, name, tags);
    }

    private Place createPlace(int id, String name, String... tags) {
        final Place place = new Place();
        place.id = id;
        place.name = name;
        place.tags = Arrays.stream(tags).map(Tag::new).collect(toSet());
        return place;
    }

    private Person createPerson(String name) {
        final Person person = new Person();
        person.name = name;
        return person;
    }

}
