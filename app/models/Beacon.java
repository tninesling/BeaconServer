package models;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static java.util.Arrays.asList;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.bson.Document;
import helpers.Callbacks;
import helpers.Callbacks;
import helpers.MongoConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Beacon {
  private static MongoConnection conn;
  private static MongoCollection<Document> beacons;
  private static MongoCollection<Document> users;
  private String creator;
  private String title;
  private Point location;
  private Date startTime;
  private Date endTime;
  private Double range;
  private String placeName;
  private String address;
  private List<String> tags;
  private Integer notifiedCount;
  private List<String> notifiedUsers;

  /**
   * This is the default constructor for Beacon objects. It constructs a Beacon
   * with default values for all Beacon data members. It is meant to be followed
   * by a find method that constructs a Beacon from the database, so the
   * connections to the database are set via the MongoConnection class.
   *
   * @param hostUri - The URI string for connecting to the mongo server with
   *                  format mongodb://<host ip>
   * @param databaseName - The name of the database being connected to
   */
  public Beacon(String hostUri, String databaseName) {
    // connect instance to database
    conn = new MongoConnection(hostUri, databaseName);
    beacons = conn.getCollection("beacons");
    users = conn.getCollection("users");

    // set default values for data members
    creator = new String();
    title = new String();
    // default location is Hofstra!! :)
    location = new Point(new Position(-73.600491, 40.714087));
    startTime = new Date();
    endTime = new Date();
    range = new Double(0.1);
    placeName = new String();
    address = new String();
    tags = new ArrayList<String>();
    notifiedCount = new Integer(0);
    notifiedUsers = new ArrayList<String>();
  }

  public Beacon(String hostUri, String databaseName, String creatorName,
                String beaconTitle, Double latCoord, Double lonCoord,
                Long start, Long end, Double beaconRange, String pName,
                String beaconAddress, List<String> tagList) {

    // connect instance to database
    conn = new MongoConnection(hostUri, databaseName);
    beacons = conn.getCollection("beacons");
    users = conn.getCollection("users");

    // initialize the fields for the beacon object
    creator = creatorName;
    title = beaconTitle;
    location = new Point(new Position(lonCoord, latCoord));
    startTime = new Date(start);
    endTime = new Date(end);
    range = beaconRange;
    placeName = pName;
    address = beaconAddress;
    tags = tagList;
    notifiedCount = 1; // creator is considered first notified
    notifiedUsers = new ArrayList<String>();
    notifiedUsers.add(creator);
  }

  /**
   * This function inserts a unique Beacon into the database. A Beacon will not
   * be inserted if the creator of this Beacon has a current or future Beacon set.
   *
   */
  public boolean insert() {
    boolean inserted = false;

    try {
      beacons.find(and(asList(eq("creator", this.creator), gt("endTime", new Date()))))
             .first(new SingleResultCallback<Document>() {
               @Override
               public void onResult(final Document document, final Throwable t) {
                 /*
                  *  if an exception was thrown, print stack trace and
                  *  set currentBeaconExists to true so insert does not occur
                  */
                 if (!t.equals(null)) {
                   throw t;
                 } else if (!document.equals(null)) {
                   throw new Exception("Current Beacon already exists");
                 } else {
                   System.out.println("Current Beacon does not exists. Perform insert");
                 }
               }
             });
    } catch (Exception e) { return false; }

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
                      .append("notifiedUsers", this.notifiedUsers);

    beacons.insertOne(beacon, new SingleResultCallback<Void>() {
      @Override
      public void onResult(final Void result, final Throwable t) {
        // if an exception was thrown on insert, set inserted to false
        if (!t.equals(null)) {
          inserted = false;
          t.printStackTrace();
        } else {
          inserted = true;
        }
      }
    });

    return inserted;
  }
}
