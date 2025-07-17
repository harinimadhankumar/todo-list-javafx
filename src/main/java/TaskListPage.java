 import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;
public class TaskListPage {
    public static void display() {
        Stage window = new Stage();
        window.setTitle("Task List");

        // Table and list setup
        TableView<Task> table = new TableView<>();
        ObservableList<Task> tasks = FXCollections.observableArrayList();

        // Fetching from MongoDB
        for (Document doc : MongoDBHandler.getTasksCollection().find()) {
            Task task = new Task(
    doc.getString("title"),
    doc.getString("description"),
    doc.getString("priority"),
    doc.getString("dueDate"), // Extract due date from DB
    doc.getBoolean("completed", false)
);

            tasks.add(task);
        }
        // Table columns
        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
    
        TableColumn<Task, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Task, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Task, Boolean> completedCol = new TableColumn<>("Completed");
        completedCol.setCellValueFactory(new PropertyValueFactory<>("completed"));

        table.setItems(tasks);
        table.getColumns().addAll(titleCol, descCol, priorityCol, completedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Layout
        Label header = new Label("Your Tasks");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));
        topBox.getChildren().add(header);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));
        layout.setTop(topBox);
        layout.setCenter(table);

        // Scene setup
        Scene scene = new Scene(layout, 600, 400);
        window.setScene(scene);
        window.show();
    }
}
