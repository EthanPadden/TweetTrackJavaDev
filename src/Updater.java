import com.mongodb.DB;

public class Updater {
    private DB db;
    private String trackerID;

    public Updater(DB db, String trackerID) {
        this.db = db;
        this.trackerID = trackerID;
    }
    //    public void updateMentions() {
//        // Does not update last_updated
//        BasicDBObject newDocument =
//                new BasicDBObject().append("$inc",
//                        new BasicDBObject().append("mentions_count", 99));
//
//        stats.update(new BasicDBObject().append("tracker_id", trackerId), newDocument);
//    }
//
//    public void updateTweetStats() {
//        // TODO: go through and count GET request to endpoints
//        // Search for tweets in DB from this tracker
//        BasicDBObject inQuery = new BasicDBObject();
//        inQuery.put("tracker_id", "5d1cbada85942e436ee7b648");
//
//        DBCursor cursor = tweets.find(inQuery);
//        JsonParser jsonParser = new JsonParser();
//        Twitter twitter = new TwitterFactory().getInstance();
//
//        // For each tweet
//        while (cursor.hasNext()) {
//            // Get the tweet id
//            JsonElement statusJSON = jsonParser.parse(cursor.next().toString());
//            JsonObject statusObj = statusJSON.getAsJsonObject();
//            long id = statusObj.get("tweet_id").getAsLong();
//
//            // Get the details from the Twitter API
//            // Update the info in the DB for that tweet
//            try {
//                Status status = twitter.showStatus(id);
//                BasicDBObject newDocument = new BasicDBObject();
//                newDocument.append("$set", new BasicDBObject().append("favourite_count", status.getFavoriteCount()));
//                newDocument.append("$set", new BasicDBObject().append("rt_count", status.getRetweetCount()));
//                BasicDBObject searchQuery = new BasicDBObject().append("tweet_id", id);
//                tweets.update(searchQuery, newDocument);
//            } catch (TwitterException e) {
//                System.out.println("Tweet with id " + id + " no longer exists");
//            }
//
//        }
}
