import com.google.gson.JsonObject;
import twitter4j.*;
import java.util.*;

public class Main {
    private static final String[] commands = {
        "overview", "tweetstats"
    };

    public static void main(String[] args) {
       //
//                        System.exit(-1);
    }

    private String getBasicInfo(String handle) {
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

