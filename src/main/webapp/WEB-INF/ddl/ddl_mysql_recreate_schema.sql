# Schema loeschen
# DROP SCHEMA if exists {1};

# Schema anlegen
CREATE SCHEMA IF NOT EXISTS {1};

DROP TABLE IF EXISTS Picture;
DROP TABLE IF EXISTS Member_Member;
DROP TABLE IF EXISTS Member;

create table Picture (id integer not null auto_increment, creationDate datetime, data LONGBLOB, description varchar(255), mimeType varchar(255), name varchar(255), publicVisible boolean, owner_id integer, primary key (id));
create table Member (id integer not null auto_increment, email varchar(255), password varchar(255), role varchar(255), name varchar(255), primary key (id));
create table Member_Member (Member_id integer not null, friendsOf_id integer not null, primary key (Member_id, friendsOf_id));
alter table Picture add index FK40C8F4DE6F11D173 (owner_id), add constraint FK40C8F4DE6F11D173 foreign key (owner_id) references Member (id);
alter table Member add constraint email_ unique (email);
alter table Member add constraint username_ unique (name);
alter table Member_Member add index FK8BA0F3BF32E89F5A (friendsOf_id), add constraint FK8BA0F3BF32E89F5A foreign key (friendsOf_id) references Member (id);
alter table Member_Member add index FK8BA0F3BF32B215B (Member_id), add constraint FK8BA0F3BF32B215B foreign key (Member_id) references Member (id);
