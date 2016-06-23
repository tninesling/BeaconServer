package helpers;

import com.mongodb.client.MongoIterable;
import org.bson.Document;
import models.BeaconUser;

import java.util.ArrayList;
import java.util.List;

public class JsonHelpers {
  // returns a JSON formatted String in the form { <name> : [<content>] }
  // where <content> is the list of Documents in the iterable parameter also in JSON format
  public static String iterableToJson(String name, MongoIterable<Document> iterable) {
    String returnList = "{\"" + name + "\": [";

    try {
      // get the response in the iterable as a List of Documents
      List<Document> docList = iterable.into(new ArrayList<Document>());

      for (int i = 0; i < docList.size(); i++) {
        returnList += docList.get(i).toJson();
        if (i < docList.size() - 1) {
          returnList += ", ";
        }
      }
    } catch (NullPointerException npe) {}
    returnList += "]}"; // complete correct JSON format

    return returnList;
  }

  public static String userToJson(BeaconUser user) {
    String userAsJson = "User not found"; // default return value for negative search result

    if (!(user.username == null || user.username.equals(""))) {
      String usernameField = "\"username\" : \"" + user.username + "\"";

      String passwordHashField = "\"passwordHash\" : \"" + user.passwordHash + "\"";

      String interestsField = "\"interests\" : [";

      for (int i = 0; i < user.interests.size(); i++) {
        interestsField += "\"";
        interestsField += user.interests.get(i);
        interestsField += "\"";
        if (i < user.interests.size() - 1) {
          interestsField += ", ";
        }
      }
      interestsField += "]";

      List<Double> coords = user.lastLocation.getCoordinates().getValues();
      String lastLocationField = "\"lastLocation\" : { \"type\" : \"Point\", \"coordinates\" : [";
      lastLocationField += coords.get(0).toString() + ", " + coords.get(1).toString() + "]}";

      userAsJson = "{" + usernameField + ", " + passwordHashField + ", " +
                   interestsField + ", " + lastLocationField + "}";
    }

    return userAsJson;
  }
}
