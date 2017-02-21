package se.racasse.raclette;

import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Component
class LunchDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    LunchDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void insertLunchTime(LocalDate date) {
        jdbcTemplate.update("insert into lunch_time (date) values (:date)",
                new MapSqlParameterSource().addValue("date", date));
    }

    boolean isParticipant(LocalDate date, int personId) {
        return jdbcTemplate.queryForObject("select count(*) from lunch_participant where person_id = :personId and lunch_time_id = :date",
                new MapSqlParameterSource().addValue("personId", personId).addValue("date", date),
                SingleColumnRowMapper.newInstance(Integer.class)) > 0;
    }

    void insertLunchParticipant(LocalDate date, int personId) {
        jdbcTemplate.update("insert into lunch_participant (person_id, lunch_time_id) values (:personId, :date)",
                new MapSqlParameterSource().addValue("personId", personId).addValue("date", date));
    }

    void removeLunchParticipant(LocalDate date, int personId) {
        jdbcTemplate.update("delete from lunch_participant where person_id = :personId and lunch_time_id = :date",
                new MapSqlParameterSource().addValue("personId", personId).addValue("date", date));
    }

    void setLunch(LocalDate date, int placeId) {
        jdbcTemplate.update("insert into lunch (lunch_time_id, place_id) values (:date, :placeId) on duplicate key update place_id = :placeId",
                new MapSqlParameterSource().addValue("date", date).addValue("placeId", placeId));
    }

    LocalDate getLatestLunchTime() {
        return jdbcTemplate.queryForObject("select date from lunch_time order by date desc limit 1",
                new MapSqlParameterSource(), SingleColumnRowMapper.newInstance(LocalDate.class));
    }

    Optional<LocalDate> getLatestLunchForPlace(int placeId, LocalDate before) {
        return jdbcTemplate.query("select lunch_time_id from lunch where place_id = :placeId and lunch_time_id < :before",
                new MapSqlParameterSource().addValue("placeId", placeId).addValue("before", before),
                SingleColumnRowMapper.newInstance(LocalDate.class))
                .stream().findFirst();
    }

    Collection<Integer> getLunchParticipants(LocalDate date) {
        return jdbcTemplate.query("select person_id from lunch_participant where lunch_time_id = :date",
                new MapSqlParameterSource("date", date), SingleColumnRowMapper.newInstance(Integer.class));
    }

    void insertLunchVote(int personId, LocalDate lunchTime, int placeId, VoteType type) {
        jdbcTemplate.update("insert into lunch_vote (person_id, lunch_time_id, place_id, type) " +
                        "values (:personId, :lunchTimeId, :placeId, :type) " +
                        "on duplicate key update type = :type",
                new MapSqlParameterSource()
                        .addValue("personId", personId)
                        .addValue("lunchTimeId", lunchTime)
                        .addValue("placeId", placeId)
                        .addValue("type", type.name().substring(0, 1)));
    }


    Collection<Vote> getLunchVotesByPersons(LocalDate lunchTime, VoteType voteType, Collection<Integer> personIds) {
        return jdbcTemplate.query("select * from lunch_vote where lunch_time_id = :lunchTimeId " +
                        "and type = :type and person_id in (:personIds)",
                new MapSqlParameterSource()
                        .addValue("lunchTimeId", lunchTime)
                        .addValue("personIds", personIds)
                        .addValue("type", voteType.name().substring(0, 1)),
                (resultSet, rowNum) -> {
                    final Vote vote = new Vote();
                    vote.personId = resultSet.getInt("person_id");
                    vote.placeId = resultSet.getInt("place_id");
                    vote.type = voteType;
                    return vote;
                });
    }

    Collection<Vote> getLunchVotesByPlaces(LocalDate lunchTime, Collection<Integer> placeIds) {
        return jdbcTemplate.query("select * from lunch_vote where lunch_time_id = :lunchTimeId " +
                        "and place_id in (:placeIds)",
                new MapSqlParameterSource()
                        .addValue("lunchTimeId", lunchTime)
                        .addValue("placeIds", placeIds),
                (resultSet, rowNum) -> {
                    final Vote vote = new Vote();
                    vote.personId = resultSet.getInt("person_id");
                    vote.placeId = resultSet.getInt("place_id");
                    vote.type = voteTypeByFirstCharacter(resultSet.getString("type"));
                    return vote;
                });
    }

    private VoteType voteTypeByFirstCharacter(String firstCharacterOfType) {
        switch (firstCharacterOfType) {
            case "U":
                return VoteType.UP;
            case "D":
                return VoteType.DOWN;
        }
        throw new IllegalStateException();
    }

    void removeLunchVotes(LocalDate lunchTime, int personId) {
        jdbcTemplate.update("delete from lunch_vote where lunch_time_id = :lunchTimeId and person_id = :personId",
                new MapSqlParameterSource().addValue("lunchTimeId", lunchTime).addValue("personId", personId));
    }
}
