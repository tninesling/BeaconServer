package helpers;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.bson.Document;

/**
 * A class to create the connection to a MongoDB instance. The class has static
 * member for the MongoClient and MongoDatabase so the same client is used
 * across the application. One MongoClient contains a pool of connections
 * so only one should be used across a cluster.
 */
public class MongoConnection {
  private static MongoClient mongoClient = null;
  private static MongoDatabase database = null;

  public MongoConnection() {}

  /**
   * Sets the MongoClient and the MongoDatabase being used by the application.
   *
   * @param connectionUri - A string "mongodb://<hostname>" to be passed to the
   *                        MongoClients.create() method
   * @param databaseName  - The name of the database being used by the application
   */
  public MongoConnection(String connectionUri, String databaseName) {
    mongoClient = MongoClients.create(connectionUri);
    database = mongoClient.getDatabase("databaseName");
  }

  /**
   * Gets a collection from the database which must already be set. To set the
   * database, use the specific constructor.
   *
   * @param collectionName - The name of the collection to be fetched
   * @return A MongoCollection with name collectionName if database is set,
   *         otherwise null
   */
  public MongoCollection<Document> getCollection(String collectionName) {
    if (database == null) {
      return null;
    } else {
      return database.getCollection(collectionName);
    }
  }

  /**
   * Closes the connection to the MongoClient to clean up resources
   */
  public void closeConnection() {
    mongoClient.close();
  }
}
