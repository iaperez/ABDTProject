-- to facilitate the cleaning, I used the tutorial UDFs
-- I looked in google for examples of regular expressions
-- this are the urls:
-- http://en.wikipedia.org/wiki/Pig_%28programming_language%29
-- http://sanjaal.com/java/tag/java-utility-regular-expression-us-zip-code-validation-regular-expression/
-- At the end, I just choose to use a basic regexp for the zip codes.

REGISTER ./tutorial.jar;                                                    
raw = LOAD 'excite-small.log' USING PigStorage('\t') AS (user, time, query);
clean1 = FILTER raw BY query MATCHES '(.*)\\w+(.*)';     
clean2 = FOREACH clean1 GENERATE user, time, org.apache.pig.tutorial.ToLower(query) as query;

-- a) The total number of query records
biggroup = GROUP clean1 ALL;                                                
totalqueries =  FOREACH biggroup GENERATE COUNT(clean1) as total;                       
dump totalqueries;

-- b) The maximum query length in words 
querylist = FOREACH clean2 GENERATE org.apache.pig.tutorial.ToLower(query) as query;
querylistfinal = DISTINCT querylist;                                                                              
querylistsplitted =  FOREACH querylistfinal GENERATE query, TOKENIZE(query) as splitted;
querycount = FOREACH querylistsplitted GENERATE  COUNT(splitted) as numberofwords;
querycountordered = ORDER querycount BY numberofwords DESC;
max = LIMIT querycountordered 1;
dump max;
                
-- c) The average query length in words
allquerycounts = GROUP querycount ALL;
avgcount = FOREACH allquerycounts GENERATE AVG(querycount.numberofwords);
dump avgcount;

-- d) The total number of unique users
userslist = FOREACH clean1 GENERATE user;
uniqueuserlist = DISTINCT userslist;
allusers = GROUP uniqueuserlist ALL;
totaluniqueuser = FOREACH allusers GENERATE COUNT(uniqueuserlist.user);
dump totaluniqueuser;

-- e) The average number of query records per user
userqueries =  FOREACH clean2 GENERATE user,query;
queriesperuser = GROUP userqueries BY user;
countqueriesperuser = FOREACH queriesperuser GENERATE FLATTEN($0) as user, COUNT($1) as count;
allcountqueriesperuser = GROUP countqueriesperuser ALL;
avgqueriesperuser =  FOREACH allcountqueriesperuser GENERATE AVG(countqueriesperuser.count);
dump avgqueriesperuser;

-- f) What percent of query records contain queries with Boolean operators (AND, OR, NOT, or +)                       
qlextended = FOREACH clean1 GENERATE user, time, query, FLATTEN(TOKENIZE(query)) as word;               
qlfilter = FILTER qlextended BY word == 'AND' OR word == 'OR' or word == 'NOT' or word MATCHES '(.*)\\+(.*)';
qllist = FOREACH qlfilter GENERATE user, time, query;
qlunique = DISTINCT qllist;                                                                             
ql5 = GROUP qlunique ALL;                             
percentageOperators = FOREACH ql5 GENERATE ( COUNT(qlunique)/(double) totalqueries.total)*100;                                               
dump percentageOperators;

-- g) The 10 longest distinct queries in words
querylist = FOREACH clean2 GENERATE org.apache.pig.tutorial.ToLower(query) as query;
querylistfinal = DISTINCT querylist;                                                                              
querylistsplitted =  FOREACH querylistfinal GENERATE query, TOKENIZE(query) as splitted;
querycount = FOREACH querylistsplitted GENERATE query, COUNT(splitted) as numberofwords;
querycountordered = ORDER querycount BY numberofwords DESC;
toptenlongestqueries = LIMIT querycountordered 10;
dump toptenlongestqueries;


-- h) The 10 most frequently occurring queries
qlfreq= group clean1 by query;
qlfreqcount =  FOREACH qlfreq GENERATE $0, COUNT($1) as number; 
qlfreqcountorder = ORDER qlfreqcount BY number DESC;
toptenfreq =  LIMIT qlfreqcountorder 10; 
dump toptenfreq; 


                                                        



