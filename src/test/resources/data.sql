DELETE FROM film_likes;
DELETE FROM film_genres;
DELETE FROM friendships;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa_ratings;

INSERT INTO mpa_ratings (rating_id, code) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

INSERT INTO genres (genre_id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

INSERT INTO users (user_id, email, login, name, birthday) VALUES
(1, 'user1@test.ru', 'user1', 'User One', '1990-01-01'),
(2, 'user2@test.ru', 'user2', 'User Two', '1995-05-15'),
(3, 'user3@test.ru', 'user3', 'User Three', '2000-10-20');

INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) VALUES
(1, 'Film One', 'First test film', '2020-01-01', 120, 1),
(2, 'Film Two', 'Second test film', '2021-05-15', 90, 3),
(3, 'Film Three', 'Third test film', '2022-10-20', 150, 4);

INSERT INTO film_genres (film_id, genre_id) VALUES
(1, 1), (1, 2),  -- Film One: COMEDY, DRAMA
(2, 3),          -- Film Two: CARTOON
(3, 4), (3, 6);  -- Film Three: THRILLER, ACTION

INSERT INTO film_likes (film_id, user_id) VALUES
(1, 1), (1, 2),  -- Film One лайкнули user1 и user2
(2, 1);          -- Film Two лайкнул user1

INSERT INTO friendships (user_id, friend_id) VALUES
(1, 2),  -- user1 добавил user2 в друзья
(1, 3);  -- user1 добавил user3 в друзья