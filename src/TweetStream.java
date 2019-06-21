import twitter4j.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class TweetStream extends TwitterEntity{
    private User user;

    public TweetStream() {
        twitter = new TwitterFactory().getInstance();
    }

    public TweetStream(String userName) {
        this();
        this.userName = userName;
        Account account = new Account(userName);
        // May be null
        user = account.verifyAccount();
    }

    public List<Status> getTweets(int numTweets) {
        if(user == null) return null;
        else {
            String handle = user.getScreenName();
            Paging paging = new Paging(1,numTweets);
            try {
                List<Status> statuses = twitter.getUserTimeline(handle, paging);
                return statuses;
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                return null;
            }
        }
    }

    public List<Status> getTweetsByTime(int numDays) {
        if(user == null) return null;
        else {
            String handle = user.getScreenName();

            // Note - more paging for more than 200 tweets
            Paging paging = new Paging(1,200);

            try {
                List<Status> statuses = twitter.getUserTimeline(handle,paging);
                if(statuses.size() <= 1) return null;

                ArrayList<Status> output = new ArrayList<Status>();
                Date date = new Date();
                date.setDate(date.getDate()-numDays);

                for(Status status : statuses) {
                    if(status.getCreatedAt().compareTo(date) > 0){
                        output.add(status);
                    }
                    else {
                        break;
                    }
                }

                return output;
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                return null;
            }
        }
    }

    public Hashtable<String, Integer> getMentionsCount(int numDays) {
        try {
            Hashtable<String, Integer> mentionsRecord = new Hashtable<String, Integer>();
            Query query = new Query(user.getScreenName() + " +exclude:retweets");

            // Dates
            Date stepperDate = new Date();
            Date pastDate = new Date();
            pastDate.setDate(stepperDate.getDate()-numDays);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String pastDateStr = format.format(pastDate);
            String stepperDateStr = format.format(stepperDate);

            // Query settings
            query.setSince(pastDateStr);
            query.setCount(100);

            try {

                while(true) {

                    QueryResult result = twitter.search(query);
                    List<Status> batch = result.getTweets();
                    if(batch.isEmpty()) break;
                    // Look at first and last tweet - compare dates
                    Status first = batch.get(0);
                    int size = batch.size();
                    Status last = batch.get(size-1);
                    String fdate = format.format(first.getCreatedAt());
                    String ldate = format.format(last.getCreatedAt());

                    if(fdate.compareTo(ldate) == 0) {
                        int currentVal = 0;
                        if(mentionsRecord.get(stepperDateStr) != null) {
                            currentVal = mentionsRecord.get(stepperDateStr);
                            mentionsRecord.put(stepperDateStr, currentVal+size);
                        } else { // If there is no record yet
                            mentionsRecord.put(stepperDateStr, size);
                        }
                    } else {
                        // Loop through to hash dates
                        int i = 0;
                        while (i < batch.size()){
                            if(stepperDateStr.compareTo(format.format(batch.get(i).getCreatedAt())) == 0) {
                                int currentVal = 0;
                                if(mentionsRecord.get(stepperDateStr) != null) {
                                    currentVal = mentionsRecord.get(stepperDateStr);
                                    mentionsRecord.put(stepperDateStr, currentVal+1);
                                } else {
                                    mentionsRecord.put(stepperDateStr, 1);
                                }
                                i++;
                            } else {
                                stepperDate.setDate(stepperDate.getDate()-1);
                                stepperDateStr = format.format(stepperDate);
                            }
                        }

                    }

                    if(result.hasNext())//there is more pages to load
                    {
                        System.out.println("HAS NEXT: " + result.hasNext());
                        query = result.nextQuery();
                    } else break;


                }


            } catch (TwitterException te) {
                System.out.println("Couldn't connect: " + te);

            }



            return mentionsRecord;


        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Caught");
            return null;
        }
    }

    public List<Status> getTweetsByTime(String startDateStr, String endDateStr) {
        /** DATE FORMAT:
         * dd/MM/yyyy
         * **/

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // HERE
        if(user == null) return null;
        else {
            String handle = user.getScreenName();

            // Note - more paging for more than 200 tweets
            Paging paging = new Paging(1,200);

            try {
                List<Status> statuses = twitter.getUserTimeline(handle,paging);
                if(statuses.size() <= 1) return null;

                ArrayList<Status> output = new ArrayList<Status>();
                Date date = new Date();
                date.setDate(date.getDate()-numDays);

                for(Status status : statuses) {
                    if(status.getCreatedAt().compareTo(date) > 0){
                        output.add(status);
                    }
                    else {
                        break;
                    }
                }

                return output;
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                return null;
            }
        }
    }
}

