/*


IGNACIO PEREZ
Assignment # 2



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


import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.IDs;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.*;


/**
 * Shows the specified user's friends.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class GetFriends {
    /**
     * Usage: java twitter4j.examples.user.GetFriendsStatuses [status id]
     *
     * @param args message
     */
    public static void main(String[] args) {
	


        int discarded=0;
        int processed=0;

	if (args.length < 1) {
            System.out.println(
                    "Usage: java getFriends [status id]");
            System.exit(-1);
        }
	
	//three modes: 
	//download a friend list from a user id
	//download a list from user id
	//download a list from a file with ids

	String mode = "";
	if (args[0].matches("-loadNodes"))
	{
	System.out.println("Loading Nodes from File: " + args[1]);
	mode = "LoadNodes";	
	}
	
	if (args[0].matches("-getFriendsFromUserId"))
        {
        System.out.println("Print friends of userid " +  args[1]);
	mode = "PrintFromUserId";
        }

	if (args[0].matches("-getFriendsFromList"))
        {
        System.out.println("Print friends from list " + args[1]);
	mode  = "PrintFromFriendList";
	}


        //System.out.println("Showing @" + args[0] + "'s friends.");
        try 	{
		
		Twitter twitter = new TwitterFactory().getInstance();
		DirectedGraph<String,DefaultEdge> g =
            		new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        	if (mode == "PrintFromUserId"){
                	User user = twitter.showUser(args[1]);
                	String userInitialId = Long.toString(user.getId());
                	System.out.println("First Level: " + args[1] + " user id: " +userInitialId );
			List<String> firstLevel =  getAllFriendsIds(userInitialId,twitter);
                	int numberOfFriends = firstLevel.size();
                	Iterator itFirstLevel=firstLevel.iterator();
                	int i = 0;
                	while(itFirstLevel.hasNext())
				{
				String idFriend = (String)itFirstLevel.next();
                        	System.out.println(userInitialId+","+idFriend);	
				}
			}
		if(mode == "PrintFromFriendList" ){
			FileInputStream fstream = new FileInputStream(args[1]);
        	        DataInputStream in = new DataInputStream(fstream);
               		BufferedReader br = new BufferedReader(new InputStreamReader(in));
                	String strLine;
        	       	//Read File Line By Line
                	while ((strLine = br.readLine()) != null)   {
                		// Print the content on the console
                        	//System.out.println (strLine);
				String[] splits = strLine.split(",");
	                        String idFriend = splits[1];
				User userff = twitter.showUser(Long.valueOf(idFriend));
				if (userff.getFriendsCount()<5000 && !userff.isProtected()){
					List<String> secondLevel = getAllFriendsIds(idFriend, twitter);
                                	int j=0;
                                	Iterator itSecondLevel = secondLevel.iterator();
                                	while(itSecondLevel.hasNext())
                                        	{
                                        	String idFriendOfFriend = (String) itSecondLevel.next();
                        			System.out.println(idFriend+","+idFriendOfFriend);
						}
					}
				}
			}	
		if(mode == "LoadNodes")
			{
	  		FileInputStream fstream = new FileInputStream(args[1]);
                        // Get the object of DataInputStream
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String strLine;
                        //Read File Line By Line
                        while ((strLine = br.readLine()) != null)   {
                        	// Print the content on the console
                                //System.out.println (strLine);
                                String[] splits = strLine.split(",");
                        	g.addVertex(splits[0]);
	                        g.addVertex(splits[1]);
				g.addEdge(splits[0],splits[1]);
				}
			System.out.println(g.toString());
			}

	} catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to show friends: " + te.getMessage());
	    // System.exit(-1);
        } catch (Exception e){//Catch exception if any
  	System.err.println("Error: " + e.getMessage());
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



