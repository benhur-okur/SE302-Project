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
        // Eğer öğrencinin katıldığı dersler listesi boşsa, öğrenci uygundur.
        if (this.getCourses() == null || this.getCourses().isEmpty()) {
            return true;
        }

        for (Course existingCourse : this.getCourses()) {
            LocalTime existingStartTime = existingCourse.getStartTime();
            LocalTime existingEndTime = existingCourse.getEndTime();
            String existingCourseDay = existingCourse.getCourseDay();
            String newCourseDay = newCourse.getCourseDay();
            LocalTime newStartTime = newCourse.getStartTime();
            LocalTime newEndTime = newCourse.getEndTime();

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