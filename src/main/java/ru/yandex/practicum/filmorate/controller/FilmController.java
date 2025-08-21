package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private long getNextId() {
        log.debug("Начало генерации следующего ID");
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        long nextId = ++currentMaxId;
        log.debug("Сгенерирован ID: {} (был максимальный: {})", nextId, currentMaxId);
        return nextId;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с ID: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        if (film.getId() == null || !films.containsKey(film.getId())) {
            String errorMessage = "Фильм с ID " + film.getId() + " не найден";
            log.warn("Ошибка обновления: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        films.put(film.getId(), film);
        log.info("Фильм с ID {} успешно обновлен", film.getId());
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов. Всего: {}", films.size());
        return films.values();
    }
}