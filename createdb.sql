CREATE TABLE movies(
  mid   INTEGER PRIMARY KEY,
  title   VARCHAR(150) NOT NULL,
  year    INTEGER NOT NULL,
  all_critic_rating   NUMBER,
  all_critic_num_reviews  INTEGER,
  top_critic_rating   NUMBER,
  top_critic_num_reviews  INTEGER,
  audience_rating   NUMBER,
  audience_num_ratings  INTEGER
);

CREATE TABLE movie_genres(
  mid   INTEGER,
  genre   VARCHAR(50),
  PRIMARY KEY (mid,genre),
  FOREIGN KEY (mid) REFERENCES movies ON DELETE CASCADE
);

CREATE TABLE movie_countries(
  mid   INTEGER,
  country   VARCHAR(50),
  PRIMARY KEY (mid,country),
  FOREIGN KEY (mid) REFERENCES movies ON DELETE CASCADE
);

CREATE TABLE movie_locations(
  mid   INTEGER,
  country   VARCHAR(50),
  PRIMARY KEY (mid,country),
  FOREIGN KEY (mid) REFERENCES movies ON DELETE CASCADE
);

CREATE TABLE tags(
  tagid   INTEGER PRIMARY KEY,
  tagtext   VARCHAR(100) NOT NULL
);

CREATE TABLE movie_tags(
  mid   INTEGER,
  tagid   INTEGER,
  tagweight   INTEGER NOT NULL,
  PRIMARY KEY (mid,tagid),
  FOREIGN KEY (mid) REFERENCES movies ON DELETE CASCADE,
  FOREIGN KEY (tagid) REFERENCES tags ON DELETE CASCADE
);
