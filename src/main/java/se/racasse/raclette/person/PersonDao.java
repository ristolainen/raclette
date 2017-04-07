package se.racasse.raclette.person;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import se.racasse.raclette.lunch.LunchVisit;
import se.racasse.raclette.tag.Tag;
import se.racasse.raclette.tag.TagType;
import se.racasse.raclette.vote.Vote;
import se.racasse.raclette.vote.VoteType;

import java.sql.Date;
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

    void deleteTag(int personId, String tag, TagType type) {
        jdbcTemplate.update("delete from person_tag where person_id = :personId and tag_id = :tagId and type = :type",
                new MapSqlParameterSource()
                        .addValue("personId", personId)
                        .addValue("tagId", tag)
                        .addValue("type", type.toString().substring(0, 1)));
    }

    Collection<Vote> getPlaceVotes(int personId) {
        return jdbcTemplate.query("select v.*, pe.name as person_name, pl.name as place_name from place_vote v " +
                        "left join person pe on pe.id = v.person_id " +
                        "left join place pl on pl.id = v.place_id " +
                        "where v.person_id = :personId",
                new MapSqlParameterSource("personId", personId),
                (resultSet, rowNum) -> {
                    final Vote vote = new Vote();
                    vote.personId = personId;
                    vote.personName = resultSet.getString("person_name");
                    vote.placeId = resultSet.getInt("place_id");
                    vote.placeName = resultSet.getString("place_name");
                    vote.type = VoteType.fromInitial(resultSet.getString("type"));
                    return vote;
                });
    }

    Collection<LunchVisit> getLatestLunchVisits(int personId) {
        return jdbcTemplate.query(
                "select max(l.lunch_time_id) as lunch_time, place_id from lunch_participant lp " +
                        "left join lunch l on l.lunch_time_id = lp.lunch_time_id " +
                        "where person_id = :personId group by place_id",
                new MapSqlParameterSource("personId", personId),
                (rs, i) -> {
                    LunchVisit visit = new LunchVisit();
                    visit.personId = personId;
                    visit.placeId = rs.getInt("place_id");
                    Date lunchTime = rs.getDate("lunch_time");
                    visit.lunchTime = lunchTime == null ? null : lunchTime.toLocalDate();
                    return visit;
                });
    }

}
