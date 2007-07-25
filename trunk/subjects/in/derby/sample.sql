connect 'jdbc:derby:subjects/out/derby/AmockDB';
create table derbyDB(num int, addr varchar(40));
insert into derbyDB values (1956, 'Webster St.');
insert into derbyDB values (1910, 'Union St.');
update derbyDB set num=180, addr='Grand Ave.' where num=1956;
select * from derbyDb;
