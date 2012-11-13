/*
 
IGNACIO PEREZ
Assignment #2



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

/**
 * Shows the specified user's friends.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class GetFriendsStatuses {
    /**
     * Usage: java twitter4j.examples.user.GetFriendsStatuses [status id]
     *
     * @param args message
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(
                    "Usage: java twitter4j.examples.user.GetFriendsStatuses [status id]");
            System.exit(-1);
        }
        System.out.println("Showing @" + args[0] + "'s friends.");
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            long cursor = -1;
	    
	    IDs ids;
            System.out.println("Listing following ids.");
	    do {
             	if (0 < args.length) {
                    ids = twitter.getFriendsIDs(args[0], cursor);
                } else {
                    ids = twitter.getFriendsIDs(cursor);
                }
                for (long id : ids.getIDs()) {
                	System.out.println(id);
		 	User user = twitter.showUser(id);
            		if (user.getStatus() != null) {
               		System.out.println("@" + user.getScreenName() + " - " + user.getStatus().getText());
            		} else {
                		// the user is protected
                		System.out.println("@" + user.getScreenName());
            		}
			
                }
            } while ((cursor = ids.getNextCursor()) != 0);
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to show friends: " + te.getMessage());
            System.exit(-1);
        }
    }
}



