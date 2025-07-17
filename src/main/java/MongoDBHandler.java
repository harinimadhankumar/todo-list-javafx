import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBHandler {
    private static final String URL = "mongodb://localhost:27017";
    private static final String DB_NAME = "todo_db";
    private static MongoDatabase database;

    static {
        MongoClient client = MongoClients.create(URL);
        database = client.getDatabase(DB_NAME);
    }

    public static MongoCollection<Document> getTasksCollection() {
        return database.getCollection("tasks");
    }

    public static MongoCollection<Document> getUsersCollection() {
        return database.getCollection("users");
    }

    // 🔐 Authenticate user
    public static boolean authenticateUser(String username, String password) {
        Document query = new Document("username", username).append("password", password);
        Document user = getUsersCollection().find(query).first();
        return user != null;
    }

    // 📝 Register new user
    public static void registerUser(String username, String password) {
        Document existing = getUsersCollection().find(new Document("username", username)).first();
        if (existing == null) {
            Document newUser = new Document("username", username).append("password", password);
            getUsersCollection().insertOne(newUser);
        }
    }
}
