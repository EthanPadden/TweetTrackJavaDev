import com.google.gson.*;
import com.mongodb.*;
import twitter4j.*;
import java.util.*;

import java.util.List;

public class Updater {
    private DBCollection stats;
    private DBCollection tweets;
    private String trackerId;
    private static final int DELAY = 900000;

    public Updater(DBCollection stats, DBCollection tweets, String trackerId) {
        this.stats = stats;
        this.tweets = tweets;
        this.trackerId = trackerId;
    }

    public void updateMentions() {
        System.out.println("Updating mentions at " + new Date());
        // Does not update last_updated
        BasicDBObject newDocument =
                new BasicDBObject().append("$inc",
                        new BasicDBObject().append("mentions_count", 1));

        stats.update(new BasicDBObject().append("tracker_id", trackerId), newDocument);
    }

    public void updateTweetStats() {
        System.out.println("Updating tweet stats at " + new Date());
        // Search for tweets in DB from this tracker
        BasicDBObject inQuery = new BasicDBObject();
        inQuery.put("tracker_id", trackerId);

        List<DBObject> statuses = tweets.find(inQuery).toArray();
        System.out.println("Found " + statuses.size() + " tweets");
        JsonParser jsonParser = new JsonParser();
        Twitter twitter = new TwitterFactory().getInstance();

        updateBatch(statuses, 0, twitter);
    }

    private void updateBatch(List<DBObject> statuses, int x, Twitter twitter) {
        for(int i = x; i < x+50; i++) {
            if(i >= statuses.size()) {
                try {
                    Thread.sleep(DELAY);
                    updateTweetStats();
                }
                catch (Exception e){
                    System.err.println(e);
                }
                break;
            } else {
                long id = (long) statuses.get(i).get("tweet_id");
                try {
                    Status status = twitter.showStatus(id);
                    BasicDBObject likesDoc = new BasicDBObject();
                    likesDoc.append("$set", new BasicDBObject().append("favourite_count", status.getFavoriteCount()));

                    BasicDBObject searchQuery = new BasicDBObject().append("tweet_id", id);
                    DBObject currentDBObj = tweets.findOne(searchQuery);
                    int currentLikes = (int) currentDBObj.get("favourite_count");
                    int newLikes = status.getFavoriteCount();
                    int difference = newLikes - currentLikes;

                    BasicDBObject newDocument =
                            new BasicDBObject().append("$inc",
                                    new BasicDBObject().append("likes_count", difference));

                    stats.update(new BasicDBObject().append("tracker_id", trackerId), newDocument);
                    tweets.update(searchQuery, likesDoc);



                } catch (TwitterException e) {
                    System.out.println("Tweet with id " + id + " no longer exists");
                }
            }
        }

        try {
            Thread.sleep(DELAY);
            updateBatch(statuses, x + 50, twitter);
        }
        catch (Exception e){
            System.err.println(e);
        }
    }


}
