import com.google.gson.JsonObject;
import twitter4j.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TweetStream extends TwitterEntity{
    private User user;

    public TweetStream() {
        twitter = new TwitterFactory().getInstance();
    }

    public TweetStream(String userName) {
        this();
        this.userName = userName;
        Account account = new Account(userName);
        // May be null
        user = account.verifyAccount();
    }

    public List<Status> getTweets(int numTweets) {
        if(user == null) return null;
        else {
            String handle = user.getScreenName();
            Paging paging = new Paging(1,numTweets);
            try {
                List<Status> statuses = twitter.getUserTimeline(handle, paging);
                return statuses;
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                return null;
            }
        }
    }

    public List<Status> getTweetsByTime(int numDays) {
        if(user == null) return null;
        else {
            String handle = user.getScreenName();

            // Note - more paging for more than 200 tweets
            Paging paging = new Paging(1,200);

            try {
                List<Status> statuses = twitter.getUserTimeline(handle,paging);
                if(statuses.size() <= 1) return null;

                ArrayList<Status> output = new ArrayList<Status>();
                Date date = new Date();
                date.setDate(date.getDate()-numDays);
                System.out.println(date.toString());

                for(Status status : statuses) {
                    System.out.println(status.getCreatedAt());

                    if(status.getCreatedAt().compareTo(date) > 0){
                        output.add(status);
                    }
                    else {
                        break;
                    }
                }

                return output;
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                return null;
            }
        }
    }
}

