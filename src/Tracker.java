import twitter4j.*;

import java.util.Scanner;

public class Tracker {
    private User user;
    private boolean isTracking;

    public Tracker() {
        isTracking = false;
    }

    public Tracker(String userName) {
        Account account = new Account(userName);
        // May be null
        user = account.verifyAccount();
        if (user == null) {
            System.out.println("Could not set up tracker - user does not exist");
        }


    }

    public void trackUserTweets() {
        if (user == null) System.out.println("Cannot find user");
        else {
            // NOT FILTERED
            StatusListener listener = new StatusListener() {
                @Override
                public void onStatus(Status status) {
//                    System.out.println("Status: " + status.getUser().getScreenName() + ": " + status.getText());
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
            TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
            twitterStream.addListener(listener);
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
}
