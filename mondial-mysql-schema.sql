CREATE TABLE Country
(Name VARCHAR(35) NOT NULL UNIQUE,
 Code VARCHAR(4),
 Capital VARCHAR(35),
 Province VARCHAR(35),
 Area FLOAT,
 Population INT,
 CONSTRAINT CountryKey PRIMARY KEY(Code),
 CONSTRAINT CountryArea CHECK (Area >= 0),
 CONSTRAINT CountryPop CHECK (Population >= 0));

CREATE TABLE City
(Name VARCHAR(35),
 Country VARCHAR(4),
 Province VARCHAR(35),
 Population INT,
 Longitude FLOAT,
 Latitude FLOAT,
 CONSTRAINT CityKey PRIMARY KEY (Name, Country, Province),
 CONSTRAINT CityPop CHECK (Population >= 0),
 CONSTRAINT CityLon CHECK ((Longitude >= -180) AND (Longitude <= 180)),
 CONSTRAINT CityLat CHECK ((Latitude >= -90) AND (Latitude <= 90)));

CREATE TABLE Province
(Name VARCHAR(35) NOT NULL,
 Country VARCHAR(4) NOT NULL ,
 Population INT,
 Area FLOAT,
 Capital VARCHAR(35),
 CapProv VARCHAR(35),
 CONSTRAINT PrKey PRIMARY KEY (Name, Country),
 CONSTRAINT PrPop CHECK (Population >= 0),
 CONSTRAINT PrAr CHECK (Area >= 0));

CREATE TABLE Economy
(Country VARCHAR(4),
 GDP FLOAT,
 Agriculture FLOAT,
 Service FLOAT,
 Industry FLOAT,
 Inflation FLOAT,
 CONSTRAINT EconomyKey PRIMARY KEY(Country),
 CONSTRAINT EconomyGDP CHECK (GDP >= 0));

CREATE TABLE Population
(Country VARCHAR(4),
 Population_Growth FLOAT,
 Infant_Mortality FLOAT,
 CONSTRAINT PopKey PRIMARY KEY(Country));

CREATE TABLE Politics
(Country VARCHAR(4),
 Independence DATE,
 Dependent  VARCHAR(4),
 Government VARCHAR(120),
 CONSTRAINT PoliticsKey PRIMARY KEY(Country));

CREATE TABLE Language
(Country VARCHAR(4),
 Name VARCHAR(50),
 Percentage FLOAT,
 CONSTRAINT LanguageKey PRIMARY KEY (Name, Country),
 CONSTRAINT LanguagePercent 
   CHECK ((Percentage > 0) AND (Percentage <= 100)));

CREATE TABLE Religion
(Country VARCHAR(4),
 Name VARCHAR(50),
 Percentage FLOAT,
 CONSTRAINT ReligionKey PRIMARY KEY (Name, Country),
 CONSTRAINT ReligionPercent 
   CHECK ((Percentage > 0) AND (Percentage <= 100)));

CREATE TABLE EthnicGroup
(Country VARCHAR(4),
 Name VARCHAR(50),
 Percentage FLOAT,
 CONSTRAINT EthnicKey PRIMARY KEY (Name, Country),
 CONSTRAINT EthnicPercent 
   CHECK ((Percentage > 0) AND (Percentage <= 100)));

CREATE TABLE Continent
(Name VARCHAR(20),
 Area FLOAT(10),
 CONSTRAINT ContinentKey PRIMARY KEY(Name));

CREATE TABLE borders
(Country1 VARCHAR(4),
 Country2 VARCHAR(4),
 Length FLOAT, 
 CONSTRAINT CHECK (Length > 0),
 CONSTRAINT BorderKey PRIMARY KEY (Country1,Country2));

CREATE TABLE encompasses
(Country VARCHAR(4) NOT NULL,
 Continent VARCHAR(20) NOT NULL,
 Percentage FLOAT,
 CONSTRAINT CHECK ((Percentage > 0) AND (Percentage <= 100)),
 CONSTRAINT EncompassesKey PRIMARY KEY (Country,Continent));

CREATE TABLE Organization
(Abbreviation VARCHAR(12) PRIMARY KEY,
 Name VARCHAR(80) NOT NULL,
 City VARCHAR(35) ,
 Country VARCHAR(4) , 
 Province VARCHAR(35) ,
 Established DATE,
 CONSTRAINT OrgNameUnique UNIQUE (Name));

