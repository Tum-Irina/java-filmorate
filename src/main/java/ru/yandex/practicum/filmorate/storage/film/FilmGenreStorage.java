package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FilmGenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_GENRE_TO_FILM_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_FROM_FILM_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM_QUERY = "SELECT g.* FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ? ORDER BY g.genre_id";

    public void addGenreToFilm(Long filmId, Integer genreId) {
        jdbcTemplate.update(ADD_GENRE_TO_FILM_QUERY, filmId, genreId);
    }

    public void deleteGenresFromFilm(Long filmId) {
        jdbcTemplate.update(DELETE_GENRES_FROM_FILM_QUERY, filmId);
    }

    public Set<Genre> findGenresByFilmId(Long filmId) {
        return new HashSet<>(jdbcTemplate.query(FIND_GENRES_BY_FILM_QUERY, this::mapRowToGenre, filmId));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}