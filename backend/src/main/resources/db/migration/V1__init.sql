CREATE TABLE users (
    id uuid NOT NULL,
    cellphone varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    username varchar(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_username_key UNIQUE (username)
);

CREATE TABLE rooms (
    id uuid NOT NULL,
    description varchar(255),
    image_url varchar(255),
    name varchar(255) NOT NULL,
    owner_id uuid NOT NULL,
    CONSTRAINT rooms_pkey PRIMARY KEY (id),
    CONSTRAINT rooms_name_key UNIQUE (name),
    CONSTRAINT rooms_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE room_members (
    role varchar(16) NOT NULL,
    room_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT room_members_pkey PRIMARY KEY (room_id, user_id),
    CONSTRAINT room_members_role_check CHECK (role IN ('MEMBER', 'OWNER')),
    CONSTRAINT room_members_room_id_fkey FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT room_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE books (
    id uuid NOT NULL,
    created_at varchar(255),
    full_title varchar(255),
    isbn_13 varchar(255) NOT NULL,
    latest_revision integer,
    publishers varchar(255)[],
    title varchar(255) NOT NULL,
    weight varchar(255) NOT NULL,
    room_id uuid NOT NULL,
    image_url varchar(2048),
    CONSTRAINT books_pkey PRIMARY KEY (id),
    CONSTRAINT books_isbn_13_key UNIQUE (isbn_13),
    CONSTRAINT books_room_id_fkey FOREIGN KEY (room_id) REFERENCES rooms (id)
);

CREATE TABLE images (
    id uuid NOT NULL,
    content_type varchar(100) NOT NULL,
    size bigint NOT NULL,
    url varchar(2048) NOT NULL,
    room_id uuid NOT NULL,
    comment varchar(1000),
    comment_text varchar(1000),
    title varchar(160),
    CONSTRAINT images_pkey PRIMARY KEY (id),
    CONSTRAINT images_room_id_fkey FOREIGN KEY (room_id) REFERENCES rooms (id)
);

CREATE TABLE movies (
    id uuid NOT NULL,
    comment varchar(255) NOT NULL,
    image_url varchar(2048),
    name varchar(255) NOT NULL,
    url varchar(255) NOT NULL,
    room_id uuid NOT NULL,
    CONSTRAINT movies_pkey PRIMARY KEY (id),
    CONSTRAINT movies_room_id_fkey FOREIGN KEY (room_id) REFERENCES rooms (id)
);

CREATE TABLE songs (
    id uuid NOT NULL,
    comment varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    url varchar(255) NOT NULL,
    room_id uuid NOT NULL,
    CONSTRAINT songs_pkey PRIMARY KEY (id),
    CONSTRAINT songs_room_id_fkey FOREIGN KEY (room_id) REFERENCES rooms (id)
);

CREATE TABLE refresh_tokens (
    id uuid NOT NULL,
    created_at timestamptz(6) NOT NULL,
    expires_at timestamptz(6) NOT NULL,
    family_id uuid NOT NULL,
    revoked_at timestamptz(6),
    token_hash varchar(255) NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT refresh_tokens_token_hash_key UNIQUE (token_hash)
);