create table cart (
    id bigserial not null,
    quantity integer not null,
    product_id bigint,
    user_id bigint,
    primary key (id)
);

create table category (
    id serial not null,
    alias varchar(255),
    name varchar(255),
    ordr integer not null,
    parent integer not null,
    primary key (id)
);

create table ordr (
    id bigserial not null,
    date timestamp(6),
    file varchar(255),
    status varchar(255),
    primary key (id)
);

create table product (
    id bigserial not null,
    alias varchar(255),
    description varchar(255),
    img varchar(255),
    name varchar(255),
    price float4 not null,
    quantity integer not null,
    category_id integer,
    vendor_id integer,
    primary key (id)
);

create table user_role (
    user_id bigint not null,
    roles varchar(255)
);
create table usr (
    id bigserial not null,
    email varchar(255),
    name varchar(255),
    password varchar(255),
    phone varchar(255),
    surname varchar(255),
    primary key (id)
);

create table vendor (
    id serial not null,
    img varchar(255),
    name varchar(255),
    primary key (id)
);

alter table if exists cart add constraint FK3d704slv66tw6x5hmbm6p2x3u foreign key (product_id) references product;
alter table if exists cart add constraint FKc9objqhvjc84nmsxvwk64dajp foreign key (user_id) references usr;
alter table if exists product add constraint FK1mtsbur82frn64de7balymq9s foreign key (category_id) references category;
alter table if exists product add constraint FK9tnjxr4w1dcvbo2qejikpxpfy foreign key (vendor_id) references vendor;
alter table if exists user_role add constraint FKfpm8swft53ulq2hl11yplpr5 foreign key (user_id) references usr;