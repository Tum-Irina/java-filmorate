package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class})
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void testFindAllGenres() {
        Collection<Genre> genres = genreStorage.findAll();
        assertThat(genres).hasSize(6);
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма", "Мультфильм",
                        "Триллер", "Документальный", "Боевик");
    }

    @Test
    void testFindGenreById() {
        Optional<Genre> genreOptional = genreStorage.findById(1);
        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    void testExistsById() {
        assertThat(genreStorage.existsById(1)).isTrue();
        assertThat(genreStorage.existsById(2)).isTrue();
        assertThat(genreStorage.existsById(3)).isTrue();
        assertThat(genreStorage.existsById(4)).isTrue();
        assertThat(genreStorage.existsById(5)).isTrue();
        assertThat(genreStorage.existsById(6)).isTrue();
        assertThat(genreStorage.existsById(999)).isFalse();
    }
}