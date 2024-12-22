package com.example.demo;

import eu.hansolo.fx.heatmap.OpacityDistribution;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class StudentManagementController {

    @FXML
    private Label studentNameLabel; // Label to display student name

    @FXML
    private GridPane scheduleGrid = new GridPane();
    @FXML

    public static Student student; // Instance variable to hold the student object

    @FXML
    private TableView<Course> courseTableView;

    @FXML
    private TableColumn<Course, String> CoursesColumn;

    @FXML
    private Button addStudentButton;

    @FXML
    private Button removeStudentButton; // Button to trigger adding student to course

    @FXML
    private Button transferStudentButton;

    @FXML
    private Button refreshButton;

    @FXML
    public void initialize() {
        // Initialize the TableColumn to display course IDs
        CoursesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseID()));

        setupLabels();
        populateCourses();

        // Ensure that the student object is set before calling any methods that depend on it
        if (student != null) {
            System.out.println(student);
            loadCourses();
        }
        if (student == null) {
            System.out.println("student NULL");
        }

        addStudentButton.setOnAction(event -> OpenViewCoursesAdd());

        removeStudentButton.setOnAction(event -> OpenViewCoursesRemove());

        transferStudentButton.setOnAction(event -> OpenViewCoursesTransfer());

        refreshButton.setOnAction(event -> refreshView());

    }

    @FXML
    private void refreshView() {
        if (student != null) {
            loadCourses();

            // Sadece ders içeriklerini temizle (VBox'ları)
            ObservableList<Node> children = scheduleGrid.getChildren();
            children.removeIf(node -> node instanceof VBox);

            // Sadece dersleri tekrar yerleştir
            populateCourses();

            System.out.println("View refreshed successfully!");
        } else {
            System.out.println("No student selected, nothing to refresh.");
        }

    }

    private void loadCourses() {
        // Fetch courses based on the student
        ArrayList<Course> courses = fetchCoursesFromDatabase();

        // Add courses to TableView
        ObservableList<Course> coursesData = FXCollections.observableArrayList(courses);
        courseTableView.setItems(coursesData);
    }

    private ArrayList<Course> fetchCoursesFromDatabase() {
        // Fetch courses based on the student name
        CourseDataAccessObject courseDAO = new CourseDataAccessObject();
        ArrayList<Course> courses = courseDAO.getCoursesBasedOnStudent(student.getName());
        return courses;
    }

    // Method to initialize the controller with the selected student
    public void setStudent(Student student) {
        this.student = student;
        studentNameLabel.setText(student.getName());

        // Now load the courses for this student
        loadCourses();
    }

    @FXML
    private void OpenViewCoursesAdd() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EnrollCourseScreen.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Pass the selected student to the new controller
        EnrollCourseScreenController.selectedStudent = student;

        // Create a new stage and show the FXML
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Select a Course to Add Please!");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    @FXML
    private void OpenViewCoursesRemove() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CourseTableView.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CourseTableViewController courseTableController = fxmlLoader.getController();
        courseTableController.setStudent(student);  // Pass the student to the controller

        // Create a new stage and show the FXML
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Select a Course to Remove Please!");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    @FXML
    private void OpenViewCoursesTransfer() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TransferScreen.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Pass the action type to the ViewCoursesController
        TransferScreenController controller = fxmlLoader.getController();
        controller.setStudent(student);
        controller.setStudentManagamentController(this);

        // Create a new stage and show the FXML
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Select a Enrolled Course and a Desired Course to Execute the Transfer Please!");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }


    // Method to handle adding the selected course to the student called from ViewCoursesController after selecting a course
    public void handleAddToCourse(Course selectedCourse) {
        if (selectedCourse != null && student != null) {
            Admin admin = new Admin();
            String result = admin.addStudentToCourse(selectedCourse, student);

            if (result.equals("SUCCESS")) {
                loadCourses();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Student successfully added to the course!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(result.replace("ERROR: ", ""));
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select a course and ensure a student is selected.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleRemoveStudentFromCourse(Course selectedCourse) {
        if (selectedCourse != null && student != null) {
            Admin admin = new Admin();
            String result = admin.removeStudentFromCourse(selectedCourse, student);

            if (result.equals("SUCCESS")) {
                loadCourses();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Student successfully removed from the course!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(result.replace("ERROR: ", ""));
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select a course and ensure a student is selected.");
            alert.showAndWait();
        }
    }

    public void handleTransferCourse(Course selectedCourse1, Course selectedCourse2) {
        if (selectedCourse1 != null && selectedCourse2 != null && student != null) {
            Admin admin = new Admin();
            String result = admin.transferStudentToAnotherCourse(selectedCourse1, selectedCourse2, student);

            if (result.equals("SUCCESS")) {
                loadCourses();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Student successfully transferred to the new course!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(result.replace("ERROR: ", ""));
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select both source and target courses and ensure a student is selected.");
            alert.showAndWait();
        }
    }

    // Günler ve saatler için grid başlıklarını ayarla
    private void setupLabels() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] timeSlots = generateTimeSlots("08:30", "19:30", 55);

        // Gün başlıklarını (Column 0 hariç) GridPane'e ekle
        for (int col = 1; col <= days.length; col++) {
            Label dayLabel = new Label(days[col - 1]);
            scheduleGrid.add(dayLabel, col, 0); // İlk satır, her bir gün için
        }

        // Saat başlıklarını (Row 0 hariç) GridPane'e ekle
        for (int row = 1; row <= timeSlots.length; row++) {
            Label timeLabel = new Label(timeSlots[row - 1]);
            scheduleGrid.add(timeLabel, 0, row); // İlk sütun, her bir saat dilimi için
        }
    }

    // Dersleri GridPane'e ekle
    private void populateCourses() {
        if (student == null) {
            System.out.println("No lecturer selected.");
            return;
        }

        // Veritabanından dersleri al
        ArrayList<VBox> vBoxes = AssignCourseClassroomDB.getAssign(CourseDataAccessObject.getCoursesBasedOnStudent(student.getName()));

        for (VBox originalVBox : vBoxes) {
            Course currentCourse;
            Label courseIDLabel = (Label) originalVBox.getChildren().get(0);
            String courseID = courseIDLabel.getText();
            currentCourse = CourseDataAccessObject.getCourseByCourseID(courseID);

            int durationOfCurrentCourse = currentCourse.getDuration();
            int col = getDayColumnIndex(currentCourse.getCourseDay());
            int startRow = getTimeRowIndex(currentCourse.getStartTime().toString());

            for (int i = 0; i < durationOfCurrentCourse; i++) {
                // Orijinal VBox'ı kopyalayarak yeni bir VBox oluştur
                VBox vboxCopy = createVBoxCopy(originalVBox);

                // Yeni VBox'ı grid'e ekle
                scheduleGrid.add(vboxCopy, col, startRow + i);
            }
        }
    }

    /**
     * Mevcut bir VBox'ın içeriğini kopyalar ve yeni bir VBox oluşturur.
     */
    private VBox createVBoxCopy(VBox originalVBox) {
        VBox vboxCopy = new VBox();
        vboxCopy.setSpacing(originalVBox.getSpacing());
        for (Node child : originalVBox.getChildren()) {
            if (child instanceof Label) {
                Label originalLabel = (Label) child;
                Label labelCopy = new Label(originalLabel.getText());
                labelCopy.setStyle(originalLabel.getStyle());
                vboxCopy.getChildren().add(labelCopy);
            }
        }
        return vboxCopy;
    }


    // Haftanın günleri için sütun indekslerini döndür
    private static int getDayColumnIndex(String day) {
        switch (day) {
            case "Monday": return 1;
            case "Tuesday": return 2;
            case "Wednesday": return 3;
            case "Thursday": return 4;
            case "Friday": return 5;
            case "Saturday": return 6;
            case "Sunday": return 7;
            default: return -1;
        }
    }

    // Saat dilimleri için satır indekslerini döndür
    private static int getTimeRowIndex(String time) {
        String[] timeSlots = generateTimeSlots("08:30", "19:30", 55);
        for (int i = 0; i < timeSlots.length; i++) {
            if (timeSlots[i].equals(time)) {
                return i + 1; // İlk satır başlık için ayrılmış
            }
        }
        return -1;
    }

    // Belirli bir başlangıç saatinden itibaren zaman dilimlerini oluştur
    private static String[] generateTimeSlots(String startTime, String endTime, int intervalMinutes) {
        ArrayList<String> timeSlots = new ArrayList<>();
        java.time.LocalTime start = java.time.LocalTime.parse(startTime);
        java.time.LocalTime end = java.time.LocalTime.parse(endTime);

        while (!start.isAfter(end)) {
            timeSlots.add(start.toString());
            start = start.plusMinutes(intervalMinutes);
        }

        return timeSlots.toArray(new String[0]);
    }

}
