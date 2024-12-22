package com.example.demo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class CourseTableViewController {


    @FXML
    private TableView<Course> courseTableView;

    @FXML
    private TableColumn<Course, String> CoursesColumn;

    public Course selectedCourse;

    public static Student student; // Instance variable to hold the student object



    @FXML
    public void initialize() {

        // Initialize the TableColumn to display course IDs
        CoursesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseID()));

        if (student != null) {
            loadCourses();
        }
        if (student == null) {
            //System.out.println("student NULL");
        }

        courseTableView.setOnMouseClicked(event -> handleCourseSelection());

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
        return CourseDataAccessObject.getCoursesBasedOnStudent(student.getName());

    }

    // Set the student object for this controller
    public void setStudent(Student student) {
        this.student = student;  // Assign the student to the class variable
        loadCourses();  // Reload courses based on the student
    }

    @FXML
    private void handleCourseSelection() {
        selectedCourse = courseTableView.getSelectionModel().getSelectedItem();

        if (selectedCourse != null) {
            selectedCourse.setStudentNames(AttendenceDatabase.studentsOfSpecificCourse(selectedCourse));

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentManagement.fxml"));
                Parent root = loader.load();

                StudentManagementController studentManagementController = loader.getController();
                studentManagementController.handleRemoveStudentFromCourse(selectedCourse);

                Stage stage = (Stage) courseTableView.getScene().getWindow();
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
