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
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> requestedGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Integer> existingGenreIds = genreService.findAllByIds(requestedGenreIds);
            if (requestedGenreIds.size() != existingGenreIds.size()) {
                throw new NotFoundException("Некоторые жанры не найдены");
            }
        }
        Film createdFilm = filmStorage.create(film);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            filmGenreStorage.addAllGenresToFilm(createdFilm.getId(), uniqueGenreIds);
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
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> requestedGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Integer> existingGenreIds = genreService.findAllByIds(requestedGenreIds);
            if (requestedGenreIds.size() != existingGenreIds.size()) {
                throw new NotFoundException("Некоторые жанры не найдены");
            }
        }
        Film updatedFilm = filmStorage.update(film);
        filmGenreStorage.deleteGenresFromFilm(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            filmGenreStorage.addAllGenresToFilm(film.getId(), uniqueGenreIds);
        }
        return findById(updatedFilm.getId());
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        if (films.isEmpty()) {
            return films;
        }
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        Map<Long, Set<Genre>> genresByFilmId = filmGenreStorage.findGenresByFilmIds(filmIds);
        for (Film film : films) {
            Set<Genre> genres = genresByFilmId.getOrDefault(film.getId(), Collections.emptySet());
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
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        Map<Long, Set<Genre>> genresByFilmId = filmGenreStorage.findGenresByFilmIds(filmIds);
        for (Film film : films) {
            Set<Genre> genres = genresByFilmId.getOrDefault(film.getId(), Collections.emptySet());
            film.setGenres(genres);
        }
        return films.stream()
                .sorted((f1, f2) -> Integer.compare(f2.getRate(), f1.getRate()))
                .limit(count)
                .collect(Collectors.toList());
    }
}