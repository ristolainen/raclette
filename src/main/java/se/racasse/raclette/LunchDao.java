package se.racasse.raclette;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

    void insertLunchParticipant(LocalDate date, int personId) {
        jdbcTemplate.update("insert into lunch_participant (person_id, lunch_time_id) values (:personId, :date)",
                new MapSqlParameterSource().addValue("personId", personId).addValue("date", date));
    }

    void setLunch(LocalDate date, Place place) {
        jdbcTemplate.update("insert into lunch (lunch_time_id, place_id) values (:date, :placeId) on duplicate key update place_id = :placeId",
                new MapSqlParameterSource().addValue("date", date).addValue("placeId", place.id));
    }

}
