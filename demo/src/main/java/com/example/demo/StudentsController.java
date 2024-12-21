package com.example.demo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;

public class StudentsController {

    @FXML
    private TableView<Student> studentTableView;

    @FXML
    private TableColumn<Student, String> studentNameColumn;

    @FXML
    private TableColumn<Student, Void> viewStudentsColumn;

    private Student selectedStudent;  // Instance variable to hold the selected student

    @FXML
    private TextField searchField; // Arama kutusu
    @FXML
    private Button searchButton;  // Arama butonu
    @FXML
    private Button resetButton; //reset Butonu

    public void initialize() {
        studentNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        // Fetch students from database
        ArrayList<Student> students = fetchStudentsFromDatabase();

        // Add students to TableView
        ObservableList<Student> studentData = FXCollections.observableArrayList(students);
        studentTableView.setItems(studentData);

        // Arama butonuna işlem bağla
        searchButton.setOnAction(event -> searchStudent());

        // Reset butonuna işlem bağla
        resetButton.setOnAction(event -> resetTable());

        // View Students butonunu ekle
        addViewStudentsButton();
    }

    private ArrayList<Student> fetchStudentsFromDatabase() {
        // Fetch all students from the database using the CourseDataAccessObject
        ArrayList<String> studentNames = CourseDataAccessObject.getAllStudents();

        // Convert the list of student names into Student objects
        ArrayList<Student> students = new ArrayList<>();
        for (String studentName : studentNames) {
            students.add(new Student(studentName)); // Create Student object for each student
        }

        return students;
    }

    private void addViewStudentsButton() {
        Callback<TableColumn<Student, Void>, TableCell<Student, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Student, Void> call(final TableColumn<Student, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("View Student");

                    {
                        btn.setOnAction(event -> {
                            Student student = getTableView().getItems().get(getIndex());
                            // HandleStudentSelection'daki işlemleri gerçekleştir
                            student.getCourses().addAll(CourseDataAccessObject.getCoursesBasedOnStudent(student.getName()));
                            StudentManagementController.student = student;

                            try {
                                // Load the StudentManagement.fxml file
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentManagement.fxml"));
                                Parent root = loader.load();

                                // Get the controller of the StudentManagement.fxml
                                StudentManagementController controller = loader.getController();
                                controller.setStudent(student); // Pass the selected student to the controller

                                // Create a new stage (window)
                                Stage stage = new Stage();
                                stage.setTitle("Student Management - " + student.getName());
                                stage.initModality(Modality.WINDOW_MODAL);
                                stage.setScene(new Scene(root));

                                // Show the new stage
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };

        viewStudentsColumn.setCellFactory(cellFactory);
    }

    private void searchStudent() {
        String searchQuery = searchField.getText();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            studentTableView.setItems(studentTableView.getItems()); // If the search field is empty, show all students
            return;
        }

        ObservableList<Student> filteredStudents = FXCollections.observableArrayList();

        for (Student student : studentTableView.getItems()) {
            if (student.getName().equalsIgnoreCase(searchQuery)) {
                filteredStudents.add(student);
            }
        }

        studentTableView.setItems(filteredStudents);
    }

    private void resetTable() {
        searchField.clear(); // Clear the search field
        studentTableView.setItems(FXCollections.observableArrayList(fetchStudentsFromDatabase())); // Reload all students
    }

}