SET IGNORECASE TRUE;

CREATE TABLE IF NOT EXISTS Collection(
    id int not null primary key auto_increment,
    name varchar(256) not null,
    description clob(10k)
);


CREATE TABLE IF NOT EXISTS Param(
                                    id int not null primary key auto_increment,
                                    "key" varchar(4096),
                                    "value" varchar(4096),
                                    description varchar(4096)
);

CREATE TABLE IF NOT EXISTS Header(
                                     id int not null primary key auto_increment,
                                     "key" varchar(4096),
                                     "value" clob(10k),
                                     description varchar(4096)
);

CREATE TABLE IF NOT EXISTS Body(
                                   id int not null primary key auto_increment,
                                   "key" varchar(4096),
                                   "value" clob(10k),
                                   description varchar(4096),
                                   type enum('NONE', 'FORMDATA', 'XWWWFORM', 'RAW', 'BINARY')
);

CREATE TABLE IF NOT EXISTS PreRequestScript(
                                               id int not null primary key auto_increment,
                                               script clob(20k)
                                            );

CREATE TABLE IF NOT EXISTS TestScript(
                                         id int not null primary key auto_increment,
                                         script clob(20k)
);

CREATE TABLE IF NOT EXISTS Environment(
                                          id int not null primary key auto_increment,
                                          name varchar(256) not null
);

CREATE TABLE IF NOT EXISTS Variable(
                                       id int not null primary key auto_increment,
                                       "key" varchar(4096) not null,
                                       "value" clob(10k) not null,
                                       environment_id int not null,

                                       foreign key (environment_id) references Environment(id) on delete cascade
);

CREATE TABLE IF NOT EXISTS Request(
    id int not null primary key auto_increment,
    name varchar(256) not null,
    url varchar(4096) not null,
    method enum('GET', 'POST', 'HEAD', 'OPTIONS', 'PATCH', 'PUT', 'DELETE') not null ,
    collection_id int not null,
    param_id int not null,
    header_id int not null,
    body_id int not null,
    pre_script_id int not null,
    test_script_id int not null,


    foreign key (collection_id) references Collection(id) on delete cascade,
    foreign key (param_id) references Param(id) on delete cascade,
    foreign key (header_id) references Header(id) on delete cascade,
    foreign key (body_id) references Body(id) on delete cascade,
    foreign key (pre_script_id) references PreRequestScript(id) on delete cascade,
    foreign key (test_script_id) references TestScript(id) on delete cascade
);
