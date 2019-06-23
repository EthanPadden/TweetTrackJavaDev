import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import twitter4j.*;
import twitter4j.json.DataObjectFactory;

import java.util.*;
import java.io.*;


public class Main {
    /**
     * args[0] => The command for the data required
     * args[1] => The Twitter handle
     * args[2] => The number of tweets required
     **/

    private static final String[] commands = {
            "overview", "tweetstats", "tweetbytime", "mentions", "tweetbydate", "tweetbyid"
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
                        System.exit(1);
                    } else {
                        try {
                            PrintWriter printWriter = new PrintWriter(args[3]);
                            for (JsonObject statusJson : output) {
                                printWriter.println(statusJson.toString());
                                printWriter.flush();
                            }
                            System.out.println("SUCCESS");
                        } catch (FileNotFoundException e) {
                            System.out.println("File not found to store tweets");
                            System.out.println("FAILURE");

                            System.exit(2);
                        }

                        System.exit(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Input must be of the form: command handle numberOfTweets");
                    System.exit(-1);
                }
            } else if (args[0].equals(commands[2])) {
                try {
                    List<JsonObject> output = getTweetsByTime(args[1], args[2]);
                    if (output == null) {
                        System.out.println("Failed to get information for this handle");
                        System.exit(-1);
                    } else {
                        try {
                            PrintWriter printWriter = new PrintWriter(args[3]);
                            for (JsonObject statusJson : output) {
                                printWriter.println(statusJson.toString());
                                printWriter.flush();
                            }
                            System.out.flush();
                            System.out.println("SUCCESS");
                            System.exit(0);

                        } catch (FileNotFoundException e) {
                            System.out.println("File not found to store tweets");
                            System.out.println("FAILURE");
                        }
                        System.exit(0);
                    }

                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Input must be of the form: command handle numberOfTweets");
                    System.exit(-1);
                }
            } else if (args[0].equals(commands[3])) {
                try {
                    boolean success = getMentionsCount(args[1], args[2]);
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
            } else if (args[0].equals(commands[4])) {
                try {
                    List<Long> output = getTweetsByDate(args[1], args[2], args[3]);

                    if (output == null) {
                        System.out.println("Failed to get information for this handle");
                        System.exit(-1);
                    } else {
                        try {
                            PrintWriter printWriter = new PrintWriter(args[4]);
                            for (long status : output){
                                printWriter.println(status);
                                printWriter.flush();
                            }
                            System.out.flush();
                            System.out.println("SUCCESS");
                            System.exit(0);

                        } catch (FileNotFoundException e) {
                            System.out.println("File not found to store tweets");
                            System.out.println("FAILURE");
                        }
                        System.exit(0);
                    }

                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Input must be of the form: command handle numberOfTweets");
                    System.exit(-1);
                }
            } else if (args[0].equals(commands[5])) {
                try {
                    TweetStream ts = new TweetStream();
                    ArrayList<String> statusStrs = new ArrayList<String >();
                    // HERE
                    try {
                        // FileReader reads text files in the default encoding.
                        FileReader fileReader =
                                new FileReader(args[1]);

                        // Always wrap FileReader in BufferedReader.
                        BufferedReader bufferedReader =
                                new BufferedReader(fileReader);
// This will reference one line at a time
                        String line = null;
                        while((line = bufferedReader.readLine()) != null && line.length() > 0) {
                            statusStrs.add(line);
                        }

                        // Always close files.
                        bufferedReader.close();
                    }
                    catch(FileNotFoundException ex) {
                        System.out.println("ERR: Unable to open file");
                        System.exit(-1);
                    }
                    catch(IOException ex) {
                        System.out.println(
                                "ERR: Error reading file");
                        // Or we could just do this:
                        // ex.printStackTrace();
                        System.exit(-1);
                    }
                    // HERE
                    if(statusStrs.size() == 0) {
                        System.out.println("ERR: List was empty");
                        System.exit(-1);
                    }
                    JsonObject output = ts.getTweetStatsById(statusStrs);
                    System.out.println("SUCCESS: " + output.toString());


                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Input must be of the form: command handle numberOfTweets");
                    System.exit(-1);
                }
            }else {
                System.out.println("Please enter a valid command");
                System.out.println("Commands must be in the form: command handle");
                System.out.println("Possible commands:");
                for (String command : commands) System.out.println(command);
                System.exit(-1);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Enter input");
            System.exit(-1);
        }
    }

    static private boolean getMentionsCount(String handle, String numDays) {
        Account account = new Account(handle);
        User user = account.verifyAccount();

        if (user != null) {
            int daysCount = Integer.parseInt(numDays);

            TweetStream tweetStream = new TweetStream(handle);
            Hashtable<String, Integer> mentionsCount = tweetStream.getMentionsCount(daysCount);

            if (mentionsCount == null) {
                System.out.println("Failed to get mentions information");
                return false;
            }

            JsonObject entries = new JsonObject();
            for (String key : mentionsCount.keySet()) {
                entries.addProperty(key, mentionsCount.get(key));
            }
            System.out.println(entries.toString());
            return true;
        } else {
            return false;
        }
    }

    static private boolean getBasicInfo(String handle) {
        Account account = new Account(handle);
        User user = account.verifyAccount();

        if (user != null) {
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

    static private List<JsonObject> getTweetsByTime(String handle, String numDays) {
        try {
            TweetStream tweetStream = new TweetStream(handle);
            int daysCount = Integer.parseInt(numDays);
            List<Status> statuses = tweetStream.getTweetsByTime(daysCount);

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

    static private List<Long> getTweetsByDate(String handle, String startDateStr, String endDateStr) {
        try {
            TweetStream tweetStream = new TweetStream(handle);
            List<Status> statuses = tweetStream.getTweetsByDate(startDateStr, endDateStr);

            if (statuses == null) return null;
            else {
                ArrayList<Long> output = new ArrayList<Long>();
                for (Status status : statuses) {
                    output.add(status.getId());
                }
                return output;
            }
        } catch (NumberFormatException e) {
            System.out.println("The number of tweets must be an integer");
            return null;
        }
    }
}

