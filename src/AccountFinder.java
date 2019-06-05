import com.google.gson.JsonObject;
import twitter4j.*;
import java.util.*;

public class AccountFinder {
    public static void main(String[] args) {
        twitter4j.Twitter twitter = new TwitterFactory().getInstance();
        JsonObject output = new JsonObject();
        try {
            User account = twitter.showUser(args[0]);
            String name = account.getName();
            String handle = account.getScreenName();
            int tweetCount = account.getStatusesCount();
            int followersCount = account.getFollowersCount();

            output.addProperty("name", name);
            output.addProperty("handle", handle);
            output.addProperty("tweetCount", tweetCount);
            output.addProperty("followersCount", followersCount);

            System.out.println(output.toString());
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        }
    }
}

