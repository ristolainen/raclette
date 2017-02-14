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

    Optional<LocalDate> getLatestLunchForPlace(int placeId) {
        return jdbcTemplate.query("select lunch_time_id from lunch where place_id = :placeId",
                new MapSqlParameterSource("placeId", placeId), SingleColumnRowMapper.newInstance(LocalDate.class))
                .stream().findFirst();
    }

    Collection<Integer> getLunchParticipants(LocalDate date) {
        return jdbcTemplate.query("select person_id from lunch_participant where lunch_time_id = :date",
                new MapSqlParameterSource("date", date), SingleColumnRowMapper.newInstance(Integer.class));
    }
}
