/* 

Ignacio Perez
Assignment #3


 * 
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import java.util.concurrent.locks.Lock;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.twitter.Extractor;

/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * * @author Ignacio Perez, based on the work of Yusuke Yamamoto
 * * @author Yusuke Yamamoto - yusuke at mac.com
 *
 */
public final class  UsersMentionDownload  extends StatusAdapter {


	// auxilar class, to manage the tweet stats		
	private static class TweetStats{
		private long TweetCount=0;
		private long ElapsedTime = 0;
		private long MentionCount = 0;
		public long incrementTweetCount()
		{
			TweetCount++;
			return TweetCount;
		}

		public long getTweetCount()
		{
			return TweetCount;
		}

		public long incrementMentionCount()
		{
			MentionCount++;
			return MentionCount;
		}

		public long getMentionCount()
		{
			return MentionCount;
		}


		public void setElapsedTime(long nwTime)
		{
			ElapsedTime =  nwTime;	
		}

		public long getElapsedTime()
		{
			return ElapsedTime;
		}
	}

	// I'm using collections in this work, this class is to order those collections.
	static class myComparator implements Comparator {
		public int compare(Object obj1, Object obj2) {
			int result = 0;
			Map.Entry e1 = (Map.Entry) obj1;
			Map.Entry e2 = (Map.Entry) obj2;
			int value1 = (Integer) e1.getValue();
			int value2 = (Integer) e2.getValue();
			result = value2-value1;
			return result;
		}
	}




	/*

	 * Main entry of this application.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws TwitterException {

		final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final String startDate =  formatter.format(new Timestamp((new Date()).getTime()));
		final TweetStats stats = new TweetStats();	
		final long startTime =  System.currentTimeMillis();
		// to split the tweet text into words
		final Pattern splitPattern =  Pattern.compile("\\b");
		final Pattern nameFilter = Pattern.compile("^[a-zA-Z0-9_]+$");

		// System.out.println("digraph mentions {");
		// I did not find a different way to count words:
		// hashtables in java allows to manage sets of data with a <key,value> structure
		// in this part of the worl, the key is the word and the value is the count.
		final Hashtable <String,Integer> wordstats = new Hashtable <String, Integer>();
		final DecimalFormat df = new DecimalFormat("#.###");

		String subtopic = args[1];
		String topic = args[0];
		String finalquery =subtopic +  " lang:en geocode:39.8,-95.583068847656,2500km";
		//finalquery ="* from:ignacioperez";
		System.out.println(topic + " "+ finalquery);
		Twitter twitter = new TwitterFactory().getInstance();
		Query query = new Query(finalquery);
		query.rpp(200);
		query.setPage(1);
		QueryResult result = twitter.search(query);
		boolean cont = true;
		try {
		while (cont)  
		{
			for (Tweet tweet : result.getTweets()) {
				System.out.println(tweet.getId() + "\t"+ topic+ "\t" + tweet.getText().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\r|\\n|\\t", "").replaceAll("\\s\\s", "\\s").trim());
			}
			int page = query.getPage();
			//System.out.println("PAGINA "+ page);
			query.setPage(page+1);
			result = twitter.search(query);
		}
		}
		catch (Exception e)
		{
			// Dont do nothing if exception is launchend....
		}

	}
}
