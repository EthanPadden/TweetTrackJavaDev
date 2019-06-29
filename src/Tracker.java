import com.google.gson.JsonObject;
import twitter4j.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.Scanner;
import java.util.Timer;

public class Tracker {
    private User user;
    private boolean isTracking;
    private PrintWriter printWriter;
    private PrintWriter msgWriter;
    private static String defMsgFile = "trackermsg.txt";
    private TwitterStream twitterStream;
//        PrintWriter printWriter = new PrintWriter(args[3]);
//        for (JsonObject statusJson : output) {
//
//        }
//        System.out.println("SUCCESS");
//    }
//                        System.exit(
    public Tracker() {
        isTracking = false;
        try {msgWriter = new PrintWriter(defMsgFile);}
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        twitterStream = new TwitterStreamFactory().getInstance();
    }

    public Tracker(String userName, String outputfile) {
        this();
        Account account = new Account(userName);
        // May be null
        user = account.verifyAccount();
        if (user == null) {
            System.out.println("Could not set up tracker - user does not exist");
        }
        try{

        printWriter= new PrintWriter(outputfile);

        } catch (
        FileNotFoundException e) {
        System.out.println("File not found to store tweets");
        System.out.println("FAILURE");

        System.exit(2);
        }
    }

    public void setFile(String filename) {
        try {
            printWriter = new PrintWriter(filename);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void trackUserTweets() {
        if (user == null) System.out.println("Cannot find user");
        else {
            // NOT FILTERED
            StatusOutput statusOutput = new StatusOutput();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(statusOutput, 1000, 60000);
            StatusListener listener = new StatusListener() {
                @Override
                public void onStatus(Status status) {
                    if(status.getUser().getScreenName().compareTo(user.getScreenName()) != 0 && !status.isRetweet()){
                        JsonObject statusJson = new JsonObject();
                        statusJson.addProperty("user", status.getUser().getScreenName());
                        statusJson.addProperty("tweet_id", status.getId());
                        statusJson.addProperty("created_at", status.getCreatedAt().toString());
                        statusJson.addProperty("text", status.getText());
                        printWriter.println(statusJson.toString());
                        printWriter.flush();
                    }

                }

                @Override
                public void onException(Exception e) {
                    System.out.println("StatusListener Exception:");
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
            isTracking = true;
        }

    }

    public boolean isTracking() {
        return isTracking;
    }

    public boolean setUser(String userName) {
        Account account = new Account(userName);
        // May be null
        user = account.verifyAccount();
        if (user == null) {
            System.out.println("Could not set up tracker - user does not exist");
            return false;
        }
        return true;
    }

    public void shutDown() {
        twitterStream.shutdown();
        System.exit(0);
    }
}
