import com.google.gson.JsonObject;
import twitter4j.*;
import java.util.*;

public class Main {
    /**
     * args[0] => The command for the data required
     * args[1] => The Twitter handle
     * **/

    private static final String[] commands = {
        "overview", "tweetstats"
    };

    public static void main(String[] args) {
        if(args[0].equals(commands[0])) {
            String output = getBasicInfo(args[1]);
            if(output == null) {
                System.out.println("Failed to get information for this handle");
                System.exit(-1);
            } else {
                System.out.println(output);
                System.exit(0);
            }
        } else if (args[0].equals(commands[1])) {
            // Get tweet analytics
            System.exit(0);
        } else {
            System.out.println("Please enter a valid command");
            System.out.println("Commands must be in the form: command handle");
            System.out.println("Possible commands:");
            for(String command : commands) System.out.println(command);
            System.exit(-1);
        }
       //
//                        System.exit(-1);
    }

    static private String getBasicInfo(String handle) {
        Account account = new Account(handle);
        User user = account.verifyAccount();

        if(user != null) {
            JsonObject output = new JsonObject();

            output.addProperty("name", user.getName());
            output.addProperty("handle", user.getScreenName());
            output.addProperty("tweetCount", user.getStatusesCount());
            output.addProperty("followersCount", user.getFollowersCount());

            return output.toString();
        } else {
            return null;
        }
    }
}

