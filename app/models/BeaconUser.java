package models;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.geoWithinCenterSphere;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.nin;
import static com.mongodb.client.model.Updates.addEachToSet;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.pullAll;
import static com.mongodb.client.model.Updates.set;
import static helpers.JsonHelpers.iterableToJson;
import static helpers.PasswordStorage.createHash;
import static helpers.PasswordStorage.verifyPassword;
import static java.util.Arrays.asList;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BeaconUser {
  // database connection members
  public MongoClient mongoClient;
  public MongoDatabase db;
  public MongoCollection<Document> users;
  public MongoCollection<Document> beacons;

  // BeaconUser fields
  public String username;
  public String passwordHash;
  public List<String> interests;
  public Point lastLocation;

  // use when loading a user from the database
  // follow by calling getUserByName
  public BeaconUser(String hostName, String dbName) {
    MongoClientURI uri = new MongoClientURI(hostName);
    mongoClient = new MongoClient(uri);
    db = mongoClient.getDatabase(dbName);
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");
  }

  // use when creating a new user
  // follow by calling insert
  public BeaconUser(String hostURI, String dbName, String name, String password,
                    List<String> userInterests, double latCoord, double longCoord) {
    // connect instance to the database
    MongoClientURI uri = new MongoClientURI(hostURI);
    mongoClient = new MongoClient(uri);
    db = mongoClient.getDatabase(dbName);
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");

    // initialize the fields for the user object
    username = name;
    try {
      passwordHash = createHash(password);
    } catch (Exception e) { passwordHash = ""; } // empty passwordHash will be rejected on insert
    interests = userInterests;
    // create a location Point: longitude first - required for database query on GeoJSON
    lastLocation = new Point(new Position(longCoord, latCoord));
  }

  // inserts the new user instance into MongoDB
  // returns true on successful insertion
  // returns false on unsuccessful insert (caused by non-unique username)
  public boolean insert() {
    boolean insert = true;

    FindIterable<Document> fi = users.find(eq("username", this.username)).limit(1);

    if (fi.first() != null) {
      insert = false;
    }

    if (insert) {
      Document user = new Document("username", this.username)
                      .append("passwordHash", this.passwordHash)
                      .append("interests", this.interests) // ArrayList converts to JSON array
                      .append("lastLocation", this.lastLocation); // Point coverts to GeoJSON

      // attempt insert only if hash is not empty
      if (!this.passwordHash.equals("")) {
        try {
          users.insertOne(user);
        } catch (MongoWriteException mwe) {
          // inserted set to false on exception for non-unique username
          insert = false;
        }
      } else { insert = false; }
    }
    return insert;
  }

  public boolean authenticate(String password) {
    boolean authenticated = false;
    String passHash = "";

    // get the password hash from the database
    FindIterable<Document> userIterable = users.find(eq("username", this.username));
    Document thisUser = userIterable.first();
    if (thisUser != null) {
      passHash = thisUser.getString("passwordHash");
    }

    try {
      authenticated = verifyPassword(password, passHash);
    } catch (Exception e) { authenticated = false; }

    return authenticated;
  }

  // loads user instance from database into constructed BeaconUser instance
  // returns String of the entire BeaconUser object as JSON
  // returns empty String if user is not found
  public String getUserByName(String name) {
    String userAsJson = null;
    FindIterable<Document> userIterable = users.find(eq("username", name));
    Document thisUser = userIterable.first();

    if (thisUser != null) {
      this.username = thisUser.getString("username");
      this.passwordHash = thisUser.getString("passwordHash");
      this.interests = thisUser.get("interests", ArrayList.class); // casts interests field to ArrayList
      // parse inner lastLocation Document to set this instance's lastLocation field
      Document loc = thisUser.get("lastLocation", Document.class);
      ArrayList<Double> coords = loc.get("coordinates", ArrayList.class);
      this.lastLocation = new Point(new Position(coords.get(0), coords.get(1)));

      userAsJson = thisUser.toJson();
    }
    return userAsJson;
  }

  // Data member accessor methods

  public String getUserName() {
    return this.username;
  }

  public List<String> getInterests() {
    return this.interests;
  }

  public Point getLastLocation() {
    return this.lastLocation;
  }

  // Data member mutator methods

  public boolean changeUsername(String newName) {
    boolean updated = true;

    try {
      users.updateOne(eq("username", this.username), set("username", newName));
    } catch (MongoWriteException mwe) {
      // updated set to false on exception for non-unique username
      updated = false;
    }

    // if the update was successful, update the name in the current object
    if (updated) {
      this.username = newName;
    }

    return updated;
  }

  public boolean addInterest(String newInterest) {
    UpdateResult ur = users.updateOne(eq("username", this.username), addToSet("interests", newInterest));
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.interests.add(newInterest);
    }
    return completed;
  }

  public boolean addInterests(List<String> newInterests) {
    UpdateResult ur = users.updateOne(eq("username", this.username), addEachToSet("interests", newInterests));
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.interests.addAll(newInterests);
    }
    return completed;
  }

  public boolean removeInterest(String removeTarget) {
    UpdateResult ur = users.updateOne(eq("username", this.username), pull("interests", removeTarget));
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.interests.remove(removeTarget);
    }
    return completed;
  }

  public boolean removeInterests(List<String> removeTargets) {
    UpdateResult ur = users.updateOne(eq("username", this.username), pullAll("interests", removeTargets));
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.interests.removeAll(removeTargets);
    }
    return completed;
  }

  public boolean updateLastLocation(String username, double latCoord, double longCoord) {
    Point newLocation = new Point(new Position(longCoord, latCoord));
    UpdateResult ur = users.updateOne(eq("username", username), set("lastLocation", newLocation));
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.lastLocation = newLocation;
    }
    return completed;
  }

  // Beacon creation method
  // returns true on successful creation
  public boolean placeBeacon(String hostName, String dbName, String title, double latCoord, double longCoord, Long start,
                             Long end, double range, String placeName, String address, List<String> tagList) {
    // call the Beacon class constructor
    Beacon newBeacon = new Beacon(hostName, dbName, this.username, title, latCoord, longCoord,
                              start, end, range, placeName, address, tagList);

    boolean created = newBeacon.insert();
    return created;
  }

  public boolean attendBeacon(Beacon beacon) {
    boolean success = true;
    // increment notified count and add user to the notified list
    success = success && beacon.updateNotifiedCount(beacon.getNotifiedCount() + 1);
    success = success && beacon.addOneNotified(this.username);

    return success;
  }


  // User search methods

  // input beacon's coordinates, proximity in miles, and list of users that have already attended
  // returns JSON formatted String of the form { users: [ <users> ]}
  // where <users> is a list of users within range of the beacon that have not yet been notified
  public String findNearbyUsers(double latCoord, double longCoord, double range, List<String> notified) {
    // aggregate result Documents from users collection
    AggregateIterable<Document> userAggregation = users.aggregate(asList(
        // first aggregate by finding users within range of the beacon
        match(geoWithinCenterSphere( "lastLocation",
                                     longCoord,
                                     latCoord,
                                     range / 3963.2 )),
        // only match users that are not in the previously notified list
        match(nin( "username", notified ))
    ));

    String result = iterableToJson("users", userAggregation);
    return result;
  }

  // private version of the nearbyUsers method
  // input is the same as findNearbyUsers with the addition of selected ArrayList
  // selected is a list of friends selected to receive notification about the beacon
  // returns JSON formatted String of the form { users: [ <users> ]}
  // where <users> is a list of users within range of the beacon that have not yet been notified
  public String privateFindNearbyUsers(double latCoord, double longCoord, double range,
                                       List<String> notified, List<String> selected) {
    // aggregate result Documents from users collection
    AggregateIterable<Document> userAggregation = users.aggregate(asList(
      // first match on the users specified in the selected list
      match(in( "username", selected )),
      // then find those close to beacon
      match(geoWithinCenterSphere( "lastLocation", longCoord, latCoord, range / 3963.2)),
      // only match users that have not yet been notified
      match(nin( "username", notified ))
    ));

    String result = iterableToJson("users", userAggregation);
    return result;
  }

  // close the connection to the database
  public void closeConnection() {
    mongoClient.close();
  }
}
