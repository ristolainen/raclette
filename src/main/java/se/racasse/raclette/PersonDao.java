package se.racasse.raclette;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Component
class PersonDao {

    private static final RowMapper<Person> PERSON_ROW_MAPPER = (resultSet, rowNum) -> {
        final Person person = new Person();
        person.id = resultSet.getInt("id");
        person.name = resultSet.getString("name");
        return person;
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    PersonDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Person getPerson(int personId) {
        return jdbcTemplate.queryForObject("select * from person where id = :id",
                new MapSqlParameterSource("id", personId),
                PERSON_ROW_MAPPER);
    }

    Optional<Person> getPersonByName(String name) {
        return jdbcTemplate.query("select * from person where name = :name",
                new MapSqlParameterSource("name", name),
                PERSON_ROW_MAPPER).stream().findFirst();
    }

    Collection<Person> getParticipatingPersons(LocalDate lunchTimeDate) {
        return jdbcTemplate.query("select * from person where id in (select person_id from lunch_participant where lunch_time_id = :lunchTimeDate)",
                new MapSqlParameterSource("lunchTimeDate", lunchTimeDate),
                PERSON_ROW_MAPPER);
    }

    int insertPerson(String name) {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("insert into person (name) values (:name)",
                new MapSqlParameterSource("name", name), keyHolder);
        return keyHolder.getKey().intValue();
    }

    Collection<Tag> getPreferredTags(int personId) {
        return getTags(personId, TagType.PREFER);
    }

    Collection<Tag> getDislikedTags(int personId) {
        return getTags(personId, TagType.DISLIKE);
    }

    Collection<Tag> getRequiredTags(int personId) {
        return getTags(personId, TagType.REQUIRE);
    }

    Collection<Tag> getTags(int personId, TagType tagType) {
        return jdbcTemplate.query("select tag_id from person_tag where person_id = :personId and type = :tagType",
                new MapSqlParameterSource().addValue("personId", personId).addValue("tagType", tagType.toString().substring(0, 1)),
                (resultSet, rowNum) -> new Tag(resultSet.getString("tag_id")));
    }

    void insertTag(int personId, String tag, TagType type) {
        jdbcTemplate.update("insert into person_tag (person_id, tag_id, type) values (:personId, :tagId, :type)",
                new MapSqlParameterSource()
                        .addValue("personId", personId)
                        .addValue("tagId", tag)
                        .addValue("type", type.toString().substring(0, 1)));
    }

}
