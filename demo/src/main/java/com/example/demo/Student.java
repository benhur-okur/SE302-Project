package com.example.demo;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Student extends Person{
    private static Map<String, Student> studentsByName = new HashMap<>();
    private Map<Course, Integer> absenceCountByCourse = new HashMap<>();  // Her dersin devamsızlık sayısı

    public Student(String name) {
        super(name);
    }

    public static Student findStudentByName(String name) {
        // Eğer öğrenci varsa, döndür
        if (studentsByName.containsKey(name)) {
            return studentsByName.get(name);
        }

        // Aksi takdirde yeni bir öğrenci oluştur ve map'e ekle
        Student student = new Student(name);
        studentsByName.put(name, student);
        return student;
    }

    public int getAbsenceCountForCourse(Course course) throws SQLException {
        //TODO SQL'dan çekilcek veri - SOLVED bi kontrol edin doğru mu yaptım - benhur
        //return absenceCountByCourse.getOrDefault(course, 0);
        return AttendenceDatabase.getAbsenceCount(course, this);
    }

    public void incrementAbsenteeismForCourse(Course course) {
        int currentCount = absenceCountByCourse.getOrDefault(course, 0);
        absenceCountByCourse.put(course, currentCount + 1);
        //AttendenceDatabase.incrementAbsenceCount(course, this); // TODO ADDED BY BENHUR - NOT böyle bir method var AttendanceDatabaase'de ilaydaya sor!
    }

    public void markAttendanceForCourse(Course course, boolean isPresent) {
        Attendance attendance = new Attendance(isPresent, course.getStartTime(), course.getCourseDay(), this, course);
        if (!isPresent) {
            incrementAbsenteeismForCourse(course);  // Devamsızlık olduğunda sayıyı artır
        }
        course.getAttendanceRecordList().add(attendance);  // Attendance kaydını derse ekle
    }


    public boolean isAvailable(Course newCourse) {
        // Debug çıktıları ekleyelim
        System.out.println("\n=== isAvailable Check Started ===");
        System.out.println("Checking availability for course: " + newCourse.getCourseID());
        System.out.println("New course day: " + newCourse.getCourseDay());
        System.out.println("New course time: " + newCourse.getStartTime() + " - " + newCourse.getEndTime());

        // Eğer öğrencinin katıldığı dersler listesi boşsa, öğrenci uygundur.
        if (this.getCourses() == null || this.getCourses().isEmpty()) {
            System.out.println("Student has no courses, available!");
            return true;
        }

        for (Course existingCourse : this.getCourses()) {
            System.out.println("\nChecking against existing course: " + existingCourse.getCourseID());
            System.out.println("Existing course day: " + existingCourse.getCourseDay());
            System.out.println("Existing course time: " + existingCourse.getStartTime() + " - " + existingCourse.getEndTime());

            LocalTime existingStartTime = existingCourse.getStartTime();
            LocalTime existingEndTime = existingCourse.getEndTime();
            String existingCourseDay = existingCourse.getCourseDay();
            String newCourseDay = newCourse.getCourseDay();
            LocalTime newStartTime = newCourse.getStartTime();
            LocalTime newEndTime = newCourse.getEndTime();

            // Aynı gün olup olmadığını kontrol et
            if (existingCourseDay.equals(newCourseDay)) {
                System.out.println("Same day detected!");
                // Zamanların çakışıp çakışmadığını kontrol et
                boolean startConflict = newStartTime.isBefore(existingEndTime);
                boolean endConflict = newEndTime.isAfter(existingStartTime);
                System.out.println("Start conflict: " + startConflict);
                System.out.println("End conflict: " + endConflict);

                if (startConflict && endConflict) {
                    System.out.println("Conflict detected! Not available.");
                    return false; // Çakışma var, öğrenci bu derse katılamaz
                }
            }
        }
        System.out.println("No conflicts found, student is available!");
        return true; // Çakışma yok, öğrenci derse katılabilir
    }

}