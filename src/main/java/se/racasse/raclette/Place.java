package se.racasse.raclette;

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

    public int score(Collection<Person> persons) {
        return scoreTags(persons) + scoreUpVotes() + scoreDownVotes();
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
