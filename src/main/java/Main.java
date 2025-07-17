// [All your existing imports...]
import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.bson.Document;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import java.time.LocalDate;

public class Main extends Application {
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();
    private final TableView<Task> taskTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        showLoginWindow(primaryStage);
    }

    private void showLoginWindow(Stage stage) {
        Label userLabel = new Label("Username:");
        Label passLabel = new Label("Password:");
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        Button loginBtn = new Button("🔓 Login");
        Button registerBtn = new Button("📝 Register");

        GridPane loginGrid = new GridPane();
        loginGrid.setPadding(new Insets(20));
        loginGrid.setVgap(10);
        loginGrid.setHgap(10);
        loginGrid.setAlignment(Pos.CENTER);

        loginGrid.add(userLabel, 0, 0);
        loginGrid.add(userField, 1, 0);
        loginGrid.add(passLabel, 0, 1);
        loginGrid.add(passField, 1, 1);
        loginGrid.add(loginBtn, 1, 2);
        loginGrid.add(registerBtn, 1, 3);

        loginBtn.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            if (MongoDBHandler.authenticateUser(username, password)) {
                stage.close();
                showMainMenu();
                startReminderService();
            } else {
                showAlert("Invalid username or password");
            }
        });

        registerBtn.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            if (!username.isEmpty() && !password.isEmpty()) {
                MongoDBHandler.registerUser(username, password);
                showAlert("✅ User registered! Please login.");
            } else {
                showAlert("❗ Please enter both username and password");
            }
        });

        Scene scene = new Scene(loginGrid, 350, 250);
        stage.setScene(scene);
        stage.setTitle("Login - To-Do App");
        stage.show();
    }

    private void showMainMenu() {
        Stage stage = new Stage();
        Button addTaskBtn = new Button("➕ Add Task");
        Button viewTasksBtn = new Button("📋 View Tasks");

        VBox menu = new VBox(20, addTaskBtn, viewTasksBtn);
        menu.setPadding(new Insets(20));
        menu.setAlignment(Pos.CENTER);

        addTaskBtn.setOnAction(e -> openAddTaskWindow());
        viewTasksBtn.setOnAction(e -> openTaskListWindow());

        stage.setScene(new Scene(menu, 300, 200));
        stage.setTitle("To-Do List App");
        stage.show();
    }

    private void openAddTaskWindow() {
        Stage addStage = new Stage();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setAlignment(Pos.CENTER);

        TextField titleField = new TextField();
        TextField descField = new TextField();
        ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList("Low", "Medium", "High"));
        DatePicker dueDatePicker = new DatePicker();
        Button saveBtn = new Button("✅ Save Task");

        saveBtn.setOnAction(e -> {
            String title = titleField.getText();
            String desc = descField.getText();
            String priority = priorityBox.getValue();
            String dueDate = dueDatePicker.getValue() != null ? dueDatePicker.getValue().toString() : "";

            if (title.isEmpty() || priority == null || dueDate.isEmpty()) {
                showAlert("❗ Please fill all fields");
                return;
            }

            Task task = new Task(title, desc, priority, dueDate, false);
            MongoDBHandler.getTasksCollection().insertOne(task.toDocument());
            addStage.close();
            loadTasks();
        });

        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descField, 1, 1);
        grid.add(new Label("Priority:"), 0, 2); grid.add(priorityBox, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3); grid.add(dueDatePicker, 1, 3);
        grid.add(saveBtn, 0, 4, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        addStage.setScene(scene);
        addStage.setTitle("Add New Task");
        addStage.show();
    }

    private void openTaskListWindow() {
        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        TextField searchBar = new TextField();
        searchBar.setPromptText("🔍 Search by title or priority...");
        FilteredList<Task> filtered = new FilteredList<>(taskList, p -> true);

        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            filtered.setPredicate(task -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return task.getTitle().toLowerCase().contains(lower) ||
                        task.getPriority().toLowerCase().contains(lower);
            });
        });

        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Task, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Task, String> prioCol = new TableColumn<>("Priority");
        prioCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Task, String> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Task, Boolean> doneCol = new TableColumn<>("Completed");
        doneCol.setCellValueFactory(cellData -> cellData.getValue().completedProperty());
        doneCol.setCellFactory(CheckBoxTableCell.forTableColumn(doneCol));
        doneCol.setEditable(true);

        TableColumn<Task, Void> editCol = new TableColumn<>("✏️");
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            {
                btn.setOnAction(e -> openEditTaskWindow(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        TableColumn<Task, Void> deleteCol = new TableColumn<>("🗑️");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    MongoDBHandler.getTasksCollection().deleteOne(new Document("title", task.getTitle()));
                    loadTasks();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        taskTable.setEditable(true);
        taskTable.setItems(filtered);
        taskTable.getColumns().setAll(titleCol, descCol, prioCol, dueCol, doneCol, editCol, deleteCol);

        // ✅ Overdue row styling in red
        taskTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (task == null || empty) {
                    setStyle("");
                } else {
                    LocalDate dueDate = LocalDate.parse(task.getDueDate());
                    if (!task.isCompleted() && dueDate.isBefore(LocalDate.now())) {
                        setStyle("-fx-background-color: #ff9999;"); // red-ish background
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        Button refreshBtn = new Button("🔁 Refresh");
        refreshBtn.setOnAction(e -> loadTasks());

        VBox topBox = new VBox(10, new Label("📋 Task List"), searchBar, refreshBtn);
        root.setTop(topBox);
        root.setCenter(taskTable);

        loadTasks();
        stage.setScene(new Scene(root, 800, 450));
        stage.setTitle("Task List");
        stage.show();
    }

    private void openEditTaskWindow(Task task) {
        Stage editStage = new Stage();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setAlignment(Pos.CENTER);

        TextField titleField = new TextField(task.getTitle());
        TextField descField = new TextField(task.getDescription());
        ComboBox<String> priorityBox = new ComboBox<>(FXCollections.observableArrayList("Low", "Medium", "High"));
        priorityBox.setValue(task.getPriority());
        DatePicker dueDatePicker = new DatePicker(LocalDate.parse(task.getDueDate()));
        Button saveBtn = new Button("✅ Save Changes");

        saveBtn.setOnAction(e -> {
            String updatedTitle = titleField.getText();
            String updatedDesc = descField.getText();
            String updatedPriority = priorityBox.getValue();
            String updatedDueDate = dueDatePicker.getValue() != null ? dueDatePicker.getValue().toString() : "";

            if (updatedTitle.isEmpty() || updatedPriority == null || updatedDueDate.isEmpty()) {
                showAlert("❗ Please fill all fields");
                return;
            }

            MongoDBHandler.getTasksCollection().updateOne(
                    new Document("title", task.getTitle()),
                    new Document("$set", new Document("title", updatedTitle)
                            .append("description", updatedDesc)
                            .append("priority", updatedPriority)
                            .append("dueDate", updatedDueDate))
            );

            loadTasks();
            editStage.close();
        });

        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descField, 1, 1);
        grid.add(new Label("Priority:"), 0, 2); grid.add(priorityBox, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3); grid.add(dueDatePicker, 1, 3);
        grid.add(saveBtn, 0, 4, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        editStage.setScene(scene);
        editStage.setTitle("Edit Task");
        editStage.show();
    }

    private void loadTasks() {
        taskList.clear();
        MongoCollection<Document> col = MongoDBHandler.getTasksCollection();
        FindIterable<Document> docs = col.find();
        for (Document doc : docs) {
            Task task = Task.fromDocument(doc);
            task.completedProperty().addListener((obs, oldVal, newVal) -> {
                MongoDBHandler.getTasksCollection().updateOne(
                        new Document("title", task.getTitle()),
                        new Document("$set", new Document("completed", newVal))
                );
            });
            taskList.add(task);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void startReminderService() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            for (Task task : taskList) {
                if (task.getDueDate().equals(LocalDate.now().toString()) && !task.isCompleted()) {
                    showReminder(task);
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void showReminder(Task task) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reminder");
        alert.setHeaderText("🕒 Task Due Today");
        alert.setContentText("Reminder: \"" + task.getTitle() + "\" is due today!");
        alert.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
