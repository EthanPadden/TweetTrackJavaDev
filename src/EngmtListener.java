import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.*;
import twitter4j.*;

import java.io.FileReader;

public class EngmtListener {
    private User user;
    private TwitterStream tweetStream;
    private FileReader fileReader;
    private DB db;
    private DBCollection trackers;
    private DBCollection hashtags;
    private String trackerId;
    private JsonParser jsonParser;

    public EngmtListener(User user, DB db, String trackerId) {
        this.user = user;
        this.db = db;
        this.trackerId = trackerId;
        jsonParser = new JsonParser();
        tweetStream = new TwitterStreamFactory().getInstance();
        hashtags = db.getCollection("hashtags");
    }

    public void listen() {
        {
            StatusListener tweetsListener = new StatusListener() {
                @Override
                public void onStatus(Status status) {
                    System.out.println("Status: " + status.getText());
                    if(status.getUser().getScreenName().compareTo(user.getScreenName()) != 0){
                            writeToDb(status);
                            System.out.println("HASHTAG: " + status.getUser().getScreenName());
                            System.out.println("Text: " + status.getText());
                    }
                }

                @Override
                public void onException(Exception e) {
                    System.out.println("Hashtag Exception:");
                    e.printStackTrace();
                }
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                }
                public void onTrackLimitationNotice(int i) {
                }
                public void onScrubGeo(long l, long l1) {
                }
                public void onStallWarning(StallWarning stallWarning) {
                }

            };
            FilterQuery query = new FilterQuery();
            String hashtag = "#" + user.getScreenName();
            String keywords[] = {hashtag};

            query.track(keywords);

            // ===============


            tweetStream.addListener(tweetsListener);
            tweetStream.filter(query);

            try{
                Thread.sleep(400000);
                System.out.println("Hashtag thread sleep over");
//                twitterStream.cleanUp();
                // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
//                twitterStream.sample();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
    public boolean writeToDb (Status status){
        BasicDBObject tweet;
        WriteResult writeResult;
        tweet = new BasicDBObject("handle", user.getScreenName())
                .append("tracker_id", trackerId)
                .append("tweeting_user", status.getUser().getScreenName())
                .append("tweet_id", status.getId())
                .append("created_at", status.getCreatedAt().getTime())
                .append("text", status.getText());
        writeResult = hashtags.insert(tweet);

        JsonObject result = jsonParser.parse(writeResult.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }
}
