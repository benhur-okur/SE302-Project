package com.example.demo;

import eu.hansolo.fx.heatmap.OpacityDistribution;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
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

        removeStudentButton.setOnAction(event -> handleRemoveStudentFromCourse());

        transferStudentButton.setOnAction(event -> OpenViewCoursesTransfer());

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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ViewCourses.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Pass the action type to the ViewCoursesController
        ViewCoursesController controller = fxmlLoader.getController();

        // Create a new stage and show the FXML
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Select a Course Please!");
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
        stage.setTitle("Select two Courses Please!");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }


    // Method to handle adding the selected course to the student called from ViewCoursesController after selecting a course
    public void handleAddToCourse(Course selectedCourse) {
        if (selectedCourse != null && student != null) {
            // Use the Admin class to add the student to the selected course
            Admin admin = new Admin();
            admin.addStudentToCourse(selectedCourse, student);

            // Reload the courses to reflect the changes in the TableView
            loadCourses();
            System.out.println("Student added to the course!");
        } else {
            System.out.println("Please select a course and ensure a student is selected.");
        }
    }

    @FXML
    private void handleRemoveStudentFromCourse() {
        // Open the CourseTableView page
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CourseTableView.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get the CourseTableViewController instance and pass the student
        CourseTableViewController courseTableController = fxmlLoader.getController();
        courseTableController.setStudent(student);  // Pass the student to the controller

        // Create and show the new stage with the CourseTableView
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Select a Course to Remove");
        stage.setScene(new Scene(root));
        stage.showAndWait();

        // Get the selected course from the CourseTableViewController
        Course selectedCourse = courseTableController.getSelectedCourse();

        if (selectedCourse != null && student != null) {
            // Admin class instance to manage course enrollment
            Admin admin = new Admin();

            // Remove the student from the selected course
            admin.removeStudentFromCourse(selectedCourse, student);

            // Reload the courses to reflect the changes in the TableView
            loadCourses();
            System.out.println("Student removed from the course!");
        } else {
            System.out.println("Please select a course and ensure a student is selected.");
        }
    }

    public void handleTransferCourse(Course selectedCourse1, Course selectedCourse2) {
        if (selectedCourse1 != null && selectedCourse2 != null && student != null) {
            // Use the Admin class to add the student to the selected course
            Admin admin = new Admin();
            admin.transferStudentToAnotherCourse(selectedCourse1, selectedCourse2, student);

            // Reload the courses to reflect the changes in the TableView
            loadCourses();
            System.out.println("Student made the transfer!");
        } else {
            System.out.println("Please courses and ensure a student is selected.");
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
        //O Classroom'un course'larını aldık!

        for (VBox vbox : vBoxes) {
            Course currentCourse;
            Label courseIDLabel = (Label) vbox.getChildren().get(0);
            String courseID = courseIDLabel.getText();
            currentCourse = CourseDataAccessObject.getCourseByCourseID(courseID);


            int col = getDayColumnIndex(currentCourse.getCourseDay());
            int row = getTimeRowIndex(currentCourse.getStartTime().toString());

            /*
            Label courseLabel = new Label(course.getCourseID());
            Label classroomLabel = new Label(course.getAssignedClassroom().getClassroomName());
            VBox vbox = new VBox();
            vbox.setSpacing(10); // Etiketler arasında 10 piksel boşluk
            vbox.getChildren().addAll(courseLabel, classroomLabel);

             */
            scheduleGrid.add(vbox, col, row);

        }

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
