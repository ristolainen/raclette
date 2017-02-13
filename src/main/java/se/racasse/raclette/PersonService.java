package se.racasse.raclette;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toSet;

@Component
class PersonService {

    private final PersonDao dao;

    PersonService(PersonDao dao) {
        this.dao = dao;
    }

    Optional<Person> getPersonByName(String name) {
        return dao.getPersonByName(name).flatMap(person -> {
            person.preferredTags = dao.getPreferredTags(person.id);
            person.dislikedTags = dao.getDislikedTags(person.id);
            person.requiredTags = dao.getRequiredTags(person.id);
            return Optional.of(person);
        });
    }

    Collection<Person> getParticipatingPersons(LocalDate date) {
        return dao.getParticipatingPersons(date).stream().map(person -> {
            person.preferredTags = dao.getPreferredTags(person.id);
            person.dislikedTags = dao.getDislikedTags(person.id);
            person.requiredTags = dao.getRequiredTags(person.id);
            return person;
        }).collect(toSet());
    }

    int addPerson(String name) {
        return dao.insertPerson(name);
    }

    void addTag(int personId, String tag, TagType type) {
        if (!dao.getTags(personId, type).contains(new Tag(tag))) {
            dao.insertTag(personId, tag, type);
        }
    }
}
