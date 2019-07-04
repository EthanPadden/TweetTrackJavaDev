import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.*;
import twitter4j.Status;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class Transporter {
    private FileReader fileReader;
    private MongoClient mongoClient;
    private DB db;
    private DBCollection mentions;
    private DBCollection trackers;
    private DBCollection tweets;
    private DBCollection stats;
    JsonParser jsonParser;
    private Tracker tracker;
    private String trackerId;
    private static String CREDS_FILE = "src/mongoCredentials.json";


    public String getTrackerId() {
        return trackerId;
    }

    public Transporter(Tracker tracker) {
        // File that stored credentials
        try {
            fileReader = new FileReader(CREDS_FILE);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        jsonParser = new JsonParser();
        this.tracker = tracker;
        setCredentials();
        boolean saved = saveTrackerToDB();
        if(saved) {
            System.out.println("ID: " + trackerId);
            initStatsRecord();
        }
    }

    private boolean saveTrackerToDB() {
        DBObject doc = new BasicDBObject("start_date", new Date().toString())
                .append("handle", tracker.getUser().getScreenName());
        WriteResult w  = trackers.insert(doc);
        trackerId = ((BasicDBObject) doc).get("_id").toString();
        JsonObject result = jsonParser.parse(w.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }

    private boolean initStatsRecord() {
        DBObject doc = new BasicDBObject("last_updated", new Date().toString())
                .append("handle", tracker.getUser().getScreenName())
                .append("tracker_id", trackerId)
                .append("tweet_count", 0)
                .append("mentions_count", 0)
                .append("likes_count", 0)
                .append("rt_count", 0);
        WriteResult w  = stats.insert(doc);
        JsonObject result = jsonParser.parse(w.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }

    public void updateMentions() {
        // Does not update last_updated
        BasicDBObject newDocument =
                new BasicDBObject().append("$inc",
                        new BasicDBObject().append("mentions_count", 99));

        stats.update(new BasicDBObject().append("tracker_id", trackerId), newDocument);
    }
    private void setCredentials() {
        try {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String fileContent = "";
            while ((line = bufferedReader.readLine()) != null) {
                fileContent += line;
            }
            bufferedReader.close();
            JsonElement creds = jsonParser.parse(fileContent);
            JsonObject credsObj = creds.getAsJsonObject();
            String userName = credsObj.get("user_name").getAsString();
            String psw = credsObj.get("psw").getAsString();
            String host = credsObj.get("host").getAsString();
            String port = credsObj.get("port").getAsString();
            String database = credsObj.get("db").getAsString();

            String clientArgs = "mongodb://" + userName + ":" + psw + "@" + host + ":" + port + "/" + database;
            mongoClient = new MongoClient(new MongoClientURI(clientArgs));
            db = mongoClient.getDB(database);
            trackers = db.getCollection("trackers");
            mentions = db.getCollection("mentions");
            tweets = db.getCollection("tweets");
            stats = db.getCollection("stats");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean writeToDb(Status status, boolean isFromTrackedAccount) {
        DBObject tweet;
        WriteResult writeResult;
        if(isFromTrackedAccount) {
            tweet = new BasicDBObject("handle", tracker.getUser().getScreenName())
                    .append("tracker_id", trackerId)
                    .append("tweet_id", status.getId())
                    .append("created_at", status.getCreatedAt().toString())
                    .append("text", status.getText())
                    .append("favourite_count", status.getFavoriteCount())
                    .append("rt_count", status.getRetweetCount())
                    .append("is_rt", status.isRetweet());
            writeResult  = tweets.insert(tweet);

        } else {
            tweet = new BasicDBObject("handle", tracker.getUser().getScreenName())
                    .append("tracker_id", trackerId)
                    .append("tweeting_user", status.getUser().getScreenName())
                    .append("tweet_id", status.getId())
                    .append("created_at", status.getCreatedAt().toString())
                    .append("text", status.getText());
            writeResult  = mentions.insert(tweet);

        }
        JsonObject result = jsonParser.parse(writeResult.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }
}
