CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(30)    NOT NULL UNIQUE,
    name        VARCHAR(200)   NOT NULL,
    price       NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
    stock       INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at  TIMESTAMP      NOT NULL DEFAULT now()
);