CREATE TABLE isMember
(Country VARCHAR(4),
 Organization VARCHAR(12),
 Type VARCHAR(35) DEFAULT 'member',
 CONSTRAINT MemberKey PRIMARY KEY (Country,Organization) );


CREATE TABLE Mountain
(Name VARCHAR(35), 
 Mountains VARCHAR(35),
 Height FLOAT,
 Type VARCHAR(10),
 Longitude FLOAT,
 Latitude FLOAT,
 CONSTRAINT MountainKey PRIMARY KEY(Name),
 CONSTRAINT CHECK ((Longitude >= -180) AND (Longitude <= 180) 
              AND  (Latitude >= -90) AND (Latitude <= 90)));

CREATE TABLE Desert
(Name VARCHAR(35),
 Area FLOAT,
 Longitude FLOAT,
 Latitude FLOAT,
 CONSTRAINT DesertKey PRIMARY KEY(Name),
 CONSTRAINT DesCoord 
   CHECK ((Longitude >= -180) AND (Longitude <= 180) 
     AND  (Latitude >= -90) AND (Latitude <= 90)));

CREATE TABLE Island
(Name VARCHAR(35),
 Islands VARCHAR(35),
 Area FLOAT,
 Height FLOAT,
 Type VARCHAR(10),
 CONSTRAINT IslandKey PRIMARY KEY(Name),
 CONSTRAINT IslandAr check (Area >= 0),
 Longitude FLOAT,
 Latitude FLOAT,
 CONSTRAINT IslandCoord
   CHECK ((Longitude >= -180) AND (Longitude <= 180) 
     AND  (Latitude >= -90) AND (Latitude <= 90)));

CREATE TABLE Lake
(Name VARCHAR(35),
 Area FLOAT,
 Depth FLOAT,
 Altitude FLOAT,
 Type VARCHAR(10),
 River VARCHAR(35),
 Longitude FLOAT,
 Latitude FLOAT,
 CONSTRAINT LakeKey PRIMARY KEY(Name),
 CONSTRAINT LakeAr CHECK (Area >= 0),
 CONSTRAINT LakeDpth CHECK (Depth >= 0),
 CONSTRAINT LakeCoord
   CHECK ((Longitude >= -180) AND (Longitude <= 180) 
     AND  (Latitude >= -90) AND (Latitude <= 90)));

CREATE TABLE Sea
(Name VARCHAR(35),
 Depth FLOAT,
 CONSTRAINT SeaKey PRIMARY KEY(Name),
 CONSTRAINT SeaDepth CHECK (Depth >= 0));

CREATE TABLE River
(Name VARCHAR(35),
 River VARCHAR(35),
 Lake VARCHAR(35),
 Sea VARCHAR(35),
 Length FLOAT,
 SourceLongitude FLOAT,
 SourceLatitude FLOAT,
 Mountains VARCHAR(35),
 SourceAltitude FLOAT,
 EstuaryLongitude FLOAT,
 EstuaryLatitude FLOAT,
 CONSTRAINT RiverKey PRIMARY KEY(Name),
 CONSTRAINT RiverLength CHECK (Length >= 0),
 CONSTRAINT SourceCoord
     CHECK ((SourceLongitude >= -180) AND 
            (SourceLongitude <= 180) AND
            (SourceLatitude >= -90) AND
            (SourceLatitude <= 90)),
 CONSTRAINT EstCoord
     CHECK ((EstuaryLongitude >= -180) AND 
            (EstuaryLongitude <= 180) AND
            (EstuaryLatitude >= -90) AND
            (EstuaryLatitude <= 90)));

CREATE TABLE geo_Mountain
(Mountain VARCHAR(35) ,
 Country VARCHAR(4) ,
 Province VARCHAR(35) ,
 CONSTRAINT GMountainKey PRIMARY KEY (Province,Country,Mountain) );

CREATE TABLE geo_Desert
(Desert VARCHAR(35) ,
 Country VARCHAR(4) ,
 Province VARCHAR(35) ,
 CONSTRAINT GDesertKey PRIMARY KEY (Province, Country, Desert) );

CREATE TABLE geo_Island
(Island VARCHAR(35) , 
 Country VARCHAR(4) ,
 Province VARCHAR(35) ,
 CONSTRAINT GIslandKey PRIMARY KEY (Province, Country, Island) );

CREATE TABLE geo_River
(River VARCHAR(35) , 
 Country VARCHAR(4) ,
 Province VARCHAR(35) ,
 CONSTRAINT GRiverKey PRIMARY KEY (Province ,Country, River) );

