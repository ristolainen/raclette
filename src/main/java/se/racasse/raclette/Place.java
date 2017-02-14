package se.racasse.raclette;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.HashSet;

public class Place {

    public int id;
    public String name;
    public Collection<Tag> tags = new HashSet<>();
    public Collection<Vote> upVotes = new HashSet<>();
    public Collection<Vote> downVotes = new HashSet<>();

    public boolean accepted(Collection<Person> persons) {
        return persons.stream().allMatch(p -> p.accepts(tags));
    }

    public float score(Collection<Person> persons, LocalDate latestLunch) {
        return scoreTags(persons) + scoreUpVotes() + scoreDownVotes() + timeSinceLastLunchBoost(latestLunch);
    }

    private float timeSinceLastLunchBoost(LocalDate latestLunch) {
        int daysBetween = 30;
        if (latestLunch != null) {
            daysBetween = Period.between(latestLunch, LocalDate.now()).getDays();
        }
        return (float) (1 + 4 * Math.log(daysBetween));
    }

    private int scoreTags(Collection<Person> persons) {
        return persons.stream().mapToInt(person -> person.scoreTags(tags)).sum();
    }

    private int scoreUpVotes() {
        return upVotes.size();
    }

    private int scoreDownVotes() {
        return downVotes.size();
    }
}
