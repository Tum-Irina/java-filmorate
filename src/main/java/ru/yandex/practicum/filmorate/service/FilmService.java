package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null || !filmStorage.existsById(film.getId())) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        User user = userService.findById(userId);
        likes.putIfAbsent(filmId, new HashSet<>());
        if (!likes.get(filmId).add(userId)) {
            throw new ValidationException("Пользователь уже лайкнул этот фильм");
        }
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        User user = userService.findById(userId);
        if (likes.containsKey(filmId)) {
            likes.get(filmId).remove(userId);
        }
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> {
                    int likes1 = getLikesCount(f1.getId());
                    int likes2 = getLikesCount(f2.getId());
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    private int getLikesCount(Long filmId) {
        return likes.getOrDefault(filmId, Collections.emptySet()).size();
    }
}