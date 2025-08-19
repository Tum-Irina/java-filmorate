package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        Film film = createValidFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь нарушений");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = createValidFilm();
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Пустое название должно вызывать ошибку");
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        Film film = createValidFilm();
        film.setDescription("Это очень длинное описание которое definitely содержит больше чем двести символов " +
                "и должно вызвать ошибку валидации потому что максимальная длина описания составляет 200 символов " +
                "и это описание явно превышает этот лимит");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Слишком длинное описание должно вызывать ошибку");
    }

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film film = createValidFilm();
        film.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Null дата релиза должна вызывать ошибку");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = createValidFilm();
        film.setDuration(-10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Отрицательная продолжительность должна вызывать ошибку");
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("The Matrix");
        film.setDescription("A computer hacker learns about the true nature of reality");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        return film;
    }
}