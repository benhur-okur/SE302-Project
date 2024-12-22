package com.example.demo;

import java.io.IOException;
import java.util.ArrayList;

public class BenhurTest {
    public static void main(String[] args) throws IOException {
        CSV_Reader csv = new CSV_Reader();


        CourseDataAccessObject courseDataAccessObject = new CourseDataAccessObject();
        courseDataAccessObject.createTable();
        courseDataAccessObject.addCourse(csv.readCourses());
        courseDataAccessObject.getCourses();

        AssignCourseClassroomDB.dropTable();
        AssignCourseClassroomDB.createTables();
        MainScreen.makeAssign();






        AttendenceDatabase.dropTables();
        AttendenceDatabase.createTables();
        AttendenceDatabase.addStudentsFromCSV();
        AttendenceDatabase.addAttendancesWithInitialDatas();











     }
}
