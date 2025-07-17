import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.bson.Document;
import java.time.LocalDate;  // <-- Import added for LocalDate

public class Task {
    private final SimpleStringProperty title;
    private final SimpleStringProperty description;
    private final SimpleStringProperty priority;
    private final SimpleStringProperty dueDate;
    private final SimpleBooleanProperty completed;

    public Task(String title, String description, String priority, String dueDate, boolean completed) {
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.priority = new SimpleStringProperty(priority);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.completed = new SimpleBooleanProperty(completed);
    }

    public String getTitle() {
        return title.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getPriority() {
        return priority.get();
    }

    public String getDueDate() {
        return dueDate.get();
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean value) {
        completed.set(value);
    }

    public SimpleBooleanProperty completedProperty() {
        return completed;
    }

    public Document toDocument() {
        return new Document("title", getTitle())
                .append("description", getDescription())
                .append("priority", getPriority())
                .append("dueDate", getDueDate())
                .append("completed", isCompleted());
    }

    public boolean isOverdue() {
        String dueDateStr = getDueDate();  // get dueDate as String
        if (dueDateStr == null || dueDateStr.isEmpty()) return false;
        LocalDate due = LocalDate.parse(dueDateStr);
        // Use isCompleted() to get boolean, not the property object
        return !isCompleted() && due.isBefore(LocalDate.now());
    }

    public static Task fromDocument(Document doc) {
        return new Task(
                doc.getString("title"),
                doc.getString("description"),
                doc.getString("priority"),
                doc.getString("dueDate"),
                doc.getBoolean("completed", false)
        );
    }
}
