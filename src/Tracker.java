import com.google.gson.JsonObject;
import twitter4j.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mongodb.*;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tracker {
    private User user;
    private boolean isTracking;
    private static String defMsgFile = "trackermsg.txt";
    private TwitterStream mentionStream;
    private TwitterStream tweetStream;
    private FileReader fileReader;
    private MongoClient mongoClient;
    private DB db;
    private DBCollection mentions;
    private DBCollection trackers;
    private DBCollection tweets;
    private DBCollection stats;
    JsonParser jsonParser;
    private String trackerId;
    private static String CREDS_FILE = "src/mongoCredentials.json";
    private Updater updater;


    public Tracker(String userName) {
        Account account = new Account(userName);
        // May be null
        user = account.verifyAccount();
        if (user == null) {
            System.out.println("Could not set up tracker - user does not exist");
            System.exit(-1);
        } else {
            isTracking = false;
            mentionStream = new TwitterStreamFactory().getInstance();
            tweetStream = new TwitterStreamFactory().getInstance();
        }

        // From transporter
        // File that stored credentials
        try {
            fileReader = new FileReader(CREDS_FILE);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        jsonParser = new JsonParser();
        setCredentials(true);
        boolean saved = saveTrackerToDB();
        if (saved) {
            System.out.println("ID: " + trackerId);
            initStatsRecord();
        }
//
        updater = new Updater(stats, tweets, trackerId);
//
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                updater.updateTweetStats();
            }
        });

        thread.start();

    }

    private boolean saveTrackerToDB() {
        DBObject doc = new BasicDBObject("start_date", new Date().toString())
                .append("handle", user.getScreenName());
        WriteResult w = trackers.insert(doc);
        trackerId = ((BasicDBObject) doc).get("_id").toString();
        JsonObject result = jsonParser.parse(w.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }

    private boolean initStatsRecord() {
        DBObject doc = new BasicDBObject("last_updated", new Date().toString())
                .append("handle", user.getScreenName())
                .append("tracker_id", trackerId)
                .append("tweet_count", 0)
                .append("mentions_count", 0)
                .append("likes_count", 0)
                .append("rt_count", 0);
        WriteResult w = stats.insert(doc);
        JsonObject result = jsonParser.parse(w.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }

    private void setCredentials (boolean isServer) {
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
            String clientArgs;
            String host = credsObj.get("host").getAsString();
            String port = credsObj.get("port").getAsString();
            String database = credsObj.get("db").getAsString();
            if(isServer) {
                String userName = credsObj.get("user_name").getAsString();
                String psw = credsObj.get("psw").getAsString();

                clientArgs = "mongodb://" + userName + ":" + psw + "@" + host + ":" + port + "/" + database;

            } else                 clientArgs = "mongodb://localhost:27017/TweetTrack";


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

    public boolean writeToDb (Status status, boolean isFromTrackedAccount){
        BasicDBObject tweet;
        WriteResult writeResult;
        if (isFromTrackedAccount) {
            tweet = new BasicDBObject("handle", user.getScreenName())
                    .append("tracker_id", trackerId)
                    .append("tweet_id", status.getId())
                    .append("created_at", status.getCreatedAt().getTime())
                    .append("text", status.getText())
                    .append("favourite_count", status.getFavoriteCount())
                    .append("rt_count", status.getRetweetCount())
                    .append("is_rt", status.isRetweet());

            BasicDBObject mediaEntitiesObj = new BasicDBObject();
            MediaEntity[] mediaEntities = status.getMediaEntities();

            if(mediaEntities != null) {
                int i = 0;
                for(MediaEntity mediaEntity : mediaEntities) {
                    if(mediaEntity.getType().compareTo("photo") == 0) {
                        BasicDBObject mediaEntityObj = new BasicDBObject().append("type", "photo")
                                .append("url", mediaEntity.getURL());
                        mediaEntitiesObj.append(Integer.toString(i), mediaEntityObj);
                    } else if(mediaEntity.getType().compareTo("video") == 0) {
                        BasicDBObject mediaEntityObj = new BasicDBObject().append("type", "video")
                                .append("url", mediaEntity.getURL());
                        mediaEntityObj.append("duration", mediaEntity.getVideoDurationMillis());
                        mediaEntitiesObj.append(Integer.toString(i), mediaEntityObj);
                    } else if(mediaEntity.getType().compareTo("animated_gif") == 0) {
                        BasicDBObject mediaEntityObj = new BasicDBObject().append("type", "animated_gif")
                                .append("url", mediaEntity.getURL());
                        mediaEntitiesObj.append(Integer.toString(i), mediaEntityObj);
                    }
                        i++;
                }
            }
            tweet.append("media_entities", mediaEntitiesObj);
            URLEntity[] urlEntities = status.getURLEntities();
            BasicDBObject urlEntitiesObj = new BasicDBObject();

            if(urlEntities != null) {
                int i = 0;
                for(URLEntity urlEntity : urlEntities) {
                    urlEntitiesObj.append(Integer.toString(i), urlEntity.getURL());
                    i++;
                }
            }
            tweet.append("url_entities", urlEntitiesObj);

            writeResult = tweets.insert(tweet);

        } else {
            tweet = new BasicDBObject("handle", user.getScreenName())
                    .append("tracker_id", trackerId)
                    .append("tweeting_user", status.getUser().getScreenName())
                    .append("tweet_id", status.getId())
                    .append("created_at", status.getCreatedAt().getTime())
                    .append("text", status.getText());
            writeResult = mentions.insert(tweet);

        }
        JsonObject result = jsonParser.parse(writeResult.toString()).getAsJsonObject();
        Number ok = result.get("ok").getAsNumber();
        int okInt = ok.intValue();
        return (okInt == 1);
    }
    public void trackUserTweets() {
        if (user == null) System.out.println("Cannot find user");
        else {
            StatusListener tweetsListener = new StatusListener() {
                @Override
                public void onStatus(Status status) {
                    System.out.println("Status: " + status.getText());
                    if(status.getUser().getScreenName().compareTo(user.getScreenName()) != 0){
                        if(status.isRetweet()) {
                            // Update retweet - in tweets and stats
                            BasicDBObject searchQuery = new BasicDBObject().append("tweet_id", status.getRetweetedStatus().getId()).append("tracker_id", trackerId);
                            try{
                                System.out.println("Retweet " + status.getUser());
                                BasicDBObject rtDoc = new BasicDBObject();
                                rtDoc.append("$inc", new BasicDBObject().append("rt_count", 1));
                                WriteResult writeResult = tweets.update(searchQuery, rtDoc);
                                System.out.println("WRITE RESULT: " + writeResult.toString());
                                JsonObject result = jsonParser.parse(writeResult.toString()).getAsJsonObject();
                                Boolean exisingTweetEdited = result.get("updatedExisting").getAsBoolean();
                                boolean exisingTweetEditedBool = exisingTweetEdited.booleanValue();

                                if(exisingTweetEditedBool) {
                                    searchQuery = new BasicDBObject().append("tracker_id", trackerId);
                                    stats.update(searchQuery, rtDoc);
                                }

                            } catch (MongoException e) {
                                System.out.println("Retweet is from a tweet prior to tracking");
                                System.out.println("Retweet stats will still be updated");
                            }
                        } else {
                            // Update mentions
                            writeToDb(status, false);
                            System.out.println("User was mentioned by " + status.getUser().getScreenName());
                            System.out.println("Text: " + status.getText());
                            updater.updateMentions();
                        }

                    } else if(status.getUser().getScreenName().compareTo(user.getScreenName()) == 0){
                        writeToDb(status, true);
                        BasicDBObject searchQuery = new BasicDBObject().append("tracker_id", trackerId);

                        BasicDBObject tweetCountStats = new BasicDBObject();
                        tweetCountStats.append("$inc", new BasicDBObject().append("tweet_count", 1));
                        stats.update(searchQuery, tweetCountStats);
                        System.out.println("User tweeted: " + status.getText());
                    }
                }

                @Override
                public void onException(Exception e) {
                    System.out.println("TweetListener Exception:");
                    e.printStackTrace();
                    isTracking = false;
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



//            mentionStream.addListener(mentionsListener);
            tweetStream.addListener(tweetsListener);
//            System.out.println("Tracking " + mentionsListener);
            FilterQuery query = new FilterQuery();
            query.follow(new long[] { user.getId() });
            tweetStream.filter(query);

            EngmtListener engmtListener = new EngmtListener(user, db, trackerId);
            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    engmtListener.listen();
                }
            });

            thread.start();

            try{
                Thread.sleep(400000);
                System.out.println("Thread sleep over");
//                twitterStream.cleanUp();
                // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
//                twitterStream.sample();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public boolean isTracking() {
        return isTracking;
    }

    public User getUser() {
        return user;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setTracking(boolean tracking) {
        isTracking = tracking;
    }
}
