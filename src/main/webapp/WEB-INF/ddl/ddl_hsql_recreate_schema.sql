# Schema loeschen
DROP SCHEMA if exists {1} CASCADE;

# Schema anlegen
CREATE SCHEMA {1};

create table Picture (id integer generated by default as identity (start with 1), creationDate datetime, data blob, description varchar(255), mimeType varchar(255), name varchar(255), publicVisible boolean, owner_id integer, primary key (id));
create table Member (id integer generated by default as identity (start with 1), email varchar(255), password varchar(255), role varchar(255), name varchar(255), primary key (id));
create table Member_Member (Member_id integer not null, friendsOf_id integer not null, primary key (Member_id, friendsOf_id));
alter table Picture add constraint FK40C8F4DE6F11D173 foreign key (owner_id) references Member on delete cascade;
alter table Member add constraint email_ unique (email);
alter table Member add constraint name_ unique (name);
alter table Member_Member add constraint FK8BA0F3BF32E89F5A foreign key (friendsOf_id) references Member;
alter table Member_Member add constraint FK8BA0F3BF32B215B foreign key (Member_id) references Member;

INSERT INTO Member (email, password, role, name) VALUES ('admin@picture.com', '$2a$12$Fw0mMs5dXY6we1yv2/9jIumDy/17I8F09mjfwd7dlE2uEzjbRdwfC', 'admin', 'a');
INSERT INTO Member (email, password, role, name) VALUES ('user-b@picture.com', '$2a$12$.nIff9W1T/Ejh31bdLqcee/S.4Pzy6JvjgzROO1dJC9jRz4QQbgO2', 'user', 'b');
INSERT INTO Member (email, password, role, name) VALUES ('user-c@picture.com', '$2a$12$sDQw4yyWlxRej4dkKvLgHu5GROojYb0YQNwoLfiFQ/MlZL2SX7X3.', 'user', 'c');