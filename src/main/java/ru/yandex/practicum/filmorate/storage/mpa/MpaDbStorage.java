package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_MPA_QUERY = "SELECT * FROM mpa_ratings ORDER BY rating_id";
    private static final String FIND_MPA_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE rating_id = ?";
    private static final String EXISTS_MPA_BY_ID_QUERY = "SELECT COUNT(*) FROM mpa_ratings WHERE rating_id = ?";

    @Override
    public Collection<MpaRating> findAll() {
        return jdbcTemplate.query(FIND_ALL_MPA_QUERY, this::mapRowToMpa);
    }

    @Override
    public Optional<MpaRating> findById(Integer id) {
        return jdbcTemplate.query(FIND_MPA_BY_ID_QUERY, this::mapRowToMpa, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_MPA_BY_ID_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("rating_id"));
        mpa.setName(rs.getString("code"));
        return mpa;
    }
}