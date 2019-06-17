import com.google.gson.JsonObject;
import twitter4j.*;

import java.text.SimpleDateFormat;
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

                for(Status status : statuses) {
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

    public int getMentionsCount(int numDays) {
        try {
                        Date date = new Date();
            date.setDate(date.getDate()-numDays);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            String dateString = format.format(date);
//            System.out.println();
            Query query = new Query(user.getScreenName());
            query.setSince(dateString);

            QueryResult result = twitter.search(query);

            return result.getTweets().size();
//        } catch (TwitterException te) {
//            return -1;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Caught");
            return -2;
        } catch (TwitterException e) {
            e.printStackTrace();

            return -1;
        }
    }
}

