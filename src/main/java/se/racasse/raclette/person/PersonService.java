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

    private final PersonDao personDao;

    PersonService(PersonDao personDao) {
        this.personDao = personDao;
    }

    public Person getPerson(int personId) {
        final Person person = personDao.getPerson(personId);
        return fetchPersonInfo(person);
    }

    public Optional<Person> getPersonByName(String name) {
        return personDao.getPersonByName(name).flatMap(person ->
                Optional.of(fetchPersonInfo(person)));
    }

    public Collection<Person> getParticipatingPersons(LocalDate date) {
        return personDao.getParticipatingPersons(date).stream()
                .map(person -> fetchPersonInfo(person))
                .collect(toSet());
    }

    private Person fetchPersonInfo(Person person) {
        person.preferredTags = personDao.getPreferredTags(person.id);
        person.requiredTags = personDao.getRequiredTags(person.id);
        person.placeVotes = personDao.getPlaceVotes(person.id);
        person.latestVisits = personDao.getLatestLunchVisits(person.id);
        return person;
    }

    public Person addPerson(String name) {
        final int personId = personDao.insertPerson(name);
        return getPerson(personId);
    }

    public void addTag(int personId, String tag, TagType type) {
        if (!personDao.getTags(personId, type).contains(new Tag(tag))) {
            personDao.insertTag(personId, tag, type);
        }
    }

    public void removeTag(int personId, String tag, TagType type) {
        personDao.deleteTag(personId, tag, type);
    }
}
