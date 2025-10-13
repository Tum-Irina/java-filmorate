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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage.findAll().forEach(film -> filmStorage.delete(film.getId()));
    }

    @Test
    void testFindFilmById() {
        Film newFilm = createTestFilm("Test Film", "Test Description");
        Film createdFilm = filmStorage.create(newFilm);
        Optional<Film> filmOptional = filmStorage.findById(createdFilm.getId());
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(createdFilm.getId());
                    assertThat(film.getName()).isEqualTo("Test Film");
                    assertThat(film.getDescription()).isEqualTo("Test Description");
                    assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
                    assertThat(film.getDuration()).isEqualTo(120);
                    assertThat(film.getMpa().getId()).isEqualTo(1);
                    assertThat(film.getMpa().getName()).isEqualTo("G");
                });
    }

    @Test
    void testFindAllFilms() {
        Film film1 = filmStorage.create(createTestFilm("Film One", "First film"));
        Film film2 = filmStorage.create(createTestFilm("Film Two", "Second film"));
        Collection<Film> films = filmStorage.findAll();
        assertThat(films).hasSize(2);
        assertThat(films)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Film One", "Film Two");
    }

    @Test
    void testCreateFilm() {
        Film newFilm = createTestFilm("New Film", "New film description");
        Film createdFilm = filmStorage.create(newFilm);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("New Film");
        assertThat(createdFilm.getDescription()).isEqualTo("New film description");
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);
        Optional<Film> savedFilm = filmStorage.findById(createdFilm.getId());
        assertThat(savedFilm).isPresent();
    }

    @Test
    void testUpdateFilm() {
        Film originalFilm = filmStorage.create(createTestFilm("Original Film", "Original description"));
        Film filmToUpdate = new Film();
        filmToUpdate.setId(originalFilm.getId());
        filmToUpdate.setName("Updated Film");
        filmToUpdate.setDescription("Updated description");
        filmToUpdate.setReleaseDate(LocalDate.of(2021, 5, 15));
        filmToUpdate.setDuration(150);
        MpaRating mpa = new MpaRating();
        mpa.setId(3);
        filmToUpdate.setMpa(mpa);
        Film updatedFilm = filmStorage.update(filmToUpdate);
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated description");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
        assertThat(updatedFilm.getMpa().getId()).isEqualTo(3);
        Optional<Film> filmFromDb = filmStorage.findById(originalFilm.getId());
        assertThat(filmFromDb)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("Updated Film");
                    assertThat(film.getMpa().getId()).isEqualTo(3);
                });
    }

    @Test
    void testExistsById() {
        Film film = filmStorage.create(createTestFilm("Test Film", "Test"));
        assertThat(filmStorage.existsById(film.getId())).isTrue();
        assertThat(filmStorage.existsById(999L)).isFalse();
    }

    private Film createTestFilm(String name, String description) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        return film;
    }
}