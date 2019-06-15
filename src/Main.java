import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import twitter4j.*;
import twitter4j.json.DataObjectFactory;

import java.util.*;

public class Main {
    /**
     * args[0] => The command for the data required
     * args[1] => The Twitter handle
     * args[2] => The number of tweets required
     * **/

    private static final String[] commands = {
        "overview", "tweetstats", "tweetbytime"
    };

    public static void main(String[] args) {
        try {
            if (args[0].equals(commands[0])) {
                try {
                    boolean success = getBasicInfo(args[1]);
                    if (!success) {
                        System.out.println("Failed to get information for this handle");
                        System.exit(-1);
                    } else {
                        System.exit(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("A Twitter handle must be inputted");
                    System.exit(-1);
                }
            } else if (args[0].equals(commands[1])) {
                try {
                    List<JsonObject> output = getTweets(args[1], args[2]);
                    if (output == null) {
                        System.out.println("Failed to get information for this handle");
                        System.exit(-1);
                    } else {
                        AsyncRouteComm asyncRouteComm = new AsyncRouteComm();
                        asyncRouteComm.asyncOutput(output);
                        System.exit(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Input must be of the form: command handle numberOfTweets");
                    System.exit(-1);
                }
            }
             else {
                System.out.println("Please enter a valid command");
                System.out.println("Commands must be in the form: command handle");
                System.out.println("Possible commands:");
                for (String command : commands) System.out.println(command);
                System.exit(-1);
            }
        } catch(IndexOutOfBoundsException e) {
            System.out.println("Enter input");
            System.exit(-1);
        }
    }

    static private boolean getBasicInfo(String handle) {
        Account account = new Account(handle);
        User user = account.verifyAccount();

        if(user != null) {
            JsonObject output = new JsonObject();

            output.addProperty("name", user.getName());
            output.addProperty("handle", user.getScreenName());
            output.addProperty("tweetCount", user.getStatusesCount());
            output.addProperty("followersCount", user.getFollowersCount());

            System.out.println(output.toString());
            return true;
        } else {
            return false;
        }
    }

    static private List<JsonObject> getTweets(String handle, String numTweets) {
        try {
            TweetStream tweetStream = new TweetStream(handle);
            int tweetCount = Integer.parseInt(numTweets);
            List<Status> statuses = tweetStream.getTweets(tweetCount);
            if (statuses == null) return null;
            else {
                ArrayList<JsonObject> output = new ArrayList<JsonObject>();

                for (Status status : statuses) {

                    JsonObject statusJson = new JsonObject();
                    statusJson.addProperty("id", status.getId());
                    statusJson.addProperty("text", status.getText());
                    statusJson.addProperty("created_at", status.getCreatedAt().toString());
                    statusJson.addProperty("favourite_count", status.getFavoriteCount());
                    statusJson.addProperty("rt_count", status.getRetweetCount());
                    statusJson.addProperty("is_rt", status.isRetweet());

                    output.add(statusJson);

                }

                return output;
            }
        } catch (NumberFormatException e) {
            System.out.println("The number of tweets must be an integer");
            return null;
        }
    }

    static private String getTweetsByTime(String handle, String numDays) {
        try {
            TweetStream tweetStream = new TweetStream(handle);
            int daysCount = Integer.parseInt(numDays);
            List<Status> statuses = tweetStream.getTweetsByTime(daysCount);

            if (statuses == null) return null;
            else {
                JsonObject output = new JsonObject();
                JsonArray statusArray = new JsonArray();

                for (Status status : statuses) {
                    JsonObject statusJson = new JsonObject();
                    statusJson.addProperty("id", status.getId());
                    statusJson.addProperty("text", status.getText());
                    statusJson.addProperty("created_at", status.getCreatedAt().toString());
                    statusJson.addProperty("favourite_count", status.getFavoriteCount());
                    statusJson.addProperty("rt_count", status.getRetweetCount());
                    statusJson.addProperty("is_rt", status.isRetweet());

                    statusArray.add(statusJson);
                }
                output.add("tweetStream", statusArray);

                return output.toString();
            }
        } catch (NumberFormatException e) {
            System.out.println("The number of tweets must be an integer");
            return null;
        }
    }
}

