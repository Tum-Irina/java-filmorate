package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmGenreStorage.class, FilmDbStorage.class})
class FilmGenreStorageTest {

    private final FilmGenreStorage filmGenreStorage;
    private final FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        filmStorage.findAll().forEach(film -> filmStorage.delete(film.getId()));
        testFilm = createTestFilm();
    }

    @Test
    void testAddGenreToFilm() {
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 1);
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 2);
        Set<Genre> genres = filmGenreStorage.findGenresByFilmId(testFilm.getId());
        assertThat(genres).hasSize(2);
        assertThat(genres)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void testDeleteGenresFromFilm() {
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 1);
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 2);
        assertThat(filmGenreStorage.findGenresByFilmId(testFilm.getId())).hasSize(2);
        filmGenreStorage.deleteGenresFromFilm(testFilm.getId());
        Set<Genre> genres = filmGenreStorage.findGenresByFilmId(testFilm.getId());
        assertThat(genres).isEmpty();
    }

    @Test
    void testFindGenresByFilmId() {
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 1);
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 3);
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 5);
        Set<Genre> genres = filmGenreStorage.findGenresByFilmId(testFilm.getId());
        assertThat(genres).hasSize(3);
        assertThat(genres)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 3, 5); // Без учета порядка
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Мультфильм", "Документальный");
    }

    @Test
    void testGenresAreSortedById() {
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 5);
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 2);
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 4);
        Set<Genre> genres = filmGenreStorage.findGenresByFilmId(testFilm.getId());
        List<Genre> sortedGenres = genres.stream()
                .sorted((g1, g2) -> g1.getId().compareTo(g2.getId()))
                .collect(Collectors.toList());
        assertThat(sortedGenres)
                .extracting(Genre::getId)
                .containsExactly(2, 4, 5);
    }

    @Test
    void testDuplicateGenres() {
        filmGenreStorage.addGenreToFilm(testFilm.getId(), 1);
        assertThrows(org.springframework.dao.DuplicateKeyException.class, () -> {
            filmGenreStorage.addGenreToFilm(testFilm.getId(), 1);
        });
        Set<Genre> genres = filmGenreStorage.findGenresByFilmId(testFilm.getId());
        assertThat(genres).hasSize(1);
        assertThat(genres)
                .extracting(Genre::getId)
                .containsExactly(1);
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
}