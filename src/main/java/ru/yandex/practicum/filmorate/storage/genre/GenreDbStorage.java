package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String EXISTS_GENRE_BY_ID_QUERY = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";

    @Override
    public Collection<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_GENRES_QUERY, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return jdbcTemplate.query(FIND_GENRE_BY_ID_QUERY, this::mapRowToGenre, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_GENRE_BY_ID_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}