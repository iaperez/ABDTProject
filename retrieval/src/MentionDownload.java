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
public final class MentionDownload  extends StatusAdapter {
    

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
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	
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
	StatusListener listener = new StatusListener() {
	   public void onStatus(Status status) {
		long actualTime = System.currentTimeMillis();
		String text = status.getText().toLowerCase();
		String wordsPerText[] = splitPattern.split(text);
		
		// to avoid problems with the counting process, this work use synchronized
		synchronized (this) {
			stats.incrementTweetCount();
			long tweetCount =  stats.getTweetCount();
			long timeElapsed = (actualTime- startTime)/1000;
			long statET =  stats.getElapsedTime();
			long mentionCount;	

			//with this code, I show the stats every 15 seconds, comparing the change of time
	
			List<String> names;
    			Extractor extractor = new Extractor();
    			
			names = extractor.extractMentionedScreennames(status.getText());
    			mentionCount = stats.getMentionCount();
			
                        String userName = status.getUser().getName().toLowerCase();
			
			//I had some problems with international names and the graphic tools, so i decided
			//To filer all the names with non ascii characters.	
			Matcher matcherUserName =  nameFilter.matcher(userName);
			if (matcherUserName.find() && names.size()>0)
			{	
				//I'm countiong the act of mention one or more tweeters as one action.
				stats.incrementMentionCount();
				for (String name : names) 
					{
					String mentionName = name.toLowerCase();
	                        	Matcher matcherMention =  nameFilter.matcher(mentionName);
					if(matcherMention.find()){
						//userName = userName.replaceAll("\\u202E|\\u200E|\\t|\\r|\\n", "").trim();
						//mentionName = mentionName.replaceAll("\\u202E|\\u200E|\\t|\\r|\\n", "").trim();
						//Are there other cleanings on names???
						System.out.println("\"" + userName + "\",\"" + mentionName + "\"");
    					}
				}
			}

			if(mentionCount > 30000 &&  timeElapsed>0 && statET!=timeElapsed && timeElapsed%15==0)
				{
				//System.out.println("}");
				stats.setElapsedTime(timeElapsed);
				String report = "";
				report += " |Date: " +startDate;
				report += " |Tweet Count: " + tweetCount ;
                        	report += " |Mention Cpunt: " + mentionCount;
				report += " |Elapsed Time (s): " +  timeElapsed;
                        	report += " |Avg. (Tweets per Second): " + (double)(tweetCount/timeElapsed) ;
				System.out.println(report);
					
				System.exit(0);
				}
			
			}
		}

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
    }
}
