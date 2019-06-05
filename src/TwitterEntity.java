import com.google.gson.JsonObject;
import twitter4j.*;
import java.util.*;

public abstract class TwitterEntity {
    protected Twitter twitter;
    protected String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
