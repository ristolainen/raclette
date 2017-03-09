package se.racasse.raclette;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
class PlaceDao {

    private static final RowMapper<Place> PLACE_ROW_MAPPER = (resultSet, rowNum) -> {
        final Place place = new Place();
        place.id = resultSet.getInt("id");
        place.name = resultSet.getString("name");
        return place;
    };
    private final NamedParameterJdbcTemplate jdbcTemplate;

    PlaceDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Collection<Place> getAllPlaces() {
        return jdbcTemplate.query("select * from place", (SqlParameterSource) null, PLACE_ROW_MAPPER);
    }

    Collection<Tag> getPlaceTags(int placeId) {
        return jdbcTemplate.query("select * from place_tag where place_id = :placeId",
                new MapSqlParameterSource("placeId", placeId),
                (resultSet, rowNum) -> new Tag(resultSet.getString("tag_id")));
    }

    Collection<Vote> getPlaceUpVotes(int placeId) {
        return getPlaceVotes(placeId, VoteType.UP);
    }

    Collection<Vote> getPlaceDownVotes(int placeId) {
        return getPlaceVotes(placeId, VoteType.DOWN);
    }

    private Collection<Vote> getPlaceVotes(int placeId, VoteType type) {
        return jdbcTemplate.query("select v.*, pe.name as person_name, pl.name as place_name from place_vote v " +
                        "left join person pe on pe.id = v.person_id " +
                        "left join place pl on pl.id = v.place_id " +
                        "where v.place_id = :placeId and v.type = :type",
                new MapSqlParameterSource()
                        .addValue("placeId", placeId)
                        .addValue("type", type.name().substring(0, 1)),
                (resultSet, rowNum) -> {
                    final Vote vote = new Vote();
                    vote.placeId = resultSet.getInt("place_id");
                    vote.placeName = resultSet.getString("place_name");
                    vote.personId = resultSet.getInt("person_id");
                    vote.personName = resultSet.getString("person_name");
                    vote.type = VoteType.fromInitial(resultSet.getString("type"));
                    return vote;
                });
    }

    int insertPlace(Place place) {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("insert into place (name) values (:name)",
                new MapSqlParameterSource().addValue("name", place.name), keyHolder);
        return keyHolder.getKey().intValue();
    }

    void insertPlaceTag(int placeId, String tag) {
        jdbcTemplate.update("insert into place_tag (place_id, tag_id) values (:placeId, :tagId)",
                new MapSqlParameterSource().addValue("placeId", placeId).addValue("tagId", tag));
    }

    Place getPlace(int placeId) {
        return jdbcTemplate.queryForObject("select * from place where id = :id",
                new MapSqlParameterSource("id", placeId), PLACE_ROW_MAPPER);
    }

    Optional<Place> getPlaceByName(String name) {
        return jdbcTemplate.query("select * from place where name = :name",
                new MapSqlParameterSource("name", name), PLACE_ROW_MAPPER).stream().findFirst();
    }

    void deletePlaceTag(int placeId, String tag) {
        jdbcTemplate.update("delete from place_tag where place_id = :placeId and tag_id = :tagId",
                new MapSqlParameterSource().addValue("placeId", placeId).addValue("tagId", tag));
    }

    void insertVote(int personId, int placeId, VoteType type) {
        jdbcTemplate.update("insert place_vote (place_id, person_id, type) values (:placeId, :personId, :type)",
                new MapSqlParameterSource()
                        .addValue("placeId", placeId)
                        .addValue("personId", personId)
                        .addValue("type", type.name().substring(0, 1)));
    }

}
