CREATE TABLE push_subscriptions (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    endpoint varchar(2048) NOT NULL,
    public_key varchar(256) NOT NULL,
    auth_secret varchar(128) NOT NULL,
    CONSTRAINT push_subscriptions_pkey PRIMARY KEY (id),
    CONSTRAINT push_subscriptions_endpoint_key UNIQUE (endpoint),
    CONSTRAINT push_subscriptions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX push_subscriptions_user_id_idx ON push_subscriptions (user_id);
