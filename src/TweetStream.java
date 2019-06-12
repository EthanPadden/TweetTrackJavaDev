import com.google.gson.JsonObject;
import twitter4j.*;

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
}

