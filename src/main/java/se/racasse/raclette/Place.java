package se.racasse.raclette;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Place {

    public int id;
    public String name;
    public Collection<Tag> tags = new HashSet<>();
    public Collection<Vote> upVotes = new HashSet<>();
    public Collection<Vote> downVotes = new HashSet<>();

    public boolean accepted(Collection<Person> persons) {
        return persons.stream().allMatch(p -> p.accepts(tags));
    }

    public float score(PlaceScoringContext scoringContext) {
        return scoreTags(scoringContext.persons)
                + scoreLunchUpVotes(scoringContext.lunchUpVotes)
                + scoreLunchDownVotes(scoringContext.lunchDownVotes)
                + scoreUpVotes(scoringContext.persons)
                + scoreDownVotes(scoringContext.persons)
                + timeSinceLastLunchBoost(scoringContext.latestLunch);
    }

    private int scoreTags(Collection<Person> persons) {
        return persons.stream().mapToInt(person -> person.scoreTags(tags)).sum();
    }

    private int scoreLunchUpVotes(Collection<Vote> votes) {
        return 3 * votes.size();
    }

    private int scoreLunchDownVotes(Collection<Vote> votes) {
        return -3 * votes.size();
    }

    private float scoreUpVotes(Collection<Person> persons) {
        final Collection<Vote> votes = filterVotesOnPersons(upVotes, persons);
        return 1.5f * votes.size();
    }

    private float scoreDownVotes(Collection<Person> persons) {
        final Collection<Vote> votes = filterVotesOnPersons(downVotes, persons);
        return -1.5f * votes.size();
    }

    private Collection<Vote> filterVotesOnPersons(Collection<Vote> votes, Collection<Person> persons) {
        final Set<Integer> personIds = persons.stream().map(p -> p.id).collect(Collectors.toSet());
        return votes.stream().filter(personIds::contains).collect(Collectors.toList());
    }

    private float timeSinceLastLunchBoost(LocalDate latestLunch) {
        int daysBetween = 30;
        if (latestLunch != null) {
            daysBetween = Period.between(latestLunch, LocalDate.now()).getDays();
        }
        return (float) (1 + 4 * Math.log(daysBetween));
    }

}
