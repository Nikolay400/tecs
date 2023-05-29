delete from user_role;
delete from usr;

insert into usr(id,email,password) values
    (1,'user1@gmail.com', '1'),
    (2,'user2@gmail.com', '2');

insert into user_role(user_id,roles) values
    (1,'USER'),
    (2,'USER');

update usr set password = crypt(password,gen_salt('bf',8));

alter sequence usr_id_seq restart with 10;