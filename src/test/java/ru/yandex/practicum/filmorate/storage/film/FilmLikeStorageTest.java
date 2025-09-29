package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmLikeStorage.class, FilmDbStorage.class, UserDbStorage.class})
class FilmLikeStorageTest {

    private final FilmLikeStorage filmLikeStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film testFilm;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        filmStorage.findAll().forEach(film -> filmStorage.delete(film.getId()));
        userStorage.findAll().forEach(user -> userStorage.delete(user.getId()));

        testFilm = createTestFilm();
        testUser1 = createTestUser("test1@mail.ru", "testuser1", "Test User 1");
        testUser2 = createTestUser("test2@mail.ru", "testuser2", "Test User 2");
    }

    @Test
    void testAddLike() {
        filmLikeStorage.addLike(testFilm.getId(), testUser1.getId());
        assertThat(filmLikeStorage.existsLike(testFilm.getId(), testUser1.getId())).isTrue();
        Set<Long> likes = filmLikeStorage.findLikesByFilmId(testFilm.getId());
        assertThat(likes).hasSize(1);
        assertThat(likes).contains(testUser1.getId());
    }

    @Test
    void testRemoveLike() {
        filmLikeStorage.addLike(testFilm.getId(), testUser1.getId());
        assertThat(filmLikeStorage.existsLike(testFilm.getId(), testUser1.getId())).isTrue();
        filmLikeStorage.removeLike(testFilm.getId(), testUser1.getId());
        assertThat(filmLikeStorage.existsLike(testFilm.getId(), testUser1.getId())).isFalse();
        Set<Long> likes = filmLikeStorage.findLikesByFilmId(testFilm.getId());
        assertThat(likes).isEmpty();
    }

    @Test
    void testFindLikesByFilmId() {
        filmLikeStorage.addLike(testFilm.getId(), testUser1.getId());
        filmLikeStorage.addLike(testFilm.getId(), testUser2.getId());
        Set<Long> likes = filmLikeStorage.findLikesByFilmId(testFilm.getId());
        assertThat(likes).hasSize(2);
        assertThat(likes).containsExactlyInAnyOrder(testUser1.getId(), testUser2.getId());
    }

    @Test
    void testExistsLike() {
        assertThat(filmLikeStorage.existsLike(testFilm.getId(), testUser1.getId())).isFalse();
        filmLikeStorage.addLike(testFilm.getId(), testUser1.getId());
        assertThat(filmLikeStorage.existsLike(testFilm.getId(), testUser1.getId())).isTrue();
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        return filmStorage.create(film);
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }
}