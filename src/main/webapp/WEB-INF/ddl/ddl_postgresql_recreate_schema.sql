# Schema loeschen
DROP SCHEMA if exists {1} CASCADE;

# Schema anlegen
CREATE SCHEMA {1} AUTHORIZATION postgres;

create table {1}.Picture ( id SERIAL, creationDate date, data bytea, description varchar(255), mimeType varchar(255), name varchar(255), publicVisible boolean, owner_id integer, primary key (id));
create table {1}.Member ( id SERIAL, email varchar(255), password varchar(255), role varchar(255), name varchar(255), primary key (id));
create table {1}.Member_Member (Member_id integer not null, friendsOf_id integer not null, primary key (Member_id, friendsOf_id));

alter table {1}.Picture add constraint FK40C8F4DE6F11D173 foreign key (owner_id) references {1}.Member on delete cascade;
alter table {1}.Member add constraint email_ unique (email);
alter table {1}.Member add constraint name_ unique (name);
alter table {1}.Member_Member add constraint FK8BA0F3BF32E89F5A foreign key (friendsOf_id) references {1}.Member;
alter table {1}.Member_Member add constraint FK8BA0F3BF32B215B foreign key (Member_id) references {1}.Member;

# create sequence {1}.pc_sequence;

ALTER DATABASE {0} SET search_path TO {1},public;
