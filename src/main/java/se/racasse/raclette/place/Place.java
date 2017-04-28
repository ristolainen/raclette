package se.racasse.raclette.place;

import se.racasse.raclette.lunch.LunchVisit;
import se.racasse.raclette.person.Person;
import se.racasse.raclette.tag.Tag;
import se.racasse.raclette.vote.Vote;

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
                + scoreTimeSinceLastLunch(scoringContext.personLunchVisits.values());
    }

    private float scoreTags(Collection<Person> persons) {
        return (float) (1.5d * persons.stream().mapToDouble(person -> person.scoreTags(tags)).sum());
    }

    private int scoreLunchUpVotes(Collection<Vote> votes) {
        return 3 * votes.size();
    }

    private int scoreLunchDownVotes(Collection<Vote> votes) {
        return -3 * votes.size();
    }

    private float scoreUpVotes(Collection<Person> persons) {
        final Collection<Vote> votes = filterVotesOnPersons(upVotes, persons);
        return 1.0f * votes.size();
    }

    private float scoreDownVotes(Collection<Person> persons) {
        final Collection<Vote> votes = filterVotesOnPersons(downVotes, persons);
        return -1.0f * votes.size();
    }

    private Collection<Vote> filterVotesOnPersons(Collection<Vote> votes, Collection<Person> persons) {
        final Set<Integer> personIds = persons.stream().map(p -> p.id).collect(Collectors.toSet());
        return votes.stream().filter(v -> personIds.contains(v.personId)).collect(Collectors.toList());
    }

    private float scoreTimeSinceLastLunch(Collection<LunchVisit> visits) {
        return visits.stream()
                .map(visit -> visit == null ? 30 : Period.between(visit.lunchTime, LocalDate.now()).getDays())
                .map(days -> (1 + 2 * Math.log(days)))
                .reduce(0d, Double::sum)
                .floatValue() / visits.size();
    }

}
