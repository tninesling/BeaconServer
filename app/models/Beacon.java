package models;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.geoWithinCenterSphere;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.addEachToSet;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.pullAll;
import static com.mongodb.client.model.Updates.set;
import static helpers.JsonHelpers.iterableToJson;
import static java.util.Arrays.asList;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Beacon {
  // database connection members
  public MongoClient mongoClient;
  public MongoDatabase db;
  public MongoCollection<Document> users;
  public MongoCollection<Document> beacons;

  // beacon data members
  String creator;
  String title;
  Point location;
  Date startTime;
  Date endTime;
  Double range;
  String placeName;
  String address;
  List<String> tags;
  Integer notifiedCount;
  List<String> notified;

  // empty constructor, creates database connection
  // the user's db connection is passed to the beacon as mdb
  // follow with findUniqueBeacon
  public Beacon(String hostName, String dbName) {
    MongoClientURI uri = new MongoClientURI(hostName);
    mongoClient = new MongoClient(uri);
    db = mongoClient.getDatabase(dbName);
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");

    // set default values for data members
    creator = "";
    title = "";
    // default location is Hofstra!! :)
    location = new Point(new Position(-73.600491, 40.714087));
    startTime = new Date();
    endTime = new Date();
    range = 0.1;
    placeName = "";
    address = "";
    tags = new ArrayList<String>();
    notifiedCount = 0;
    notified = new ArrayList<String>();
  }

  // use when creating a new beacon
  // the user's db connection is passed to the beacon as mdb
  // follow by calling insert
  public Beacon(String hostURI, String dbName, String authorName, String beaconTitle, Double latCoord, Double longCoord,
                Long start, Long end, Double beaconRange, String pName, String beaconAddress, List<String> tagList) {
    // connect instance to database
    MongoClientURI uri = new MongoClientURI(hostURI);
    mongoClient = new MongoClient(uri);
    db = mongoClient.getDatabase(dbName);
    users = db.getCollection("users");
    beacons = db.getCollection("beacons");

    // initialize the fields for the beacon object
    creator = authorName;
    title = beaconTitle;
    location = new Point(new Position(longCoord, latCoord));
    startTime = new Date(start);
    endTime = new Date(end);
    range = beaconRange;
    placeName = pName;
    address = beaconAddress;
    tags = tagList;
    notifiedCount = 1; // creator is considered first notified
    ArrayList<String> nu = new ArrayList<String>();
    nu.add(creator);
    notified = nu;
  }

  // inserts the new user instance into MongoDB
  // returns true on successful insertion
  // returns false on unsuccessful (unsuccessful if user already has a beacon placed)
  public boolean insert() {
    boolean insert = true;

    // search for a beacon by the creator with an endTime greater than current time
    // limits users to one beacon at a time
    FindIterable<Document> fi = beacons.find(and(asList(
      eq("creator", this.creator),
      gt("endTime", new Date())
    )))
    .limit(1);

    // do not insert if a match is found
    if (fi.first() != null) {
      insert = false;
    }

    if (insert) {
      Document beacon = new Document("creator", this.creator)
                        .append("title", this.title)
                        .append("location", this.location)
                        .append("startTime", this.startTime)
                        .append("endTime", this.endTime)
                        .append("range", this.range)
                        .append("placeName", this.placeName)
                        .append("address", this.address)
                        .append("tags", this.tags)
                        .append("notifiedCount", this.notifiedCount)
                        .append("notified", this.notified);

      beacons.insertOne(beacon);
    }

    return insert;
  }

  // remove the user's beacon occurring at the specified time
  // returns true for successful deletion
  public boolean deleteBeacon(Long time) {
    // delete the beacon by the creator with an endTime greater than current time
    DeleteResult dr = beacons.deleteOne(and(asList(
      eq("creator", this.creator),
      gte("endTime", new Date(time)),
      lte("startTime", new Date(time))
    )));

    return (dr.getDeletedCount() > 0);
  }

  // deletes the current or future beacon for the specified user
  public boolean deleteNextBeacon(String creator) {
    DeleteResult dr = beacons.deleteOne(and(asList(
      eq("creator", creator),
      gt("endTime", new Date())
    )));

    return (dr.getDeletedCount() > 0);
  }

  // loads the beacon with the specified creator at the specified time
  // the time can be any time within the duration of the beacon
  // returns true if a beacon is found, false if not
  public boolean findBeacon(String beaconCreator, Long dateTime) {
    boolean found = false;

    FindIterable<Document> fi = beacons.find(and(asList(
      eq("creator", beaconCreator),
      lte("startTime", new Date(dateTime)),
      gte("endTime", new Date(dateTime))
    )))
    .limit(1);

    Document thisBeacon = fi.first();

    // if found, load the information into the Beacon instance
    if (thisBeacon != null) {
      found = true;

      this.creator = thisBeacon.getString("creator");
      this.title = thisBeacon.getString("title");
      // parse location document to get coordinates
      Document loc = thisBeacon.get("location", Document.class);
      ArrayList<Double> coords = loc.get("coordinates", ArrayList.class);
      this.location = new Point(new Position(coords.get(0), coords.get(1)));
      this.startTime = thisBeacon.get("startTime", Date.class);
      this.endTime = thisBeacon.get("endTime", Date.class);
      this.range = thisBeacon.getDouble("range");
      this.placeName = thisBeacon.getString("placeName");
      this.address = thisBeacon.getString("address");
      this.tags = thisBeacon.get("tags", ArrayList.class);
      this.notifiedCount = thisBeacon.getInteger("notifiedCount");
      this.notified = thisBeacon.get("notified", ArrayList.class);
    }

    return found;

  }

  // Data member accessor methods

  public String getCreator() {
    return this.creator;
  }

  public String getTitle() {
    return this.title;
  }

  public Point getLocation() {
    return this.location;
  }

  public Date getStartTime() {
    return this.startTime;
  }

  public Date getEndTime() {
    return this.endTime;
  }

  public Double getRange() {
    return this.range;
  }

  public String getPlaceName() {
    return this.placeName;
  }

  public String getAddress() {
    return this.address;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public Integer getNotifiedCount() {
    return this.notifiedCount;
  }

  public List<String> getNotifiedUsers() {
    return this.notified;
  }

  // Data member mutator methods

  public boolean updateTitle(String newTitle) {
    return updateBeaconField("title", this.title, newTitle);
  }

  public boolean updateLocation(Double latCoord, Double longCoord) {
    Point newLocation = new Point(new Position(longCoord, latCoord));
    return updateBeaconField("startTime", this.location, newLocation);
  }

  public boolean updateStartTime(Long start) {
    return updateBeaconField("startTime", this.startTime, new Date(start));
  }

  public boolean updateEndTime(Long end) {
    return updateBeaconField("endTime", this.endTime, new Date(end));
  }

  public boolean updateRange(Double newRange) {
    return updateBeaconField("range", this.range, newRange);
  }

  public boolean updatePlaceName(String newPlaceName) {
    return updateBeaconField("placeName", this.placeName, newPlaceName);
  }

  public boolean updateAddress(String newAddress) {
    return updateBeaconField("address", this.address, newAddress);
  }

  // adds new tags, deltes tags that already exist, supports mixed list
  public boolean updateTags(List<String> tagList) {
    List<String> addList = new ArrayList<String>();
    List<String> delList = new ArrayList<String>();

    // add new tags to the addList and the tags that already exist to the delList
    for (String tag: tagList) {
      if (!this.tags.contains(tag)) {
        addList.add(tag);
      } else {
        delList.add(tag);
      }
    }

    UpdateResult ur1 = beacons.updateOne(and(asList(
                                        eq("creator", this.creator),
                                        eq("endTime", this.endTime))),
                                        addEachToSet("tags", addList));

    UpdateResult ur2 = beacons.updateOne(and(asList(
                                        eq("creator", this.creator),
                                        eq("endTime", this.endTime))),
                                        pullAll("tags", delList));

     boolean completed = (ur1.getModifiedCount() + ur2.getModifiedCount() > 0);
     if (completed) {
       this.tags.addAll(addList);
       this.tags.removeAll(delList);
     }
     return completed;
  }

  public boolean updateNotifiedCount(Integer newCount) {
    return updateBeaconField("notifiedCount", this.notifiedCount, newCount);
  }

  public boolean addOneNotified(String newNotified) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                addToSet("notified", newNotified)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.notified.add(newNotified);
    }
    return completed;
  }

  public boolean addManyNotified(List<String> newNotifieds) {
    UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                addEachToSet("notified", newNotifieds)
    );
    boolean completed = (ur.getModifiedCount() > 0);
    if (completed) {
      this.notified.addAll(newNotifieds);
    }
    return completed;
  }

  // general method for simple beacon field updates
  public boolean updateBeaconField(String fieldName, Object field, Object newFieldValue) {
    boolean completed = true;
    try {
      UpdateResult ur = beacons.updateOne(
                                and(asList(eq("creator", this.creator),
                                           eq("endTime", this.endTime)
                                )),
                                set(fieldName, newFieldValue)
      );
      completed = (ur.getModifiedCount() > 0);
    } catch (MongoWriteException mwe) {
      completed = false;
    }
    if (completed) {
      field = newFieldValue;
    }
    return completed;
  }

  // Beacon search methods

  // input max number of beacons, user's coordinates, and proximity in miles
  // returns JSON formatted String of the form { beacons: [ <beacons> ]}
  // where <beacons> is a list of max beacons within distance of the user's coordinates
  public String findNearbyBeacons(int max, double latCoord, double longCoord, double distance) {
    // filter by the geoWithinCenterSphere filter
    // distance / 3963.2 converts distance to radians (3963.2 approximates Earth's radius)
    FindIterable<Document> iterable = beacons.find(geoWithinCenterSphere(
                                                    "location",
                                                    longCoord,
                                                    latCoord,
                                                    distance / 3963.2
                                                   ))
                                                   .limit(max);

    String result = iterableToJson("beacons", iterable);
    return result;
  }

  public String findNearbyBeaconsByTags(int max, double latCoord, double longCoord, double distance, List<String> tags) {
    // aggregate result Documents from beacons collection
    AggregateIterable<Document> beaconAggregation = beacons.aggregate(asList(
      // first match nearby beacons
      match(geoWithinCenterSphere("location", longCoord, latCoord, distance / 3963.2)),
      // only match beacons with the correct tags
      match(in("tags", tags)),
      // limit the aggregation to specified number of beacons
      limit(max)
    ));

    String result = iterableToJson("beacons", beaconAggregation);
    return result;
  }

  public String findNearbyBeaconsWithoutUser(int max, double latCoord, double longCoord, double distance, String user) {
    AggregateIterable<Document> beaconAggregation = beacons.aggregate(asList(
      // first match nearby beacons
      match(geoWithinCenterSphere("location", longCoord, latCoord, distance / 3963.2)),
      // only match beacons with the correct tags
      match(not(elemMatch("username", eq("username", user)))),
      // limit the aggregation to specified number of beacons
      limit(max)
    ));

    String result = iterableToJson("beacons", beaconAggregation);
    return result;
  }

  // close the connection to the database
  public void closeConnection() {
    mongoClient.close();
  }
}
