package se.racasse.raclette.person;

import org.springframework.stereotype.Component;
import se.racasse.raclette.tag.Tag;
import se.racasse.raclette.tag.TagType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toSet;

@Component
public class PersonService {

    private final PersonDao dao;

    PersonService(PersonDao dao) {
        this.dao = dao;
    }

    public Person getPerson(int personId) {
        final Person person = dao.getPerson(personId);
        person.preferredTags = dao.getPreferredTags(person.id);
        person.requiredTags = dao.getRequiredTags(person.id);
        person.placeVotes = dao.getPlaceVotes(personId);
        return person;
    }

    public Optional<Person> getPersonByName(String name) {
        return dao.getPersonByName(name).flatMap(person -> {
            person.preferredTags = dao.getPreferredTags(person.id);
            person.requiredTags = dao.getRequiredTags(person.id);
            person.placeVotes = dao.getPlaceVotes(person.id);
            return Optional.of(person);
        });
    }

    public Collection<Person> getParticipatingPersons(LocalDate date) {
        return dao.getParticipatingPersons(date).stream().map(person -> {
            person.preferredTags = dao.getPreferredTags(person.id);
            person.requiredTags = dao.getRequiredTags(person.id);
            person.placeVotes = dao.getPlaceVotes(person.id);
            return person;
        }).collect(toSet());
    }

    public Person addPerson(String name) {
        final int personId = dao.insertPerson(name);
        return getPerson(personId);
    }

    public void addTag(int personId, String tag, TagType type) {
        if (!dao.getTags(personId, type).contains(new Tag(tag))) {
            dao.insertTag(personId, tag, type);
        }
    }

    public void removeTag(int personId, String tag, TagType type) {
        dao.deleteTag(personId, tag, type);
    }
}
