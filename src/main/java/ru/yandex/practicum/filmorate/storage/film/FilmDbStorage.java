package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_FILM_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE film_id = ?";
    private static final String FIND_ALL_FILMS_QUERY = "SELECT f.*, m.rating_id as mpa_id, m.code as mpa_code FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.rating_id ORDER BY f.film_id";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT f.*, m.rating_id as mpa_id, m.code as mpa_code FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.rating_id WHERE f.film_id = ?";
    private static final String EXISTS_FILM_BY_ID_QUERY = "SELECT COUNT(*) FROM films WHERE film_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_FILM_QUERY, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        return film;
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return jdbcTemplate.query(FIND_ALL_FILMS_QUERY, this::mapRowToFilm);
    }

    @Override
    public Optional<Film> findById(Long id) {
        return jdbcTemplate.query(FIND_FILM_BY_ID_QUERY, this::mapRowToFilm, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_FILM_BY_ID_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(DELETE_FILM_QUERY, id);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_rating_id"));
        mpa.setName(rs.getString("mpa_code"));
        film.setMpa(mpa);
        film.setGenres(new HashSet<>());
        return film;
    }
}