REGISTER ./tutorial.jar;                                                    
baseTweets = LOAD '../data/alltweets' USING PigStorage('\t') AS (btid,bttags,bttext);
stopwords =  LOAD '../data/stopwords.txt'	USING PigStorage('\t') AS (word:chararray);

----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
clean1 = FOREACH baseTweets 
	GENERATE 
		btid AS id, 
		org.apache.pig.tutorial.ToLower(bttags) AS tag, 
		org.apache.pig.tutorial.ToLower(bttext) AS text;
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
-- counting all the tweet in the model...  is there a better way to do this??? 
justid = FOREACH clean1 
	GENERATE 
		id;

djustId = DISTINCT justid;

totalofTweetsBaseModel = FOREACH (GROUP djustId ALL) 
	GENERATE 
		COUNT(djustId) AS total;
DUMP totalofTweetsBaseModel;

----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
-- counting the number of tweets per tag
tagsAndId = FOREACH clean1 
	GENERATE 
		FLATTEN(TOKENIZE(tag)) as tags, 
		id;
tagcounting = 	FOREACH (DISTINCT(GROUP tagsAndId by tags)) 
	GENERATE 
		group as tag,
		COUNT(tagsAndId) as totaltag,
		((LONG)totalofTweetsBaseModel.total - COUNT(tagsAndId)) as totalnegative,
		(LONG)totalofTweetsBaseModel.total as tweetstotal;
DESCRIBE tagcounting;
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
-- counting the number of times in which an Ngram is associated with a particular tag 
clean2 = FOREACH clean1  
	GENERATE
		id, 
		FLATTEN(TOKENIZE(tag)) AS tags, 
		FLATTEN(org.apache.pig.tutorial.NGramGenerator(text)) AS ngram ;

ng = FOREACH (GROUP clean2 by (ngram,tags)) 
	GENERATE 
		group, 
		COUNT(clean2) as ct; 
ngramTagCounting =  FOREACH ng 
	GENERATE 
		FLATTEN(group), 
		ct;
DESCRIBE ngramTagCounting;
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
-- counting the number of tweets that contain a particular ngram
clean3 = FOREACH clean1 
	GENERATE 
		id,
		FLATTEN(org.apache.pig.tutorial.NGramGenerator(text)) AS ngram;
	
ngramTweetCounting = FOREACH (DISTINCT (GROUP clean3 by ngram)) 
	GENERATE 
		group as ng, 
		COUNT(clean3) as ct; 
DESCRIBE ngramTweetCounting;
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------

-- counting datasets
-- tagcounting: {tag: chararray,totaltag: long,totalnegative: long,tweetstotal: long}
-- ngramTagCounting: {group::ngram: chararray,group::tags: chararray,ct: long}
-- ngramTweetCounting: {ng: chararray,ct: long}

-- temp: {	
--	tagcounting::tag: chararray,
--	tagcounting::totaltag: long,
--	tagcounting::totalnegative: long,
--	tagcounting::tweetstotal: long,
--	ngramTagCounting::group::ngram: chararray,
--	ngramTagCounting::group::tags: chararray,
--	ngramTagCounting::ct: long}

temp = JOIN tagcounting by tag, ngramTagCounting by group::tags;

-- temp2: {
--	temp::tagcounting::tag: chararray,
--	temp::tagcounting::totaltag: long,
--	temp::tagcounting::totalnegative: long,
--	temp::tagcounting::tweetstotal: long,
--	temp::ngramTagCounting::group::ngram: chararray,
--	temp::ngramTagCounting::group::tags: chararray,
--	temp::ngramTagCounting::ct: long,
--	ngramTweetCounting::ng: chararray,
--	ngramTweetCounting::ct: long}

temp2 = JOIN 
	temp by ngramTagCounting::group::ngram, 
	ngramTweetCounting by ng;
describe temp2;

-- cleaning names
basemodeldata = FOREACH temp2 
	GENERATE  
		temp::tagcounting::tag as tc_tag,
		temp::tagcounting::totaltag as tc_totaltag,
		temp::tagcounting::totalnegative as tc_totalnegative,
		temp::tagcounting::tweetstotal as tc_tweetstotal,
		temp::ngramTagCounting::group::ngram as ngramTC_ngram,
		temp::ngramTagCounting::group::tags as ngramTC_tag,
		temp::ngramTagCounting::ct as ngramTC_count,
		ngramTweetCounting::ng as ngTweetCounting,
		ngramTweetCounting::ct as ngram_total_per_tweet;

bm = FOREACH basemodeldata 
	GENERATE  
		ngramTC_ngram as ngram,
		tc_tag as tag,
		((double)ngramTC_count/(double)tc_totaltag)/(((double)ngramTC_count/(double)tc_totaltag) + ((double)(ngram_total_per_tweet-ngramTC_count)/((double)tc_totalnegative))) as probability;

basemodel =  DISTINCT bm;	

describe basemodel;
STORE  basemodel INTO '../data/basemodel' USING PigStorage ('\t');





                                                        



