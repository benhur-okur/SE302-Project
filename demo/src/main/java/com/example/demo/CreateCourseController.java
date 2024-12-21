package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CreateCourseController {

    @FXML
    private TextField courseNameField;
    @FXML
    private ListView<String> lecturerListView;
    @FXML
    private ListView<String> startTimeListView;
    @FXML
    private ListView<String> dayListView;
    @FXML
    private ListView<String> classroomListView;
    @FXML
    private ListView<Integer> durationListView;

    private ObservableList<String> availableLecturers;
    private ObservableList<String> availableStartTimes;
    private ObservableList<String> availableDays;
    private ObservableList<String> availableClassrooms;
    private ObservableList<Integer> availableDurations;

    public void initialize() {
        // Load lecturers from the database
        ObservableList<String> getLecturers = FXCollections.observableArrayList(CourseDataAccessObject.getLecturers());
        availableLecturers = FXCollections.observableArrayList(getLecturers);
        lecturerListView.setItems(availableLecturers);

        // Generate 55-minute time slots
        availableStartTimes = FXCollections.observableArrayList(generateTimeSlots("08:30", "19:30", 55));
        startTimeListView.setItems(availableStartTimes);

        // Initialize available days
        availableDays = FXCollections.observableArrayList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        dayListView.setItems(availableDays);
        dayListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Initialize available classrooms
        ObservableList<String> classroomsNames = FXCollections.observableArrayList(ClassroomDataAccessObject.getClassroomsNames());
        availableClassrooms = FXCollections.observableArrayList(classroomsNames);
        classroomListView.setItems(availableClassrooms);

        // Initialize available durations
        availableDurations = FXCollections.observableArrayList(1, 2, 3, 4, 5);
        durationListView.setItems(availableDurations);
    }

    private String[] generateTimeSlots(String start, String end, int intervalMinutes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(start, formatter);
        LocalTime endTime = LocalTime.parse(end, formatter);

        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        while (!startTime.isAfter(endTime)) {
            timeSlots.add(startTime.format(formatter));
            startTime = startTime.plusMinutes(intervalMinutes);
        }

        return timeSlots.toArray(new String[0]);
    }

    @FXML
    private void handleCreateCourse() {
        String courseName = courseNameField.getText();
        String selectedLecturer = lecturerListView.getSelectionModel().getSelectedItem();
        String selectedStartTime = startTimeListView.getSelectionModel().getSelectedItem();
        ObservableList<String> selectedDays = dayListView.getSelectionModel().getSelectedItems();
        String selectedClassroom = classroomListView.getSelectionModel().getSelectedItem();
        Integer selectedDuration = durationListView.getSelectionModel().getSelectedItem();

        // Girdilerin boş olup olmadığını kontrol et
        if (courseName == null || courseName.isEmpty() || selectedLecturer == null
                || selectedStartTime == null || selectedDays.isEmpty()
                || selectedClassroom == null || selectedDuration == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Please fill in all fields before creating the course.");
            alert.showAndWait();
            return;
        }

        // Gün ve saat bilgisini birleştir
        String timeToStart = selectedDays.get(0) + " " + selectedStartTime;

        // Yeni bir Course nesnesi oluştur
        Course newCourse = new Course(
                courseName,          // courseID
                timeToStart,         // timeToStart
                selectedDuration,    // duration
                selectedLecturer     // lecturerName
        );

        // Classroom'u ata (varsayılan olarak sadece ismi kaydediyoruz)
        newCourse.setClassroomName(selectedClassroom);

        // Test için log yazdır
        System.out.println("New Course Created:");
        System.out.println("Course ID: " + newCourse.getCourseID());
        System.out.println("Time to Start: " + newCourse.getTimeToStart());
        System.out.println("Duration: " + newCourse.getDuration() + " hours");
        System.out.println("Lecturer: " + newCourse.getLecturerName());
        System.out.println("Classroom: " + newCourse.getClassroomName());

        CourseDataAccessObject.addSingleCourse(newCourse);
        AttendenceDatabase.addAttendancesForNewCourse(newCourse);
        

        // Pencereyi kapat
        Stage stage = (Stage) courseNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) courseNameField.getScene().getWindow();
        stage.close();
    }
}