CREATE TABLE geo_Sea
(Sea VARCHAR(35) ,
 Country VARCHAR(4)  ,
 Province VARCHAR(35) ,
 CONSTRAINT GSeaKey PRIMARY KEY (Province, Country, Sea) );

CREATE TABLE geo_Lake
(Lake VARCHAR(35) ,
 Country VARCHAR(4) ,
 Province VARCHAR(35) ,
 CONSTRAINT GLakeKey PRIMARY KEY (Province, Country, Lake) );

CREATE TABLE geo_Source
(River VARCHAR(35) ,
 Country VARCHAR(4) ,
 Province VARCHAR(35) ,
 CONSTRAINT GSourceKey PRIMARY KEY (Province, Country, River) );

CREATE TABLE geo_Estuary
(River VARCHAR(35) ,
 Country VARCHAR(4) ,
 Province VARCHAR(35) ,
 CONSTRAINT GEstuaryKey PRIMARY KEY (Province, Country, River) );

CREATE TABLE mergesWith
(Sea1 VARCHAR(35) ,
 Sea2 VARCHAR(35) ,
 CONSTRAINT MergesWithKey PRIMARY KEY (Sea1, Sea2) );

CREATE TABLE located
(City VARCHAR(35) ,
 Province VARCHAR(35) ,
 Country VARCHAR(4) ,
 River VARCHAR(35),
 Lake VARCHAR(35),
 Sea VARCHAR(35) );

CREATE TABLE locatedOn
(City VARCHAR(35) ,
 Province VARCHAR(35) ,
 Country VARCHAR(4) ,
 Island VARCHAR(35) ,
 CONSTRAINT locatedOnKey PRIMARY KEY (City, Province, Country, Island) );

CREATE TABLE islandIn
(Island VARCHAR(35) ,
 Sea VARCHAR(35) ,
 Lake VARCHAR(35) ,
 River VARCHAR(35) );

CREATE TABLE MountainOnIsland
(Mountain VARCHAR(35),
 Island  VARCHAR(35),
 CONSTRAINT MntIslKey PRIMARY KEY (Mountain, Island) );


CREATE TABLE Country0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35) NOT NULL UNIQUE,
 Code VARCHAR(4),
 Capital VARCHAR(35),
 Province VARCHAR(35)
)
auto_increment=20000;

CREATE TABLE Country0np		# country having no province
(Id mediumint unsigned NOT NULL primary key,
 Name VARCHAR(35) NOT NULL UNIQUE,
 Code VARCHAR(4),
 Capital VARCHAR(35),
 Province VARCHAR(35));

CREATE TABLE Country0p		# country having real provinces
(Id mediumint unsigned NOT NULL primary key,
 Name VARCHAR(35) NOT NULL UNIQUE,
 Code VARCHAR(4),
 Capital VARCHAR(35),
 Province VARCHAR(35));

CREATE TABLE City0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35),
 Country VARCHAR(4),
 Province VARCHAR(35))
auto_increment=40000;

CREATE TABLE Province0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35) NOT NULL,
 Country VARCHAR(4) NOT NULL ,
 Capital VARCHAR(35),
 CapProv VARCHAR(35))
auto_increment=30000;

CREATE TABLE Province0m		# province belong to a country having real provinces
(Id mediumint unsigned NOT NULL primary key,
 Name VARCHAR(35) NOT NULL,
 Country VARCHAR(4) NOT NULL ,
 Capital VARCHAR(35),
 CapProv VARCHAR(35));

CREATE TABLE Politics0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Country VARCHAR(4),
 Government VARCHAR(120))
auto_increment=60000;

CREATE TABLE Religion0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Country VARCHAR(4),
 Name VARCHAR(50))
auto_increment=70000;

CREATE TABLE Continent0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(20))
auto_increment=10000;

CREATE TABLE Organization0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Abbreviation VARCHAR(12),
 Name VARCHAR(80) NOT NULL,
 City VARCHAR(35) ,
 Country VARCHAR(4) , 
 Province VARCHAR(35),
 text VARCHAR(93))
auto_increment=50000;

CREATE TABLE mountain0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35), 
 text VARCHAR(82))
auto_increment=0;

CREATE TABLE lake0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35),
 River VARCHAR(35),
 text VARCHAR(46))
auto_increment=80000;

