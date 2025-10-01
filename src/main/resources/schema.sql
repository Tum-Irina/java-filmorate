create TABLE IF NOT EXISTS mpa_ratings (
    rating_id INTEGER PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE
);

create TABLE IF NOT EXISTS genres (
    genre_id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

create TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE CHECK (email LIKE '%@%'),
    login VARCHAR(255) NOT NULL UNIQUE CHECK (login NOT LIKE '% %'),
    name VARCHAR(255),
    birthday DATE NOT NULL CHECK (birthday <= CURRENT_DATE)
);

create TABLE IF NOT EXISTS films (
    film_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL CHECK (release_date >= '1895-12-28'),
    duration INTEGER NOT NULL CHECK (duration > 0),
    mpa_rating_id INTEGER NOT NULL REFERENCES mpa_ratings(rating_id),
    rate INTEGER DEFAULT 0 NOT NULL CHECK (rate >= 0)
);

create TABLE IF NOT EXISTS film_genres (
    film_id BIGINT NOT NULL REFERENCES films(film_id) ON delete CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(genre_id),
    PRIMARY KEY (film_id, genre_id)
);

create TABLE IF NOT EXISTS film_likes (
    film_id BIGINT NOT NULL REFERENCES films(film_id) ON delete CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON delete CASCADE,
    PRIMARY KEY (film_id, user_id)
);

create TABLE IF NOT EXISTS friendships (
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON delete CASCADE,
    friend_id BIGINT NOT NULL REFERENCES users(user_id) ON delete CASCADE,
    PRIMARY KEY (user_id, friend_id),
    CHECK (user_id != friend_id)
);