package se.racasse.raclette.person;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import se.racasse.raclette.lunch.LunchVisit;
import se.racasse.raclette.tag.Tag;
import se.racasse.raclette.vote.Vote;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Person {

    public int id;
    public String name;
    public Collection<Tag> requiredTags = new HashSet<>();
    public Collection<Tag> preferredTags = new HashSet<>();
    public Collection<Vote> placeVotes = new HashSet<>();
    public Collection<LunchVisit> latestVisits = new HashSet<>();

    public boolean accepts(Collection<Tag> placeTags) {
        return requiredTags.stream().allMatch(placeTags::contains);
    }

    public double scoreTags(Collection<Tag> placeTags) {
        if (placeTags.size() == 0) {
            return 0d;
        }
        return (1d / placeTags.size()) * tagIntersection(preferredTags, placeTags).size();
    }

    private Set<Tag> tagIntersection(Collection<Tag> tags, Collection<Tag> placeTags) {
        return Sets.intersection(ImmutableSet.copyOf(tags), ImmutableSet.copyOf(placeTags));
    }

}
