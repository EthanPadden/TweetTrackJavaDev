import twitter4j.*;

public class Account extends TwitterEntity{
    private User user;

    public Account() {
        twitter = new TwitterFactory().getInstance();
    }

    public Account(String userName) {
        this();
        this.userName = userName;
    }

    public User verifyAccount() {
        if(userName == null) return null;
        else {
            try {
                return twitter.showUser(userName);
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                return null;
            }
        }
    }
}
