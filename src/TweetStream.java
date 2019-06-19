import twitter4j.*;

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

    public Hashtable<String, Integer> getMentionsCount(int span, String unit) {
        try {
            Hashtable<String, Integer> mentionsRecord = new Hashtable<String, Integer>();
            Query query = new Query(user.getScreenName() + " +exclude:retweets");

            // Dates
            Date stepperDate = new Date();
            Date pastDate = new Date();

            // Inputs come into play here
            if(unit.compareTo("days") == 0)  {
                pastDate.setDate(stepperDate.getDate()-span);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String pastDateStr = format.format(pastDate);
                String stepperDateStr = format.format(stepperDate);
                // Query settings
                query.setSince(pastDateStr);
                query.setCount(100);
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



            } else if (unit.compareTo("hours") == 0) {
                pastDate.setHours(stepperDate.getHours() - span);
                // TODO: Check hours changing over midnight

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String pastDateStr = format.format(pastDate);
                // Query settings
                query.setSince(pastDateStr);
                query.setCount(100);
                while(true) {
                    QueryResult result = twitter.search(query);
                    List<Status> batch = result.getTweets();
                    if(batch.isEmpty()) break;
                    // Look at first and last tweet - compare dates
                    Status first = batch.get(0);
                    int size = batch.size();
                    Status last = batch.get(size-1);
                    int fhours  = first.getCreatedAt().getHours();
                    int lhours  = last.getCreatedAt().getHours();
                    String timeAppend = ":00";

                    if(fhours == lhours) {
                        int currentVal = 0;
                        if(mentionsRecord.get(stepperDate.getHours()+timeAppend) != null) {
                            currentVal = mentionsRecord.get(stepperDate.getHours()+timeAppend);
                            mentionsRecord.put(stepperDate.getHours()+timeAppend, currentVal+size);
                        } else { // If there is no record yet
                            mentionsRecord.put(stepperDate.getHours()+timeAppend, size);
                        }
                    } else {
                        // Loop through to hash dates
                        int i = 0;
                        Date compareStepperDate = stepperDate;
                        Date comparePastDate = pastDate;
                        comparePastDate.setMinutes(0);
                        comparePastDate.setSeconds(0);
                        compareStepperDate.setMinutes(0);
                        compareStepperDate.setMinutes(0);

                        while (i < batch.size() && compareStepperDate.compareTo(comparePastDate) > 0){
                            if(stepperDate.getHours() == batch.get(i).getCreatedAt().getHours()) {
                                int currentVal = 0;
                                if(mentionsRecord.get(stepperDate.getHours()+timeAppend) != null) {
                                    currentVal = mentionsRecord.get(stepperDate.getHours()+timeAppend);
                                    mentionsRecord.put(stepperDate.getHours()+timeAppend, currentVal+1);
                                } else {
                                    mentionsRecord.put(stepperDate.getHours()+timeAppend, 1);
                                }
                                i++;
                            } else {
                                stepperDate.setHours(stepperDate.getHours()-1);
                            }
                        }

                    }

                    if(result.hasNext())//there is more pages to load
                    {
                        System.out.println("HAS NEXT: " + result.hasNext());
                        query = result.nextQuery();
                    } else break;


                }



            }
                /**
                pastDate.setHours(stepperDate.getHours() - span);
                // TODO: Check hours changing over midnight

                // Query settings
                query.setCount(100);

                while(true) {
                    QueryResult result = twitter.search(query);
                    List<Status> batch = result.getTweets();
                    if(batch.isEmpty()) break;

                    // Look at first and last tweet - compare hours
                    Status first = batch.get(0);
                    int size = batch.size();
                    Status last = batch.get(size-1);
                    int fhours  = first.getCreatedAt().getHours();
                    int lhours  = last.getCreatedAt().getHours();
                    String timeAppend = ":00";

                    if(fhours == lhours) {
                        int currentVal = 0;
                        if(mentionsRecord.get(fhours) != null) {
                            currentVal = mentionsRecord.get(fhours);
                            mentionsRecord.put(fhours+timeAppend, currentVal+size);
                        } else { // If there is no record yet
                            mentionsRecord.put(fhours+timeAppend, size);
                        }
                    } else {
                        // Loop through to hash dates
                        int i = 0;
                        while (i < batch.size()){
                            if(fhours == batch.get(i).getCreatedAt().getHours()) {
                                int currentVal = 0;
                                if(mentionsRecord.get(fhours+timeAppend) != null) {
                                    currentVal = mentionsRecord.get(fhours+timeAppend);
                                    mentionsRecord.put(fhours+timeAppend, currentVal+1);
                                } else {
                                    mentionsRecord.put(fhours+timeAppend, 1);
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



            }**/ else {
                System.out.println("Enter valid unit argument");
                System.exit(-1);
            }

            return mentionsRecord;


        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Caught");
            return null;
        } catch (TwitterException te) {
            System.out.println("Couldn't connect: " + te);
            return null;
        }

    }
}

