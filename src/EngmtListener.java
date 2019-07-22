import com.google.gson.JsonParser;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import twitter4j.TwitterStream;
import twitter4j.User;

import java.io.FileReader;

public class EngmtListener {
    private User user;
    private TwitterStream tweetStream;
    private FileReader fileReader;
    private DB db;
    private DBCollection trackers;
    private DBCollection hashtags;
    private DBCollection other;

    public EngmtListener(User user, DB db) {
        this.user = user;
        this.db = db;
    }

    public void listen() {
        System.out.println("Listening");
    }
}
