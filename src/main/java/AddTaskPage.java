// AddTaskPage.java
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

public class AddTaskPage {

    public static void display() {
        Stage window = new Stage();
        window.setTitle("➕ Add New Task");

        // Input fields
        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter task title");

        Label descLabel = new Label("Description:");
        TextField descField = new TextField();
        descField.setPromptText("Enter task description");

        Label priorityLabel = new Label("Priority:");
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setPromptText("Select priority");

        Label dateLabel = new Label("Due Date:");
        DatePicker dueDatePicker = new DatePicker();

        Label message = new Label();

        Button saveBtn = new Button("✅ Save Task");
        saveBtn.setOnAction(e -> {
            String title = titleField.getText();
            String desc = descField.getText();
            String priority = priorityBox.getValue();
            String dueDate = (dueDatePicker.getValue() != null) ? dueDatePicker.getValue().toString() : "";

            if (title.isEmpty() || priority == null || dueDate.isEmpty()) {
                message.setText("❗ Please fill in all fields.");
                return;
            }

            Document task = new Document("title", title)
                    .append("description", desc)
                    .append("priority", priority)
                    .append("dueDate", dueDate)
                    .append("completed", false);

            MongoDBHandler.getTasksCollection().insertOne(task);
            message.setText("✅ Task Added Successfully!");

            // Clear fields
            titleField.clear();
            descField.clear();
            priorityBox.setValue(null);
            dueDatePicker.setValue(null);
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(
                new Label("📝 Add New Task"),
                titleLabel, titleField,
                descLabel, descField,
                priorityLabel, priorityBox,
                dateLabel, dueDatePicker,
                saveBtn, message
        );

        Scene scene = new Scene(layout, 400, 450);
        window.setScene(scene);
        window.show();
    }
}
