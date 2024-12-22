package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AssignCourseClassroomDB {
    public static void dropTable() {
        String sql = "DROP TABLE IF EXISTS Assign" ;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Assign" + " table has been dropped.");
        } catch (SQLException e) {
            System.out.println("Dropping table error: " + e.getMessage());
        }
    }

    public static void createTables() {

        //TODO Duplicate öğrenci probblem var - SOLVED
        String sql = """
            CREATE TABLE IF NOT EXISTS Assign (
                course_id TEXT NOT NULL ,
                classroom_name TEXT NOT NULL,
                FOREIGN KEY (course_id) REFERENCES Course(Course),
                FOREIGN KEY (classroom_name) REFERENCES Classroom(Classroom),
                UNIQUE (course_id)                               \s
                         );
        \s""";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Assign table has just created or it already exists.");
        } catch (SQLException e) {
            System.out.println("Creating table error: " + e.getMessage());
        }

    }

    public static void initializeAssigning (Course course, String classroom) {
        String courseName = course.getCourseID();
        String classroomName = classroom;

        String insertQuery = "INSERT OR IGNORE INTO Assign(course_id, classroom_name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, courseName);
            preparedStatement.setString(2, classroomName);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insertion error: " + e.getMessage(), e);
        }
    }
    public static ArrayList<Course> getCourseNamesByClassroom(String classroomName) {
        ArrayList<String> courseList = new ArrayList<>();

        // SQL sorgusu
        String query = "SELECT course_id FROM Assign WHERE classroom_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            // Parametreyi ayarla
            preparedStatement.setString(1, classroomName);

            // Sorguyu çalıştır ve sonuçları işle
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                courseList.add(resultSet.getString(1));
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while fetching courses: " + e.getMessage());
        }

        return getCourseByClassroom(courseList);
    }

    public static ArrayList<Course> getCourseByClassroom(ArrayList<String> courseNames) {
        ArrayList<Course> courseList = new ArrayList<>();



        for (String courseName : courseNames) {
            // SQL sorgusu
            String query = "SELECT * FROM Course WHERE Course = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                // Parametreyi ayarla
                preparedStatement.setString(1, courseName);

                // Sorguyu çalıştır ve sonuçları işle
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    courseList.add(new Course(resultSet.getString(1), resultSet.getString(2),
                            resultSet.getInt(3), resultSet.getString(4),
                            stringToArrayList(resultSet.getString(5))));
                }

            } catch (SQLException e) {
                System.out.println("An error occurred while fetching courses: " + e.getMessage());
            }
        }

        return courseList;
    }
    private static ArrayList<String> stringToArrayList(String students) {
        ArrayList<String> studentList = new ArrayList<>();
        if (students != null && !students.isEmpty()) {
            // Virgülle ayrılmış String'i parçalayarak bir ArrayList'e çevir
            studentList.addAll(Arrays.asList(students.split(",")));
        }
        return studentList;
    }
    public static ArrayList<VBox> getAssign (ArrayList<Course> courses) {
        ArrayList<Course> courseArrayList = new ArrayList<>();
        ArrayList<VBox> vBoxes = new ArrayList<>();
        //TODO ne döndürcez?
        courseArrayList = courses;

        for (Course course : courseArrayList) {
            // SQL sorgusu
            String query = "SELECT * FROM Assign WHERE course_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                // Parametreyi ayarla
                preparedStatement.setString(1, course.getCourseID());

                // Sorguyu çalıştır ve sonuçları işle
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {

                    Label courseLabel = new Label(resultSet.getString(1));
                    Label classroomLabel = new Label(resultSet.getString(2));
                    VBox vbox = new VBox();
                    vbox.setSpacing(10); // Etiketler arasında 10 piksel boşluk
                    vbox.getChildren().addAll(courseLabel, classroomLabel);
                    vBoxes.add(vbox);

                }

            } catch (SQLException e) {
                System.out.println("An error occurred while fetching courses: " + e.getMessage());
            }
        }

        return vBoxes;

    }
    public static ObservableList<Course> getCoursesWithAssignedClassrooms(ObservableList<Course> courses) {
        ObservableList<Course> courseList = FXCollections.observableList(courses);



        for (Course course : courseList) {
            // SQL sorgusu
            String query = "SELECT classroom_name FROM Assign WHERE course_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                // Parametreyi ayarla
                preparedStatement.setString(1, course.getCourseID());

                // Sorguyu çalıştır ve sonuçları işle
                ResultSet resultSet = preparedStatement.executeQuery();
                course.setAssignedClassroom(new Classroom(resultSet.getString(1),
                        ClassroomDataAccessObject.getCapacityWhereClassroomIs(resultSet.getString(1))));
                course.setEnrolledStudentsList(AttendenceDatabase.studentObjectsOfSpecificCourse(course));

            } catch (SQLException e) {
                System.out.println("An error occurred while fetching courses: " + e.getMessage());
                System.out.println("HATA!");
            }
        }

        return courseList;
    }

    public static void updateClassroomForCourse(Course course, String newClassroom, ViewCoursesController controller) {
        String sql = "UPDATE Assign SET classroom_name = ? WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newClassroom);
            pstmt.setString(2, course.getCourseID());
            pstmt.executeUpdate();

            course.setClassroomName(newClassroom);

            // Güncellenen kurs bilgilerini yenile
            controller.tableView.refresh();
        } catch (SQLException e) {
            System.out.println("Error updating classroom: " + e.getMessage());
        }
    }

    /*
    public static ObservableList<Course> getClassroomNamesByCourse(ArrayList<Course> courseList) {
        ArrayList<Classroom> classrooms = new ArrayList<>();
        ArrayList<String> classroomNames = new ArrayList<>();


        // SQL sorgusu
        String query = "SELECT classroom_name FROM Assign WHERE course_id = ?";

        for (Course course : courseList)
        {           try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                // Parametreyi ayarla
                preparedStatement.setString(1, course.getCourseID());

                // Sorguyu çalıştır ve sonuçları işle
                ResultSet resultSet = preparedStatement.executeQuery();
                classroomNames.add(resultSet.getString(1));


            } catch (SQLException e) {
                System.out.println("An error occurred while fetching courses: " + e.getMessage());
            }
        }

        String query2 = "SELECT * FROM Classroom WHERE Classroom = ?";


        int i = 0;
        for (String classroomName : classroomNames) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query2)) {

                // Parametreyi ayarla
                preparedStatement.setString(1, classroomName);

                // Sorguyu çalıştır ve sonuçları işle
                ResultSet resultSet = preparedStatement.executeQuery();
                Classroom newClassroom = new Classroom(resultSet.getString(1), resultSet.getInt(2));
                classrooms.add(newClassroom);

                courseList.get(i).setAssignedClassroom(newClassroom);

                i++;
            } catch (SQLException e) {
                System.out.println("An error occurred while fetching courses: " + e.getMessage());
            }
        }

        ObservableList<Course> observableList = FXCollections.observableArrayList(courseList);
        return observableList;
    }

     */


}
