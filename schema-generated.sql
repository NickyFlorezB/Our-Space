create table users (id uuid not null, cellphone varchar(255) not null, email varchar(255) not null unique, password varchar(255) not null, username varchar(255) not null unique, primary key (id));