CREATE TABLE Sea0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35))
auto_increment=100000;

CREATE TABLE island0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35),
 text VARCHAR(82))
auto_increment=110000;

CREATE TABLE River0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(35),
 River VARCHAR(35),
 Lake VARCHAR(35),
 Sea VARCHAR(35))
auto_increment=90000;


CREATE TABLE Desert0
(Id mediumint unsigned NOT NULL auto_increment primary key,
 Name VARCHAR(30))
auto_increment=130000;

insert into Country0 (Name, Code, Capital, Province) 
	select Name, Code, Capital, Province from Country;
commit;

insert into Country0p (id, Name, Code, Capital, Province) 
	select id, Name, Code, Capital, Province from Country0 
	where code in (select country from province group by country having count(*) > 1);

insert into Country0np (id, Name, Code, Capital, Province) 
	select id, Name, Code, Capital, Province from Country0 
	where code in (select country from province group by country having count(*) = 1);

insert into City0 (Name, Country, Province) 
	select Name, Country, Province from City;

insert into Province0 (Name, Country, Capital, CapProv) 
	select Name, Country, Capital, CapProv from Province;
commit;
insert into Province0m (id, Name, Country, Capital, CapProv) 
	select p.id, p.Name, p.Country, p.Capital, p.CapProv from Province0 p, Country0p c
	where p.Country = c.code;

insert into Politics0 (Country, Government ) 
select Country, Government from Politics;

insert into Religion0 (Country, Name ) 
select Country, Name from Religion;

insert into Continent0 (Name ) 
select Name from Continent;

insert into Organization0 (Abbreviation, Name, City, Country, Province, text) 
select Abbreviation, Name, City, Country, Province, concat(name,' ',abbreviation)
from Organization;

insert into mountain0 (name, text)
select name, concat(name,' ',ifnull(Mountains,''),' ',ifnull(type,'')) as text
from mountain; 

insert into lake0 (name, river, text)
select name, river, concat(name,' ',ifnull(type,'')) as text
from lake; 

insert into island0 (name, text)
select name, concat(name,' ',ifnull(islands,''),' ',ifnull(type,'')) as text
from island; 

insert into Sea0 (Name) 
select Name from Sea;

insert into River0 (Name, River, Lake, Sea) 
select Name, River, Lake, Sea from River;

insert into Desert0(name)
select name from Desert; 

COMMIT;

#cd project_home
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name,' country') as text from country0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id" 
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name, ' province') as text from province0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name, ' city') as text from city0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id" 
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(government, ' government') as text from politics0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO"  -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name, ' religion') as text from religion0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO"  -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name,' sea',' ocean') as text from sea0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO"  -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name,' river') as text from river0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, name as text from continent0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO"  -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name, ' organization') as text from organization0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(text,' mountain') as text from mountain0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO"  -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(text,' lake') as text from  lake0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(text,' island') as text from  island0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id"
java -cp lib/mysql-connector-java-5.1.19.jar:lib/lusql-3.6.jar ca.gnewton.lusql.core.LuSqlMain -q  "select id, concat(name,' desert') as text from  desert0" -c "jdbc:mysql://localhost/mondial?user=cspark&password=cspark1234" -l res/mondial/index-mondial -A -i "id=Index.NO:Store.YES:TermVector.NO" -map "id=id"

# 0.
cd res/mondial
rm index-mondial/(*)
rm basicIndex
rm NKMapForBlink
rm NKMapForBlink/sub

# 1. 
ant mondial.buildGraph 
# 6431 nodes, 20591 edges

# 2.
create table NMR
(startNode mediumint unsigned not null,
endNode mediumint unsigned not null,
firstNode mediumint unsigned not null,
rel float);

# 3.
mysqlimport --user='' --password='cspark1234' -p mondial --local 'res/mondial/NMR.0.1000000' --fields-terminated-by=',' 
create index ind_nmr_endNode on NMR(endNode);

# 2005536 records inserted in NMR

# 4. 
drop table KNS;
drop table KNNR;
ant mondial.buildIndex

# 16437 records in KNS (5596 distinct keywords);
# 5199262 records in KNNR

# index builder¿¡¼­ data load ÈÄ ÀÎµ¦½º »ý¼º (»ý·«)
#create index ind_knnr_keyword on KNNR(keyword);
#create index ind_knnr_destNode on KNNR(destNode);
#create index ind_knnr_rel on knnr(rel);

# 5.
ant mondial.Exp1stat
