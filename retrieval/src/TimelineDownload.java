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


import twitter4j.*;
import java.sql.Timestamp;
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
import java.util.TimeZone;

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
public final class   TimelineDownload  extends StatusAdapter {


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

		System.out.println("why this is empty!!! -"+ args[0] + "-");
		
		
		String useridentifier = args[0];
		int continuesin = Integer.parseInt(args[1]); 
		Twitter twitter = new TwitterFactory().getInstance();

		User user = twitter.showUser(useridentifier);
		String userInitialId = Long.toString(user.getId());
		List<String> firstLevel =  getAllFriendsIds(userInitialId,twitter);
        
        int numberOfFriends = firstLevel.size();
        System.out.println("number of users: " + numberOfFriends);
        Iterator <String> itFirstLevel=firstLevel.iterator();
        
        DateFormat formattoGMT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  
        formattoGMT.setTimeZone(TimeZone.getTimeZone("GMT"));  
        Date date = new Date();  
        System.out.println(formattoGMT.format(date).toString());  
        
        try { 
        	int i = 1;
        	while(itFirstLevel.hasNext())
        	{	
        		if(i>=continuesin) 
        		{	
        			String idFriend = (String)itFirstLevel.next();
        			User friendUser = twitter.showUser(Long.parseLong(idFriend));
        			if(!friendUser.isProtected())
        			{
        				//System.out.println(userInitialId+"->"+idFriend);
        				Paging paging = new Paging(1, 100);
        				List<Status> statuses = twitter.getUserTimeline(friendUser.getId(),paging);
        				//System.out.println(statuses.size());
        				for (Status st : statuses)
        				{
        					
        					String rtcount = "0"+"\t"+"0"+"\t";
        					if(st.isRetweet())
        					{
        						rtcount =st.getRetweetCount()+"\t"+st.getRetweetedStatus().getRetweetCount() +"\t";
        					}
        					
          					System.out.println(
        							i+"\t"+
        							userInitialId+"\t"+
        							st.getUser().getId()+"\t"+
        							st.getUser().getScreenName().replaceAll("\\r|\\n|\\t", "")+"\t"+
        							formattoGMT.format(st.getCreatedAt())+"\t"+
        							st.getId()+"\t"+
        							st.getInReplyToScreenName()+"\t"+
        							st.getInReplyToStatusId()+"\t"+
        							st.getInReplyToUserId()+"\t"+
        							rtcount+
        							st.getText().toLowerCase().
        								replaceAll("[^a-zA-Z0-9\\s]", "").
        								replaceAll("\\r|\\n|\\t", "").
        								replaceAll("\\s\\s", "\\s").trim());                	
        				}
        			}
        		}
        		i++;	
        	}
        }
        catch (Exception e)
        {
        	System.out.println(e.toString());
        }
			
	}

	public static List<String> getAllFriendsIds(String idInitial, Twitter twitter){
		List<String> Ids = new ArrayList<String>();
		//System.out.println("Downloading @" + idInitial + "'s friends.");
		try {
			long cursor = -1;

			IDs ids;
			//System.out.println("Listing following ids.");
			do {
				if (0 < idInitial.length()) {
					ids = twitter.getFriendsIDs(Long.valueOf(idInitial), cursor);
				} else {
					ids = twitter.getFriendsIDs(cursor);
				}
				for (long id : ids.getIDs()) {
					//System.out.println(id);
					Ids.add(Long.toString(id));
				}
			} while ((cursor = ids.getNextCursor()) != 0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to show friends: " + te.getMessage());
			//System.exit(-1);
		}
		return Ids;
	}


}
