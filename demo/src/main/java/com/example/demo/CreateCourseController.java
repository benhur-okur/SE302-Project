package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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
    ViewCoursesController viewCoursesController;

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

        // Check for empty inputs
        if (courseName == null || courseName.isEmpty() || selectedLecturer == null
                || selectedStartTime == null || selectedDays.isEmpty()
                || selectedClassroom == null || selectedDuration == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input",
                    "Please fill in all fields before creating the course.");
            return;
        }

        // Check time boundaries
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(selectedStartTime, formatter);
        LocalTime endTime = startTime.plusHours(selectedDuration);

        // Check if course ends after allowed time
        LocalTime latestEndTime = LocalTime.parse("21:20", formatter);
        if (endTime.isAfter(latestEndTime)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Time",
                    "The course end time exceeds the limit of 19:30. Please adjust the start time or duration.");
            return;
        }

        // Check for time conflicts in selected classroom
        for (String selectedDay : selectedDays) {
            if (!isClassroomAvailable(selectedClassroom, selectedDay, startTime, endTime)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Time Conflict",
                        "There is already a course scheduled in " + selectedClassroom +
                                " on " + selectedDay + " between " + startTime + " and " + endTime);
                return;
            }
        }

        // If no conflicts, create the course
        String timeToStart = selectedDays.get(0) + " " + selectedStartTime;
        Course newCourse = new Course(
                courseName,
                timeToStart,
                selectedDuration,
                selectedLecturer
        );
        newCourse.setClassroomName(selectedClassroom);

        // Save to database
        CourseDataAccessObject.addSingleCourse(newCourse);
        AssignCourseClassroomDB.initializeAssigning(newCourse, selectedClassroom);

        // Update tables
        viewCoursesController.tableView.refresh();
        viewCoursesController.setTableView();

        // Close window
        Stage stage = (Stage) courseNameField.getScene().getWindow();
        stage.close();
    }

    private boolean isClassroomAvailable(String classroom, String day, LocalTime startTime, LocalTime endTime) {
        // Get all courses in the selected classroom
        ArrayList<Course> existingCourses = AssignCourseClassroomDB.getCourseNamesByClassroom(classroom);

        for (Course existingCourse : existingCourses) {
            // Only check courses on the same day
            if (existingCourse.getCourseDay().equals(day)) {
                LocalTime existingStartTime = existingCourse.getStartTime();
                LocalTime existingEndTime = existingCourse.getEndTime();

                // Check for time overlap
                if (startTime.isBefore(existingEndTime) && endTime.isAfter(existingStartTime)) {
                    return false; // Conflict found
                }
            }
        }
        return true; // No conflicts found
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    private void handleCancel() {
        Stage stage = (Stage) courseNameField.getScene().getWindow();
        stage.close();
    }

    public void setMainController(ViewCoursesController viewCoursesController) {
        this.viewCoursesController = viewCoursesController;
    }
    public boolean isAvailable(Course newCourse) {
        // Eğer öğrencinin katıldığı dersler listesi boşsa, öğrenci uygundur.

        for (Course existingCourse : MainScreen.courseList) {
            LocalTime existingStartTime = existingCourse.getStartTime();
            System.out.println(existingStartTime);
            LocalTime existingEndTime = existingCourse.getEndTime();
            System.out.println(existingEndTime);
            String existingCourseDay = existingCourse.getCourseDay();
            System.out.println(existingCourseDay);
            String newCourseDay = newCourse.getCourseDay();
            System.out.println(newCourseDay);
            LocalTime newStartTime = newCourse.getStartTime();
            System.out.println(newStartTime);
            LocalTime newEndTime = newCourse.getEndTime();
            System.out.println(newEndTime);

            // Aynı gün olup olmadığını kontrol et
            if (existingCourseDay.equals(newCourseDay)) {
                // Zamanların çakışıp çakışmadığını kontrol et
                if (newStartTime.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime)) {
                    return false; // Çakışma var, öğrenci bu derse katılamaz
                }
            }
        }
        return true; // Çakışma yok, öğrenci derse katılabilir
    }
}
