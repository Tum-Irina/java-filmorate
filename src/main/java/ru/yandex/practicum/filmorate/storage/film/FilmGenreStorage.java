package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmGenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_GENRE_TO_FILM_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_FROM_FILM_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM_QUERY = "SELECT g.* FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ? ORDER BY g.genre_id";
    private static final String FIND_GENRES_BY_FILM_IDS_QUERY = "SELECT fg.film_id, g.* FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id IN (%s) ORDER BY fg.film_id, g.genre_id";

    public void addGenreToFilm(Long filmId, Integer genreId) {
        jdbcTemplate.update(ADD_GENRE_TO_FILM_QUERY, filmId, genreId);
    }

    public void addAllGenresToFilm(Long filmId, Set<Integer> genreIds) {
        List<Object[]> batchArgs = genreIds.stream()
                .map(genreId -> new Object[]{filmId, genreId})
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(ADD_GENRE_TO_FILM_QUERY, batchArgs);
    }

    public void deleteGenresFromFilm(Long filmId) {
        jdbcTemplate.update(DELETE_GENRES_FROM_FILM_QUERY, filmId);
    }

    public Set<Genre> findGenresByFilmId(Long filmId) {
        return new HashSet<>(jdbcTemplate.query(FIND_GENRES_BY_FILM_QUERY, this::mapRowToGenre, filmId));
    }

    public Map<Long, Set<Genre>> findGenresByFilmIds(List<Long> filmIds) {
        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));
        String sql = String.format(FIND_GENRES_BY_FILM_IDS_QUERY, placeholders);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());
        Map<Long, Set<Genre>> genresByFilmId = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Genre genre = new Genre();
            genre.setId(((Number) row.get("genre_id")).intValue());
            genre.setName((String) row.get("name"));
            genresByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        }
        return genresByFilmId;
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}