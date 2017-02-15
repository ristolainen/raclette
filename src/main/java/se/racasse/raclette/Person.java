package se.racasse.raclette;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Person {

    int id;
    String name;
    Collection<Tag> requiredTags = new HashSet<>();
    Collection<Tag> preferredTags = new HashSet<>();

    boolean accepts(Collection<Tag> placeTags) {
        return requiredTags.stream().allMatch(placeTags::contains);
    }

    int scoreTags(Collection<Tag> placeTags) {
        return tagIntersection(preferredTags, placeTags).size();
    }

    private Set<Tag> tagIntersection(Collection<Tag> tags, Collection<Tag> placeTags) {
        return Sets.intersection(ImmutableSet.copyOf(tags), ImmutableSet.copyOf(placeTags));
    }

}
