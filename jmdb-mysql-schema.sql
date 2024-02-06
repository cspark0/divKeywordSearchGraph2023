
mysql -ucspark -pcspark1234


drop table paper;
drop table author;
drop table venue;		//book, journal, conference, ...

create table paper {
(id mediumint unsigned auto_increment primary key,
 actorid mediumint unsigned,
 name varchar(100))
auto_increment=2000000;

insert into actors0 (actorid, name)

----------------------------------------------------------------\
----------------------------------------------------------------\
----------------------------------------------------------------\
----------------------------------------------------------------\
----------------------------------------------------------------\
----------------------------------------------------------------\

select title, name from movies0 m, movies2actors0 ma, actors0 a, keywords0 k where m.movieid = ma.movieid and  ma.actorid = a.actorid and m.movieid = k.movieid and k.keywords like '%Superhero%' order by title;

 select title, name, keywords from movies0 m, movies2actors0 ma, actors0 a, keywords0 k where m.movieid = ma.movieid and  ma.actorid = a.actorid and m.movieid = k.movieid and k.keywords like '%Superhero%' order by title;

select title, name 
from movies0 m, movies2actors0 ma, actors0 a
where m.movieid = ma.movieid and 
ma.actorid = a.actorid and
m.title like '%hero%';

select title, name, keyword from movies0 m, movies2actors0 ma, actors0 a, keywords00 k where m.movieid = ma.movieid and  ma.actorid = a.actorid and m.movieid = k.movieid and k.keyword like '%Superhero%' order by title;

----------------------------------------------------------------\

mysqldump -ucspark -p jmdb > jmdb.sql 
mysqldump -ucspark -p jmdb NMR > jmdb.NMR.sql 

mysql -ucspark -p < jmdb.sql 
mysql -ucspark -p jmdb < jmdb.NMR.sql 

-----------------------------------------------------------------
# 0. prepare useful tables....
####################################################
/*
drop table eng_lang;
drop table bad_genres;
drop table eng_lang_good_genres;
drop table movies0;

create table bad_lang
	select movieid from language
	where language not like '%English%';

create table bad_genres
	select movieid from genres
	where genre in ('Reality-TV', 'Talk-Show', 'Game-Show', 'News');

create table bad_lang_bad_genres
	select * from bad_lang
	union
	select * from bad_genres;
create index ind_lang_gen  on bad_lang_bad_genres(movieid);

create table movies0
	select movieid, title, year from movies
	where year >= '2000' and year <= '2017' and
		movieid not in (select movieid from bad_lang_bad_genres);
*/
drop table movies0;
drop table actors0;
drop table directors0;
drop table genres0;
drop table keywords0;
drop table plots0;
drop table movielinks0;
DROP TABLE mov2act0;
DROP TABLE mov2dir0;
DROP TABLE mov2genres0;
DROP TABLE mov2keywords0;


create table movies_sel0
	select * from movies 
	where title not like '%{%}%';
create table movies_sel1
	select * from movies_sel0
	where title not like '%(%)%(%)%';
create table movies_sel2
	select * from movies_sel1
	where (title > '"A' and title < '"Z') or 
		(title > 'A' and title < 'Z');

create table genres_sel
	select movieid from genres where genre not in ('Short', 'Adult', 'Documentary', 'Reality-TV', 'Talk-Show', 'Game-Show', 'News');
create index gsel on genres_sel(movieid);

create table movies0
	select movieid, title, year from movies_sel2
	# where (year='1992' or year >='2000') and year <> '????'
	#where year >='1998' and year <> '????'
	where year >='2000' and year <> '????'
	and movieid in (select movieid from genres_sel)
	and movieid in (select movieid from movies2actors)
	and movieid in (select movieid from movies2directors)
	and (movieid in (select movieid from keywords) 
	      or 	
        movieid in (select movieid from plots));
alter table movies0 add constraint primary key(movieid);

# result in 63K movies which have actor, director, genre, and 
at least keyword or plot
/*
create table mov2act_dist
  select distinct(movieid) from movies2actors;
create index imd on mov2act_dist (movieid);
create table mov1
  select m.movieid, title, year from mov0 m, mov2act_dist k  where m.movieid = k.movieid;
create index indmo1 on mov1 (movieid);

create table mov2dir_dist                                           
  select distinct(movieid) from movies2directors ;
create index imd on mov2dir_dist (movieid);  

create table movies0
  select m.movieid, title, year from mov1 m, mov2dir_dist k  where m.movieid = k.movieid;
alter table movies0 add constraint primary key(movieid);

drop table mov0;
drop table mov1;
drop table mov2act_dist;
drop table mov2dir_dist;
*/
drop table movies_sel0;
drop table movies_sel1;
drop table movies_sel2;
drop table genres_sel;


####################################################

create table movies2actors0		# auxiliary table
	select movieid, actorid
	from movies2actors
	where movieid in (select movieid from movies0);
#create index ind_mov2act0_m on movies2actors0(movieid);
create index ind_mov2act0_a on movies2actors0(actorid);

CREATE TABLE actors0
(id mediumint unsigned auto_increment primary key,
 actorid mediumint unsigned,
 name varchar(100))
auto_increment=2000000;

insert into actors0 (actorid, name)
	select actorid, name 
	from actors
	where actorid in (select actorid from movies2actors0);
commit;
create index ind_act0 on actors0(actorid);

CREATE TABLE mov2act0
	select a.movieid, b.id as actortableid
	from movies2actors0 a, actors0 b 
	where a.actorid = b.actorid;

# results in 480K actors 851K mov2act0
####################################################

create table movies2dirs0		# auxiliary table
	select movieid, directorid
	from movies2directors
	where movieid in (select movieid from movies0);
create index ind_mov2dir0 on movies2dirs0(directorid);

CREATE TABLE directors0
(id mediumint unsigned auto_increment primary key,
 directorid mediumint unsigned,
 name varchar(100))
auto_increment=4000000;

insert into directors0 (directorid, name)
	select directorid, name 
	from directors
	where directorid in (select directorid from movies2dirs0);
commit;
create index ind_dir0 on directors0(directorid);

CREATE TABLE mov2dir0
	select a.movieid, b.id as directortableid
	from movies2dirs0 a, directors0 b 
	where a.directorid = b.directorid;

# results in 50K directors and 71K mov2dir0

DROP table movies2actors0;		# auxiliary table
DROP table movies2dirs0;	# auxiliary table
####################################################
/*
CREATE TABLE akatitles0
(id int unsigned auto_increment primary key,
 movieid mediumint unsigned,
 title VARCHAR(300))
auto_increment=5000000;

CREATE TABLE genres0
(id int unsigned auto_increment primary key,
 movieid mediumint unsigned,
 genre VARCHAR(20))
auto_increment=6000000;

CREATE TABLE keywords0
(id int unsigned auto_increment primary key,
 movieid mediumint unsigned,
 keyword VARCHAR(80))
auto_increment=8000000;
*/
/*
insert into akatitles0 (movieid, title)
	select a.movieid, a.title from akatitles a, movies0 b
	where a.movieid = b.movieid;
commit;
insert into genres0 (movieid, genre)
	select a.movieid, a.genre from genres a, movies0 b
	where a.movieid = b.movieid;
commit;
insert into keywords0 (movieid, keyword)
	select a.movieid, a.keyword from keywords a, movies0 b
	where a.movieid = b.movieid;
commit;
*/

CREATE TABLE plots0
(id mediumint unsigned auto_increment primary key,
 movieid mediumint unsigned,
 plottext text)
auto_increment=6000000;

CREATE TABLE movielinks0
(id mediumint unsigned auto_increment primary key,
 movieid mediumint unsigned,
 movielinkstext text)
auto_increment=7000000;

insert into plots0 (movieid, plottext)
	select movieid, plottext 
	from plots a
	where movieid in (select movieid from movies0);
commit;
insert into movielinks0 (movieid, movielinkstext)
	select movieid, movielinkstext 
	from movielinks 
	where movieid in (select movieid from movies0);
commit;

############################################################
#  create tables for genres and keywords concaterated into one tuple per movie
############################################################

/*
DROP TABLE genres0;
DROP TABLE keywords0;

CREATE TABLE genres0
(id mediumint unsigned auto_increment primary key,
 movieid mediumint unsigned,
 genres VARCHAR(100))
auto_increment=5000000;

CREATE TABLE keywords0
(id mediumint unsigned auto_increment primary key,
 movieid mediumint unsigned,
 keywords VARCHAR(1000))
auto_increment=8000000;

insert into genres0(movieid, genres)
	select movieid, substr(group_concat(genre),1,100) as genres 
	from genres
	where movieid in (select movieid from movies0) 
	group by movieid;
commit;

SET SESSION group_concat_max_len=15000;
insert into keywords0(movieid, keywords)
	select movieid, substr(group_concat(distinct keyword),1,1000) as keywords 
	from keywords 
	where movieid in (select movieid from movies0) 
	group by movieid;
commit;
*/


create table genres_c		# auxiliary table
	select movieid, substr(group_concat(genre),1,100) as genres 
	from genres
	where movieid in (select movieid from movies0) 
	group by movieid;

#create index ind_genres_c on genres_c(genres);

CREATE TABLE genres0
(id mediumint unsigned auto_increment primary key,
 genres VARCHAR(100))
auto_increment=5000000;

insert into genres0(genres)
	select distinct genres 
	from genres_c
commit;
create index ind_genres0 on genres0(genres);

CREATE TABLE mov2genres0
	select a.movieid, b.id as genresid
	from genres_c a, genres0 b 
	where a.genres = b.genres;

DROP TABLE genres_c;
############################################################

SET SESSION group_concat_max_len=15000;
create table keywords_c   # auxiliary table
	select movieid, substr(group_concat(distinct keyword),1,1000) as keywords 
	from keywords 
	where movieid in (select movieid from movies0) 
	group by movieid;

CREATE TABLE keywords0
(id mediumint unsigned auto_increment primary key,
 keywords VARCHAR(1000))
auto_increment=8000000;

insert into keywords0(keywords)
	select distinct keywords
	from keywords_c
commit;
create index ind_keywords0 on keywords0(keywords);

CREATE TABLE mov2keywords0
	select a.movieid, b.id as keywordsid
	from keywords_c a, keywords0 b 
	where a.keywords = b.keywords;

DROP TABLE keywords_c;

# results in 6057 genres0, 63K mov2genres0, 37K keywords0, 45K mov2keywords0
/*
create table genres_st1 
select a.id as genreid, a.genre, count(b.movieid) as numOfMovies 
 from genres0 a join genres00 b on a.genre = b.genre
 group by a.id, a.genre;
create index igs1 on genres_st1(genre);

create table genres_st2 
select movieid, count(genre) as numOfGenres from genres00 group by movieid;
create index igs2 on genres_st2(movieid);

create table movies2genres
select a.movieid, b.genreid, b.numOfMovies, c.numOfGenres
 from genres00 a join genres_st1 b on a.genre = b.genre
	join genres_st2 c on a.movieid = c.movieid;

select a.id as keywordid, a.keyword, count(b.movieid) as numOfMovies 
 from keywords0 a join keywords00 b on a.keyword = b.keyword
 group by a.id, a.keyword;
create index igs1 on keywords_st1(keyword);

create table keywords_st2 
select movieid, count(keyword) as numOfKeywords from keywords00 group by movieid;
create index igs2 on keywords_st2(movieid);

create table movies2keywords
select a.movieid, b.keywordid, b.numOfMovies, c.numOfKeywords
 from keywords00 a join keywords_st1 b on a.keyword = b.keyword
	join keywords_st2 c on a.movieid = c.movieid;
*/
############################################################


# 0.5. create a lucene index for text attributes of all tuples 
cd (project_home)
rm res/jmdb/index-jmdb/(*)

java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select movieid as id, title as text from movies0" -c "jdbc:mysql://localhost/jmdb?user=cspark&password=cspark1234" -l res/jmdb/index-jmdb -i "movieid=Index.NO:Store.YES:TermVector.NO" -map "movieid=id" -map "title=text" 
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q "select id, name as text from actors0" -c "jdbc:mysql://localhost/jmdb?user=cspark&password=cspark1234" -l res/jmdb/index-jmdb  -A  -i "id=Index.NO:Store.YES:TermVector.NO"  -map "name=text"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q "select id, name as text from directors0" -c "jdbc:mysql://localhost/jmdb?user=cspark&password=cspark1234" -l res/jmdb/index-jmdb -A  -i "id=Index.NO:Store.YES:TermVector.NO"  -map "name=text"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q "select id, genres as text from genres0" -c "jdbc:mysql://localhost/jmdb?user=cspark&password=cspark1234" -l res/jmdb/index-jmdb  -A  -i "id=Index.NO:Store.YES:TermVector.NO" -map "genres=text"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q "select id, plottext as text from plots0" -c "jdbc:mysql://localhost/jmdb?user=cspark&password=cspark1234" -l res/jmdb/index-jmdb  -A  -i "id=Index.NO:Store.YES:TermVector.NO" -map "plottext=text"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q "select id, movielinkstext as text from movielinks0" -c "jdbc:mysql://localhost/jmdb?user=cspark&password=cspark1234" -l res/jmdb/index-jmdb -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "movielinkstext=text"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q "select id, keywords as text from keywords0" -c "jdbc:mysql://localhost/jmdb?user=cspark&password=cspark1234" -l res/jmdb/index-jmdb   -A  -i "id=Index.NO:Store.YES:TermVector.NO" -map "keywords=text"

# 0.6.  build a table KNS (and its index) from lucene index (compute scores) 
drop table KNS;
ant jmdb.loadLuceneIndexToKNS 
(including create index ikns on KNS(keyword);)
## There are 5337457 keywords and 294711 distinct keywords in the graph.
# There are 5188055 keywords and 294710 distinct keywords in the graph.
# There are 4790961 keywords and 280051 distinct keywords in the graph.

# 1.
# 2. compute Node-Node relationship into NMR.txt
# 3. build NMR table and index for it
drop table NMR;		# cautious!!
create table NMR
(endNode mediumint unsigned not null,
startNode mediumint unsigned not null,
firstNode mediumint unsigned not null,
rel float);

## There are 5513899 vertices, 14416608 edges in the graph.
# There are 736811 vertices, 2208291 edges in the graph.
# There are 687257 vertices, 2003395 edges in the graph.

#rm res/jmdb/NMR.txt  	# custious !!!
rm res/jmdb/NMR.0.1000000  	# custious !!!
rm res/jmdb/NMR.1000000.2000000  	# custious !!!
rm res/jmdb/NMR.2000000.3000000  	# custious !!!
...

ant jmdb.buildGraph1 	# compute NMR for start node No.: 2000000 ~ 3000000
ant jmdb.buildGraph2 	# compute NMR for start node No.1000000 <= < 2000000
nohup ant jmdb.buildGraph3& 	# compute NMR for start node No. 0 ~ 1000000
ant jmdb.buildGraph5 	# compute NMR for start node No.: 4000000 ~ 5000000
ant jmdb.buildGraph6 	# compute NMR for start node No.: 5000000 ~ 6000000
ant jmdb.buildGraph7 	# compute NMR for start node No.: 6000000 ~ 7000000
ant jmdb.buildGraph8 	# compute NMR for start node No.: 7000000 ~ 8000000
ant jmdb.buildGraph9 	# compute NMR for start node No.: 8000000 ~ 9000000
mysqlimport --user='' --password='cspark1234' -p jmdb --local 'res/jmdb/NMR.0.1000000' --fields-terminated-by=',' 
mysqlimport --user='' --password='cspark1234'  -p jmdb --local 'res/jmdb/NMR.1000000.2000000' --fields-terminated-by=',' 
mysqlimport --user='' --password='cspark1234' -p jmdb --local 'res/jmdb/NMR.2000000.3000000' --fields-terminated-by=',' 
mysqlimport --user='' --password='cspark1234' -p jmdb --local 'res/jmdb/NMR.4000000.5000000' --fields-terminated-by=',' 
mysqlimport --user='' --password='cspark1234' -p jmdb --local 'res/jmdb/NMR.5000000.6000000' --fields-terminated-by=',' 
mysqlimport --user='' --password='cspark1234' -p jmdb --local 'res/jmdb/NMR.6000000.7000000' --fields-terminated-by=',' 
mysqlimport --user='' --password='cspark1234' -p jmdb --local 'res/jmdb/NMR.7000000.8000000' --fields-terminated-by=',' 
mysqlimport --user='' --password='cspark1234' -p jmdb --local 'res/jmdb/NMR.8000000.9000000' --fields-terminated-by=',' 
create index inmr on NMR(endNode);

## There are 3474931037 records in NMR table
# There are 3019606263 records in NMR table

# 4. explain  
# select a.keyword, b.startNode, b.endNode, b.firstNode, a.score*b.rel AS relv   
# from KNS a, NMR b where a.srcNode = b.endNode  order by a.keyword, relv desc;

# 5. build KNS_SEL for selected keywords 
drop table KNS_SEL;
/*
drop table KNS_1;
drop table KNS_2;
drop table KNS_3;
drop table KNS_4;
drop table KNS_5;
drop table KNS_6;
*/

create table KNS_SEL
  select * from KNS where keyword in (
"hitchcock", "mystery", "thriller", "horror","suspense", "panic",
"crash", "explosion", "escape", "disaster", "rescue", 
"hero","survival","destruction","gangster", "fight", "crime", "murder", "trial", "violence", "police", 
"criminal", "spy", "terrorist", "chase", "deception", "betrayal",
"warrior", "war", "battle", "army", "military","killer", "ship", "soldier", "attack", "invasion", "massacre", "weapon", "aircraft", "fighter", "rocket",
"sea", "ocean", "return", "space", "earth", "moon", "mars", "star", "planet", "time", "travel", "future","spaceship",
"western", "cowboy", "sheriff", "gunman", "action", "superhero","adventure",
"history", "historical", "rome", "empire", "emperor", "king", "conquest", "epic", "independence", "colony",
"award", "academy", "oscar", 
"crowe", "russell", "tom", "cruise", "hanks", "morgan", "freeman", "matt", "damon", "leonardo", "dicaprio",
"tragedy","revenge", "reality", "nonfiction", "non-fiction",  
"elf", "dwarf", "fantasy", "vampire", "alien","ghost", "zombie", "monster", "devil","human","miracle",
"political", "politics", "drama", "romance","romantic", "comedy", "christmas", "sacrifice",
"musical", "love", "success", "happiness","friendship","relationship","religion", "justice", "conflict",
"animation", "family", "kids", "animal", "knight", "beast", "prince", "princess", "sport", "sf", "sci-fi", "robot", "machine", "dystopia"
"noir", "wedding", "apocalypse", "past", "nuclear");
create index ikns_sel on KNS_SEL(srcNode);

/*
  select * from KNS where keyword in (
"hitchcock", "mystery", "thriller", "horror","suspense", "panic",
"crash", "explosion", "escape", "disaster", "rescue", 
"hero","survival","destruction","gangster", "fight", "crime", "murder", "trial", "violence", "police", 
"criminal", "spy", "terrorist", "chase", "deception", "betrayal",
"warrior", "war", "battle", "army", "military","killer", "ship", "soldier", "attack", "invasion", "massacre", "weapon", "aircraft", "fighter", "rocket");
create index ikns_1 on KNS_1(srcNode);

create table KNS_2
  select * from KNS where keyword in (
"sea", "ocean", "return", "space", "earth", "moon", "mars", "star", "planet", "time", "travel", "future","spaceship",
"western", "cowboy", "sheriff", "gunman", "action", "superhero","adventure",
"history", "historical", "rome", "empire", "emperor", "king", "conquest", "epic", "independence", "colony");
create index ikns_2 on KNS_2(srcNode);

create table KNS_4
  select * from KNS where keyword in (
"award", "academy", "oscar", 
"crowe", "russell", "tom", "cruise", "hanks", "morgan", "freeman", "matt", "damon", "leonardo", "dicaprio");      # ... selected keywords list .. 
create index ikns_4 on KNS_4(srcNode);

create table KNS_3
  select * from KNS where keyword in (
"tragedy","revenge", "reality", "nonfiction", "non-fiction",  
"elf", "dwarf", "fantasy", "vampire", "alien","ghost", "zombie", "monster", "devil","human","miracle");
create index ikns_3 on KNS_3(srcNode);

create table KNS_6
  select * from KNS where keyword in (
"political", "politics", "drama", "romance","romantic", "comedy", "christmas", "sacrifice");
create index ikns_6 on KNS_6(srcNode);

create table KNS_5
  select * from KNS where keyword in (
"musical", "love", "success", "happiness","friendship","relationship","religion", "justice", "conflict",
"animation", "family", "kids", "animal", "knight", "beast", "prince", "princess", "sport", "sf", "sci-fi", "robot", "machine", "dystopia");
create index ikns_5 on KNS_5(srcNode);
*/

/*
# 6.  compute KNNR for selected keywords using KNS_1 and NMR tables
drop table KNNR;
drop table KNNR_1; # cautious!!! 
drop table KNNR_2;
drop table KNNR_3;
drop table KNNR_4;
drop table KNNR_5;
drop table KNNR_6;

create table KNNR_1
	select a.keyword, b.startNode as destNode, b.endNode as srcNode, b.firstNode, a.score*b.rel AS rel   
	from KNS_1 a, NMR b where a.srcNode = b.endNode 
	order by a.keyword, rel desc;


create table KNNR_2
	select a.keyword, b.startNode as destNode, b.endNode as srcNode, b.firstNode, a.score*b.rel AS rel   
	from KNS_2 a, NMR b where a.srcNode = b.endNode
	order by a.keyword, rel desc;
create table KNNR_3
	select a.keyword, b.startNode as destNode, b.endNode as srcNode, b.firstNode, a.score*b.rel AS rel   
	from KNS_3 a, NMR b where a.srcNode = b.endNode
	order by a.keyword, rel desc;
create table KNNR_4
	select a.keyword, b.startNode as destNode, b.endNode as srcNode, b.firstNode, a.score*b.rel AS rel   
	from KNS_4 a, NMR b where a.srcNode = b.endNode
	order by a.keyword, rel desc;
create table KNNR_5
	select a.keyword, b.startNode as destNode, b.endNode as srcNode, b.firstNode, a.score*b.rel AS rel   
	from KNS_5 a, NMR b where a.srcNode = b.endNode
	order by a.keyword, rel desc;
create table KNNR_6
	select a.keyword, b.startNode as destNode, b.endNode as srcNode, b.firstNode, a.score*b.rel AS rel   
	from KNS_6 a, NMR b where a.srcNode = b.endNode
	order by a.keyword, rel desc;
*/

/*
select a.keyword, b.startNode as destNode, b.endNode as srcNode, b.firstNode, a.score*b.rel AS rel   
  INTO OUTFILE 'res/jmdb/knnr_1.txt'
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
from KNS_1 a, NMR b where a.srcNode = b.endNode
order by a.keyword, rel desc;
*/

# 7. from KNNR_1, build KNList files(in basicIndex/) and NKMap in BDB(in NKMapForBlink/ and NKMapForBlink2/)
rm res/jmdb/basicIndex/(*)
rm res/jmdb/NKMapForBlink/(*)
rm res/jmdb/NKMapForBlink/sub/(*)
rm res/jmdb/NKMapForBlink2/(*)
rm res/jmdb/NKMapForBlink2/sub/(*)

# edit build.xml to change <arg value='KNNR_1' /> for arguments of jmdb.buildIndexForKeyword

# 
# to add a new keyword into index, insert into KNS_SEL table in MySQL
#    insert into table KNS_SEL
#         select * from KNS where keyword in ("...");
# and edit String[] selectedKeywords in src/index/jmdb/buildIndexForKeyword.java to include the keywords defined in the table KNS_SEL, 
# and then execute ant jmdb.buildIndexForKeyword

ant jmdb.buildIndexForKeyword
/*
ant jmdb.buildIndexForKeyword1
ant jmdb.buildIndexForKeyword2
ant jmdb.buildIndexForKeyword3
ant jmdb.buildIndexForKeyword4
ant jmdb.buildIndexForKeyword5
ant jmdb.buildIndexForKeyword6
*/
# set session group_concat_max_len = 39;
# select keyword, group_concat(score order by score)  from KNS where keyword = 'aba' group by keyword;

# 8. execute test queries 
# edit args(tau and q.size) in build.xml for jmdb.Exp1stat 
ant jmdb.Exp2

/*
drop table KNS_1;
drop table KNS_2;
drop table KNS_3;
drop table KNS_4;
drop table KNS_5;
drop table KNS_6;
drop table KNNR_1; 
drop table KNNR_2;
drop table KNNR_3;
drop table KNNR_4;
drop table KNNR_5;
drop table KNNR_6;
*/
the end

===========================================================================
old script!!!

create table NNR
(startNode int unsigned not null,
endNode int unsigned not null,
rel float);

# buildGraph 
