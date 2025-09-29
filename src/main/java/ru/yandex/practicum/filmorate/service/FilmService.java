package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FilmGenreStorage filmGenreStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       FilmGenreStorage filmGenreStorage,
                       FilmLikeStorage filmLikeStorage,
                       MpaService mpaService,
                       GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.filmGenreStorage = filmGenreStorage;
        this.filmLikeStorage = filmLikeStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Film create(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.findById(film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    genreService.findById(genre.getId());
                }
            }
        }
        Film createdFilm = filmStorage.create(film);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (Integer genreId : uniqueGenreIds) {
                filmGenreStorage.addGenreToFilm(createdFilm.getId(), genreId);
            }
        }
        return findById(createdFilm.getId());
    }

    public Film update(Film film) {
        if (film.getId() == null || !filmStorage.existsById(film.getId())) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.findById(film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    genreService.findById(genre.getId());
                }
            }
        }
        Film updatedFilm = filmStorage.update(film);
        filmGenreStorage.deleteGenresFromFilm(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (Integer genreId : uniqueGenreIds) {
                filmGenreStorage.addGenreToFilm(film.getId(), genreId);
            }
        }
        return findById(updatedFilm.getId());
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        for (Film film : films) {
            Set<Genre> genres = filmGenreStorage.findGenresByFilmId(film.getId());
            film.setGenres(genres);
        }
        return films;
    }

    public Film findById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        Set<Genre> genres = filmGenreStorage.findGenresByFilmId(id);
        Set<Genre> sortedGenres = genres.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(sortedGenres);
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        User user = userService.findById(userId);
        if (filmLikeStorage.existsLike(filmId, userId)) {
            throw new ValidationException("Пользователь уже лайкнул этот фильм");
        }
        filmLikeStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        User user = userService.findById(userId);
        filmLikeStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        Collection<Film> films = filmStorage.findAll();
        for (Film film : films) {
            Set<Genre> genres = filmGenreStorage.findGenresByFilmId(film.getId());
            film.setGenres(genres);
            Set<Long> likes = filmLikeStorage.findLikesByFilmId(film.getId());
        }
        return films.stream()
                .sorted((f1, f2) -> {
                    int likes1 = filmLikeStorage.findLikesByFilmId(f1.getId()).size();
                    int likes2 = filmLikeStorage.findLikesByFilmId(f2.getId()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }
}