package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void testFindAllMpaRatings() {
        Collection<MpaRating> mpaRatings = mpaStorage.findAll();
        assertThat(mpaRatings).hasSize(5);
        assertThat(mpaRatings)
                .extracting(MpaRating::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindMpaById() {
        Optional<MpaRating> mpaOptional = mpaStorage.findById(1);
        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId()).isEqualTo(1);
                    assertThat(mpa.getName()).isEqualTo("G");
                });
    }

    @Test
    void testExistsById() {
        assertThat(mpaStorage.existsById(1)).isTrue();
        assertThat(mpaStorage.existsById(2)).isTrue();
        assertThat(mpaStorage.existsById(3)).isTrue();
        assertThat(mpaStorage.existsById(4)).isTrue();
        assertThat(mpaStorage.existsById(5)).isTrue();
        assertThat(mpaStorage.existsById(999)).isFalse();
    }
}