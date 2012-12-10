-- ################################################################################################################
REGISTER ./tutorial.jar;                                                                                             
raw = LOAD 'excite-small.log' USING PigStorage('\t') AS (user, time, query);                                         
citynames = LOAD 'dataen.txt' USING PigStorage('\t');
clean1 = FILTER raw BY query MATCHES '(.*)\\w+(.*)';                                                                 

-- a) Find queries with references to U.S. zip codes	
zipcodereferences = FILTER clean1 by query MATCHES '(.*)\\d{5}(-\\d{4})?(.*)';

-- b) Find queries with references to place names
-- The basic idea to solve this problem is to construct a cross relation between the queries and 
-- and the citi names, and then try to find string by string if a city name is in a query. 
-- It is not efficient, but works.

prunedcitynames = FOREACH citynames GENERATE org.apache.pig.tutorial.ToLower($1) as city;                            
clean2 = FOREACH clean1 GENERATE user, time, org.apache.pig.tutorial.ToLower(query) as qr;                           
fulldata = CROSS clean2, prunedcitynames ;                                                                           
describe fulldata;                                                                                                   
	fulldata: {clean2::user: bytearray,clean2::time: bytearray,clean2::qr: chararray,prunedcitynames::city: chararray}
searchcity = FOREACH fulldata GENERATE user, time, qr, city, STRSPLIT(qr,city,1) as cityinquery;                         
cityreferences = FILTER searchcity BY cityinquery != null;   
