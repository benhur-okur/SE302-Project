package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.*;
import java.util.ArrayList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewCoursesController {

    @FXML
    public TableView<Course> tableView;
    @FXML
    private TableColumn<Course, String> courseID;
    @FXML
    private TableColumn<Course, String> timeToStart;
    @FXML
    private TableColumn<Course, String> duration;
    @FXML
    private TableColumn<Course, String> lecturerName;
    @FXML
    private TableColumn<String, String> assignedClassroom;
    @FXML
    private TableColumn<Course, Void> changeClassroom;
    @FXML
    private TableColumn<Course, Void> viewStudentsColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button createCourseButton;

    public ViewCoursesController viewCoursesController = this;

    public static Student selectedStudent;

    private ObservableList<Course> allCourses;

    private Course selectedCourse;
    public CreateCourseController createCourseController;

    public void initialize() {


        // Sütunları yapılandır
        courseID.setCellValueFactory(new PropertyValueFactory<>("courseID"));
        timeToStart.setCellValueFactory(new PropertyValueFactory<>("timeToStart"));
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        lecturerName.setCellValueFactory(new PropertyValueFactory<>("lecturerName"));
        assignedClassroom.setCellValueFactory(new PropertyValueFactory<>("classroomName"));


        setTableView();

        // "View Students" sütununu ekle
        addViewStudentsButton();

        addChangeClassroomButton();

        // Arama butonuna işlem bağla
        searchButton.setOnAction(event -> searchCourse());

        // Reset butonuna işlem bağla
        resetButton.setOnAction(event -> resetTable());


        tableView.setOnMouseClicked(this::handleCourseSelection);  // Course selection handler

    }

    @FXML
    private void openCreateCoursePage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreateCourse.fxml"));
        Parent root = fxmlLoader.load();

        createCourseController = fxmlLoader.getController();
        createCourseController.setMainController(this);


        //yeni stage oluştur ve .fxml'i göster
        Stage stage = new Stage();
        stage.setMinWidth(400);
        stage.setMinHeight(400);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Create Management");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
    public void setTableView () {

        ObservableList<Course> hypotethicalCourses = CourseDataAccessObject.getCoursesWithoutStudents();
        allCourses = AssignCourseClassroomDB.getCoursesWithAssignedClassrooms(hypotethicalCourses);
        //allCourses = CourseDataAccessObject.getCoursesWithoutStudents();
        tableView.setItems(allCourses);
        MainScreen.makeAssign();
        ArrayList<Course> arrayList = new ArrayList<>(MainScreen.courseList);
        ObservableList<Course> courseObservableList = FXCollections.observableList(arrayList);
        tableView.setItems(courseObservableList);

    }

    // Event handler for when a course is clicked
    @FXML
    private void handleCourseSelection(MouseEvent event) {
        // Get the selected course from the table
        selectedCourse = tableView.getSelectionModel().getSelectedItem();
        selectedCourse.setStudentNames(AttendenceDatabase.studentsOfSpecificCourse(selectedCourse));

        // Pass the selected course to StudentManagementController and close the window
        if (selectedCourse != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentManagement.fxml"));
                Parent root = loader.load();  // Ensure the controller is loaded

                StudentManagementController studentManagementController = loader.getController();

                studentManagementController.handleAddToCourse(selectedCourse); //TODO!!
                System.out.println(selectedCourse.getStudentNames());
                //CourseDataAccessObject.updateForAddingStudentToCourse(selectedCourse, selectedStudent);

                Stage stage = (Stage) tableView.getScene().getWindow();
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();  // Handle any exceptions that might occur during the loading of the FXML
            }
        }

    }

    private void addChangeClassroomButton() {
        Callback<TableColumn<Course, Void>, TableCell<Course, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Course, Void> call(final TableColumn<Course, Void> param) {
                return new TableCell<>() {
                    private final ChoiceBox<String> choiceBox = new ChoiceBox<>();
                    private final Button btn = new Button("Change");
                    private final HBox container = new HBox(10); // ChoiceBox ve Button için bir konteyner

                    {
                        // Classroom'ları ChoiceBox'a ekle
                        choiceBox.getItems().addAll(ClassroomDataAccessObject.getClassroomsNames());

                        // Butona tıklanınca sınıf değişimini yap
                        btn.setOnAction(event -> {
                            Course selectedCourse = getTableView().getItems().get(getIndex());
                            String selectedClassroom = choiceBox.getValue();
                            if (selectedClassroom != null) {
                                AssignCourseClassroomDB.updateClassroomForCourse(selectedCourse, selectedClassroom, viewCoursesController);
                            }
                        });

                        // HBox içine ChoiceBox ve Button ekle
                        container.getChildren().addAll(choiceBox, btn);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null); // Hücre boşsa hiçbir şey gösterme
                        } else {
                            setGraphic(container); // Hücre doluysa ChoiceBox ve Button'ı göster
                        }
                    }
                };
            }
        };

        changeClassroom.setCellFactory(cellFactory); // "Change Classroom" sütununu güncelle
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
        addViewStudentsButton(); // "View Students" butonlarını yeniden ekle

    }

    private void resetTable() {
        searchField.clear(); // Arama kutusunu temizle
        tableView.setItems(allCourses); // Tüm kursları tekrar yükle
        addViewStudentsButton(); // "View Students" butonlarını tekrar ekle
        setTableView();
    }


    private void addViewStudentsButton() {
        Callback<TableColumn<Course, Void>, TableCell<Course, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Course, Void> call(final TableColumn<Course, Void> param) {
                return new TableCell<>() {

                    private final Button btn = new Button("View Students");

                    {
                        btn.setOnAction(event -> {
                            Course selectedCourse = getTableView().getItems().get(getIndex());
                            showStudentsForCourse(selectedCourse);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null); // Boş hücrelerde buton gösterme
                        } else {
                            setGraphic(btn); // Hücrede butonu göster
                        }
                    }
                };
            }
        };

        viewStudentsColumn.setCellFactory(cellFactory);
    }

    private void showStudentsForCourse(Course course) {
        ArrayList<String> students = AttendenceDatabase.studentsOfSpecificCourse(course);
        System.out.println("Students in course " + course.getCourseID() + ": " + students);

        // Öğrencileri göstermek için ScrollPane içinde bir TextArea kullan
        StringBuilder studentsList = new StringBuilder();
        for (String student : students) {
            studentsList.append(student).append("\n");
        }

        TextArea studentsTextArea = new TextArea();
        studentsTextArea.setText(studentsList.toString());
        studentsTextArea.setWrapText(true);
        studentsTextArea.setEditable(false);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(studentsTextArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        setTableView();
        Stage studentsStage = new Stage();
        studentsStage.setTitle("Students in Course: " + course.getCourseID());
        Scene scene = new Scene(scrollPane, 400, 300);
        studentsStage.setScene(scene);
        studentsStage.show();
    }

}
