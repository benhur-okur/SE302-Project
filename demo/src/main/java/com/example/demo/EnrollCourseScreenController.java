package com.example.demo;

import com.example.demo.Course;
import com.example.demo.CourseDataAccessObject;
import com.example.demo.Student;
import com.example.demo.StudentManagementController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class EnrollCourseScreenController {
    @FXML
    private TableView<Course> tableView;
    @FXML
    private TableColumn<Course, String> courseID;
    @FXML
    private TableColumn<Course, String> timeToStart;
    @FXML
    private TableColumn<Course, String> duration;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button resetButton;

    public static Student selectedStudent;

    private ObservableList<Course> allCourses;

    private Course selectedCourse;

    public void initialize() {

        courseID.setCellValueFactory(new PropertyValueFactory<>("courseID"));
        timeToStart.setCellValueFactory(new PropertyValueFactory<>("timeToStart"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));

        setTableView();

        // Arama butonuna işlem bağla
        searchButton.setOnAction(event -> searchCourse());

        // Reset butonuna işlem bağla
        resetButton.setOnAction(event -> resetTable());

        // Course selection handler
        tableView.setOnMouseClicked(event -> handleCourseSelection());
    }

    public void setTableView() {

        ObservableList<Course> hypotethicalCourses = CourseDataAccessObject.getCoursesWithoutStudents();
        allCourses = AssignCourseClassroomDB.getCoursesWithAssignedClassrooms(hypotethicalCourses);

        MainScreen.makeAssign();
        ArrayList<Course> arrayList = new ArrayList<>(MainScreen.courseList);
        ObservableList<Course> courseObservableList = FXCollections.observableList(arrayList);
        tableView.setItems(courseObservableList);
    }

    @FXML
    private void handleCourseSelection() {
        selectedCourse = tableView.getSelectionModel().getSelectedItem();

            if (selectedCourse != null) {
                selectedCourse.setStudentNames(AttendenceDatabase.studentsOfSpecificCourse(selectedCourse));

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentManagement.fxml"));
                    Parent root = loader.load();

                    StudentManagementController studentManagementController = loader.getController();
                    studentManagementController.handleAddToCourse(selectedCourse);
                    System.out.println(selectedCourse.getStudentNames());

                    Stage stage = (Stage) tableView.getScene().getWindow();
                    stage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    }

    private void searchCourse() {
        String searchQuery = searchField.getText();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            setTableView(); // Eğer arama kutusu boşsa tüm kursları göster
        } else {
            ObservableList<Course> filteredCourses = FXCollections.observableArrayList();
            for (Course course : allCourses) {
                if (course.getCourseID().equalsIgnoreCase(searchQuery)) {
                    filteredCourses.add(course);
                }
            }
            tableView.setItems(filteredCourses); // Filtrelenmiş kursları göster
        }
    }

    private void resetTable() {
        searchField.clear(); // Arama kutusunu temizle
        tableView.setItems(allCourses); // Tüm kursları tekrar yükle
        setTableView();
    }
}