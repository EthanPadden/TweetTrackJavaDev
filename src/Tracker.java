import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import twitter4j.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.Scanner;
import java.util.Timer;

public class Tracker {
    private User user;
    private boolean isTracking;
    private static String defMsgFile = "trackermsg.txt";
    private TwitterStream twitterStream;
    private Transporter transporter;


    public Tracker(String userName) {
        Account account = new Account(userName);
        // May be null
        user = account.verifyAccount();
        if (user == null) {
            System.out.println("Could not set up tracker - user does not exist");
            System.exit(-1);
        } else {
            transporter = new Transporter(this);
            isTracking = false;
            twitterStream = new TwitterStreamFactory().getInstance();
        }
    }

    public void trackUserTweets() {
        if (user == null) System.out.println("Cannot find user");
        else {
            // NOT FILTERED
//            StatusOutput statusOutput = new StatusOutput();
//            Timer timer = new Timer();
//            timer.scheduleAtFixedRate(statusOutput, 1000, 60000);
            StatusListener listener = new StatusListener() {
                @Override
                public void onStatus(Status status) {
                    if(status.getUser().getScreenName().compareTo(user.getScreenName()) != 0 && !status.isRetweet()){
                        transporter.writeToDb(status, false);

                    } else if(status.getUser().getScreenName().compareTo(user.getScreenName()) == 0){
                        transporter.writeToDb(status, true);
                    }
                }

                @Override
                public void onException(Exception e) {
                    System.out.println("StatusListener Exception:");
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

            twitterStream.addListener(listener);
            twitterStream.filter(new FilterQuery("@" + user.getScreenName()));

            try{
                Thread.sleep(400000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            twitterStream.cleanUp();
            // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
            twitterStream.sample();
        }

    }

    public boolean isTracking() {
        return isTracking;
    }


    public User getUser() {
        return user;
    }

    public void setTracking(boolean tracking) {
        isTracking = tracking;
    }
}
