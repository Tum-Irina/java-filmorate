package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage.findAll().forEach(user -> userStorage.delete(user.getId()));
    }

    @Test
    void testFindUserById() {
        User newUser = createTestUser("test@mail.ru", "testuser", "Test User");
        User createdUser = userStorage.create(newUser);
        Optional<User> userOptional = userStorage.findById(createdUser.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(createdUser.getId());
                    assertThat(user.getEmail()).isEqualTo("test@mail.ru");
                    assertThat(user.getLogin()).isEqualTo("testuser");
                    assertThat(user.getName()).isEqualTo("Test User");
                    assertThat(user.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void testFindUserById_NotFound() {
        Optional<User> userOptional = userStorage.findById(999L);
        assertThat(userOptional).isEmpty();
    }

    @Test
    void testFindAllUsers() {
        User user1 = userStorage.create(createTestUser("user1@mail.ru", "user1", "User One"));
        User user2 = userStorage.create(createTestUser("user2@mail.ru", "user2", "User Two"));
        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@mail.ru", "user2@mail.ru");
    }

    @Test
    void testCreateUser() {
        User newUser = createTestUser("new@mail.ru", "newuser", "New User");
        User createdUser = userStorage.create(newUser);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("new@mail.ru");
        assertThat(createdUser.getLogin()).isEqualTo("newuser");
        assertThat(createdUser.getName()).isEqualTo("New User");
        Optional<User> savedUser = userStorage.findById(createdUser.getId());
        assertThat(savedUser).isPresent();
    }

    @Test
    void testUpdateUser() {
        User originalUser = userStorage.create(createTestUser("original@mail.ru", "original", "Original"));
        User userToUpdate = new User();
        userToUpdate.setId(originalUser.getId());
        userToUpdate.setEmail("updated@mail.ru");
        userToUpdate.setLogin("updated");
        userToUpdate.setName("Updated Name");
        userToUpdate.setBirthday(LocalDate.of(1995, 5, 15));
        User updatedUser = userStorage.update(userToUpdate);
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("updated");
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        Optional<User> userFromDb = userStorage.findById(originalUser.getId());
        assertThat(userFromDb)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo("updated@mail.ru");
                    assertThat(user.getLogin()).isEqualTo("updated");
                });
    }

    @Test
    void testExistsById() {
        User user = userStorage.create(createTestUser("test@mail.ru", "test", "Test"));
        assertThat(userStorage.existsById(user.getId())).isTrue();
        assertThat(userStorage.existsById(999L)).isFalse();
    }

    @Test
    void testDeleteUser() {
        User user = userStorage.create(createTestUser("delete@mail.ru", "delete", "Delete"));
        assertThat(userStorage.existsById(user.getId())).isTrue();
        userStorage.delete(user.getId());
        assertThat(userStorage.existsById(user.getId())).isFalse();
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}