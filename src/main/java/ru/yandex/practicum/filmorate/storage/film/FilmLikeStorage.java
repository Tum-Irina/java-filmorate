package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FilmLikeStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_BY_FILM_QUERY = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String EXISTS_LIKE_QUERY = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String INCREASE_RATE_QUERY = "UPDATE films SET rate = rate + 1 WHERE film_id = ?";
    private static final String DECREASE_RATE_QUERY = "UPDATE films SET rate = GREATEST(rate - 1, 0) WHERE film_id = ?";

    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(ADD_LIKE_QUERY, filmId, userId);
        jdbcTemplate.update(INCREASE_RATE_QUERY, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(REMOVE_LIKE_QUERY, filmId, userId);
        jdbcTemplate.update(DECREASE_RATE_QUERY, filmId);
    }

    public Set<Long> findLikesByFilmId(Long filmId) {
        return new HashSet<>(jdbcTemplate.query(
                FIND_LIKES_BY_FILM_QUERY,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        ));
    }

    public boolean existsLike(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_LIKE_QUERY, Integer.class, filmId, userId);
        return count != null && count > 0;
    }
}