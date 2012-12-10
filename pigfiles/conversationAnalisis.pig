REGISTER ./tutorial.jar;                                                    
basemodel = LOAD '../data/basemodel.txt' USING PigStorage('\t') AS (ngram,topic,probability);
targetset = LOAD '../data/coreysConversations20120605.txt' USING PigStorage('\t') AS (username,id,date,idfollowed,nameusertweet,tweetid,text);
stopwords =  LOAD '../data/stopwords.txt'	USING PigStorage('\t') AS (word:chararray);

-- generate a list with all the intended topics
topiclist =  DISTINCT (FOREACH basemodel GENERATE topic);

targetclean = FOREACH targetset 
	GENERATE
		id as bmid, 
		date as bdate,
		org.apache.pig.tutorial.ToLower(text) AS bmtext;

targetcleanngram = FOREACH targetclean 
	GENERATE
	 bmid as id,
	 bmtext as text,
	 FLATTEN(org.apache.pig.tutorial.NGramGenerator(bmtext)) as ngrams;
	 

crossTS = CROSS targetcleanngram,topiclist;
describe crossTS;

-- to compare with the basemodel probailityes I need id, ngram and tag
idngramtopic = DISTINCT(FOREACH(GROUP crossTS by (targetcleanngram::id, targetcleanngram::ngrams,topiclist::topic))
	GENERATE
		FLATTEN(group));


targetdataset = FOREACH idngramtopic
	GENERATE
		$0 as id,
		$1 as ngram,
		$2 as topic;

probilitiesPerNgram = JOIN targetdataset by (ngram,topic) left,basemodel by (ngram,topic);

fixed = DISTINCT(FOREACH probilitiesPerNgram GENERATE 
	targetdataset::id AS id,
	targetdataset::ngram AS ngram,
	targetdataset::topic AS topic,
	(basemodel::probability IS NOT NULL ? LOG((double)basemodel::probability):LOG((double)0.4)) AS logprobability,
	(basemodel::probability IS NOT NULL ? LOG(1.0-(double)basemodel::probability):LOG((double)0.6)) AS lognegprobability); 

gg = GROUP fixed BY (id,topic);

probabilities = FOREACH gg
	GENERATE 
		group as key,
		1.0/(1.0+EXP(SUM(fixed.lognegprobability) - SUM(fixed.logprobability))) as prob;
		
results = FOREACH probabilities GENERATE flatten(key),prob;
resultsclean = FOREACH results GENERATE 
	$0 as id, $1 as topic, $2 as prob;

final =  JOIN results by id,targetclean by bmid;

STORE  resultsclean INTO '../data/ConversationAnalysisResults' USING PigStorage ('\t');
dump final;
dump resultsclean;
describe final;





