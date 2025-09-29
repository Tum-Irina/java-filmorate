package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.Collection;
import java.util.Optional;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_USER_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users ORDER BY user_id";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String EXISTS_USER_BY_ID_QUERY = "SELECT COUNT(*) FROM users WHERE user_id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_USER_QUERY, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(UPDATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_USERS_QUERY, this::mapRowToUser);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbcTemplate.query(FIND_USER_BY_ID_QUERY, this::mapRowToUser, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_USER_BY_ID_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(DELETE_USER_QUERY, id);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}