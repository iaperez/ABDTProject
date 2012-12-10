/* 



 * 
 * Copyright 2007 Yusuke Yamamoto, modified  by Ignacio Perez, Seema Hari
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
import twitter4j.internal.org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TimeZone;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import java.io.InputStream;



public final class   ConversationDownload  extends StatusAdapter {


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

		DateFormat formattoGMT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  
		formattoGMT.setTimeZone(TimeZone.getTimeZone("GMT"));  
		// System.out.println("digraph mentions {");
		// I did not find a different way to count words:
		// hashtables in java allows to manage sets of data with a <key,value> structure
		// in this part of the worl, the key is the word and the value is the count.
		final DecimalFormat df = new DecimalFormat("#.###");

		System.out.println(args[0] + "-");


		String useridentifier = args[0];
		int continuesin = Integer.parseInt(args[1]);

		System.out.println(args[0] + "-");


		//String useridentifier = "CoreyHyllested";
		//int continuesin = 0;

		Twitter twitter = new TwitterFactory().getInstance();

		//User user = twitter.showUser(useridentifier);
		//String userInitialId = Long.toString(user.getId());
		//List<String> firstLevel =  getAllFriendsIds(userInitialId,twitter);

		//int numberOfFriends = firstLevel.size();
		//System.out.println("number of users: " + numberOfFriends);
		//Iterator <String> itFirstLevel=firstLevel.iterator();

		final Hashtable <String,Integer> countconversationtweets = new Hashtable <String, Integer>();
		final Hashtable <String,Integer> tweetlist = new Hashtable <String, Integer>();


		try {

			//reading the feed of the user, pre - stored.
			BufferedReader reader = new BufferedReader(new FileReader("coreysTimeline.test"));
			String line;
			while ((line = reader.readLine()) != null) {
				String [] elements =  line.split("\t");
				tweetlist.put(elements[4],1);
			}
			reader.close();

			Enumeration<String> e = tweetlist.keys();
			BufferedWriter writer = new BufferedWriter(new FileWriter("ConversationsExtraData"+continuesin+".txt", true));

			while(e.hasMoreElements())
			{
				String conversationId = e.nextElement();

				//jump in case the conversation was already reviewed...
				if (countconversationtweets.contains(conversationId)) continue;
				countconversationtweets.put(conversationId, 1);

				//Adding the kloutscore
				String convoKloutScore = Float.toString(getKloutScore(Long.parseLong(conversationId)));

				RelatedResults rel = twitter.getRelatedResults(Long.parseLong(conversationId));
				ResponseList<Status> rep = rel.getTweetsWithConversation();

				if(!rep.isEmpty())
				{
					// if the list of related results is not empty, then I will add the
					// this first tweet as the conversation reference
					Status origin = twitter.showStatus(Long.parseLong(conversationId));
					String origindate = formattoGMT.format(origin.getCreatedAt());
					//This will allow me to count the data
					countconversationtweets.put(conversationId, 1);



					HashtagEntity[] hashTagList = origin.getHashtagEntities(); 
					MediaEntity[] mediaList =origin.getMediaEntities();
					URLEntity[] urlList = origin.getURLEntities(); 
					UserMentionEntity[] userList =origin.getUserMentionEntities(); 

					//System.out.println("printing conversation detail");
					for(int j =0;hashTagList !=null && j<hashTagList.length;j++)
					{
						writer.write(origindate+"\t"+conversationId+"\thashtag\t"+hashTagList[j].getText().replaceAll("\\r|\\n|\\t", "")+"\n");
					}

					for(int j =0;mediaList !=null && j<mediaList.length;j++)
					{

						writer.write(origindate+"\t"+conversationId+"\tmedia\t"+mediaList[j].getType()+"-"+mediaList[j].getExpandedURL().toString().replaceAll("\\r|\\n|\\t", "")+"\n");
					}

					for(int j =0;urlList !=null && j<urlList.length;j++)
					{
						writer.write(origindate+"\t"+conversationId+"\turl\t"+urlList[j].getExpandedURL().toString().replaceAll("\\r|\\n|\\t", "")+"\n");
					}

					for(int j =0;userList !=null && j<userList.length;j++)
					{
						writer.write(origindate+"\t"+conversationId+"\tuser\t"+userList[j].getScreenName().replaceAll("\\r|\\n|\\t", "")+"\t"+convoKloutScore+"\n");
					}

					System.out.println(useridentifier
							+ "\t"
							+ conversationId
							+ "\t"
							+ origindate
							+ "\t"
							+ origin.getUser().getId()
							+ "\t"
							+ origin.getUser().getScreenName()
							+ "\t"
							+ origin.getId()
							+ "\t"
							+ origin.getText().toLowerCase()
							.replaceAll("[^a-zA-Z0-9\\s]", "")
							.replaceAll("\\r|\\n|\\t", "")
							.replaceAll("\\s\\s", "\\s").trim());

					//then we are interested in all the related tweets.
					for (Status r : rep){
						String tweetId = Long.toString(r.getId());
						//if the conversation was already reviewed, the conversation will ve discarded
						if (countconversationtweets.contains(tweetId)) continue;
						countconversationtweets.put(tweetId, 1);

						System.out.println(useridentifier
								+ "\t"
								+ conversationId
								+ "\t"
								+ origindate
								+ "\t"
								+ origin.getUser().getId()
								+ "\t"
								+ r.getUser().getScreenName()
								+ "\t"
								+ r.getId()
								+ "\t"
								+ r.getText().toLowerCase()
								+ "\t"
								+ convoKloutScore
								.replaceAll("[^a-zA-Z0-9\\s]", "")
								.replaceAll("\\r|\\n|\\t", "")
								.replaceAll("\\s\\s", "\\s").trim());

						hashTagList =r.getHashtagEntities(); 
						mediaList =r.getMediaEntities();
						urlList = r.getURLEntities(); 
						userList =r.getUserMentionEntities(); 

						//System.out.println("printing conversation detail");
						for(int j =0;hashTagList !=null && j<hashTagList.length;j++)
						{
							writer.write(origindate+"\t"+conversationId+"\thashtag\t"+hashTagList[j].getText().replaceAll("\\r|\\n|\\t", "")+"\n");
						}

						for(int j =0;mediaList !=null && j<mediaList.length;j++)
						{

							writer.write(origindate+"\t"+conversationId+"\tmedia\t"+mediaList[j].getType()+"-"+mediaList[j].getExpandedURL().toString().replaceAll("\\r|\\n|\\t", "")+"\n");
						}

						for(int j =0;urlList !=null && j<urlList.length;j++)
						{
							writer.write(origindate+"\t"+conversationId+"\turl\t"+urlList[j].getExpandedURL().toString().replaceAll("\\r|\\n|\\t", "")+"\n");
						}

						for(int j =0;userList !=null && j<userList.length;j++)
						{
							writer.write(origindate+"\t"+conversationId+"\tuser\t"+userList[j].getScreenName().replaceAll("\\r|\\n|\\t", "")+"\t"+convoKloutScore+"\n");
						}
					}
				}
			}
			writer.close();

		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.exit(0);

	}


	public static float getKloutScore(Long tweetid)
	{
		Twitter twitter = new TwitterFactory().getInstance();
		RelatedResults rel;
		int count = 0;
		float convoKloutScore=0;
		try {
			rel = twitter.getRelatedResults(tweetid);

			
			//For every tweet find out related tweets and then tweets with conversations
			ResponseList<Status> rep = rel.getTweetsWithConversation();		
			if (rep.isEmpty()==false){
			for (Status r:rep){

				Long id = r.getUser().getId();

				String apikey = "v4562vn3kepkm65frm2qs785";

				URL kloutidurl = new URL("http://api.klout.com/v2/identity.json/tw/" + id + "?key="+ apikey);
				HttpURLConnection huc = (HttpURLConnection) kloutidurl.openConnection();
				if (huc.getResponseCode()!=404&& (r.getUser().isProtected()==false)){
					count++;
					InputStreamReader is = new InputStreamReader(kloutidurl.openStream());
					BufferedReader rd = new BufferedReader(is);

					String line = rd.readLine();
					String jsonText = "";
					while (line!=null){
						jsonText += line;
						line = rd.readLine();

					}

					JSONObject json = new JSONObject(jsonText.trim());
					URL kloutscoreurl = new URL("http://api.klout.com/v2/user.json/"+json.get("id") +"/score?key="+apikey);

					// calculate klout score
					InputStream is2 = kloutscoreurl.openStream();
					BufferedReader scorerd = new BufferedReader(new InputStreamReader(is2, Charset.forName("UTF-8")));
					line = null;
					line = scorerd.readLine();
					jsonText = "";

					while (line!=null){

						jsonText += line;
						line = scorerd.readLine();

					}
					JSONObject kloutscore = new JSONObject(jsonText.trim());	
					convoKloutScore += (float) kloutscore.getDouble("score");
					System.out.println("ConvoScore" + convoKloutScore);
				}
				


			}System.out.println("ConvoScorefinal :"+ (convoKloutScore)/count);
			return (convoKloutScore)/count;
			}
			else {
				return 0;
			}
		} catch (TwitterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return convoKloutScore;
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			return convoKloutScore;
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
