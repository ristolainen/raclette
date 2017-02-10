package se.racasse.raclette;

import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
class PlaceDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    PlaceDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Collection<Place> getAllPlaces() {
        return jdbcTemplate.query("select * from place", (SqlParameterSource) null, (resultSet, rowNum) -> {
            final Place place = new Place();
            place.id = resultSet.getInt("id");
            place.name = resultSet.getString("name");
            return place;
        });
    }

    Collection<Tag> getPlaceTags(int placeId) {
        return jdbcTemplate.query("select * from place_tag where place_id = :placeId",
                new MapSqlParameterSource("placeId", placeId),
                (resultSet, rowNum) -> new Tag(resultSet.getString("id")));
    }

    Collection<Vote> getPlaceUpVotes(int placeId) {
        return getPlaceVotes(placeId, VoteType.UP);
    }

    Collection<Vote> getPlaceDownVotes(int placeId) {
        return getPlaceVotes(placeId, VoteType.DOWN);
    }

    private Collection<Vote> getPlaceVotes(int placeId, VoteType type) {
        final Integer votes = jdbcTemplate.queryForObject("select count(*) from place_vote where place_id = :placeId and type = :type",
                new MapSqlParameterSource().addValue("placeId", placeId).addValue("type", type.toString().charAt(0)),
                SingleColumnRowMapper.newInstance(Integer.class));
        return Stream.generate(Vote::new).limit(votes).collect(toSet());
    }

    int insertPlace(Place place) {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("insert into place (name) values (:name)",
                new MapSqlParameterSource().addValue("name", place.name), keyHolder);
        return keyHolder.getKey().intValue();
    }

}
