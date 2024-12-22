package com.example.demo;

import com.example.demo.Classroom;
import com.example.demo.Course;
import com.example.demo.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Admin {
    private CourseDataAccessObject courseDAO;
    private ClassroomDataAccessObject classroomDAO;

    public Admin() {
        courseDAO = new CourseDataAccessObject();
        classroomDAO = new ClassroomDataAccessObject();
    }

    public String addStudentToCourse(Course course, Student student) {

        if(course.getAssignedClassroom() != null){
            Classroom cls = course.getAssignedClassroom();
            if(!student.getCourses().contains(course)) {
                if(cls.getCapacity() > course.getEnrolledStudentsList().size()) {
                    if(student.isAvailable(course)){
                        student.getCourses().add(course);
                        course.getEnrolledStudentsList().add(student);
                        CourseDataAccessObject.updateForAddingStudentToCourse(course, student);
                        AttendenceDatabase.addAttendanceSingleRow(course, student);
                        return "SUCCESS";
                    } else {
                        return "ERROR: This student has another course at that time!";
                    }
                } else {
                    return "ERROR: There is no space in the classroom.";
                }
            } else {
                return "ERROR: The student is already enrolled in this course";
            }
        }
        return "ERROR: Course has no assigned classroom";
    }

    public String removeStudentFromCourse(Course course, Student student) {
        System.out.println(student);
        if(student.getCourses().contains(course)) {
            System.out.println("heyyyyyyyyooooooo");
            try {
                student.getCourses().remove(course);
                course.getEnrolledStudentsList().remove(student);
                CourseDataAccessObject.updateForRemovingStudent(course, student);
                AttendenceDatabase.deleteAttendanceRecord(student, course);
                return "SUCCESS";
            } catch (SQLException e) {
                return "ERROR: Database error occurred while removing student";
            }
        } else {
            return "ERROR: The student is not enrolled in this course.";
        }
    }

    public String transferStudentToAnotherCourse(Course enrolledCourse, Course transferCourse, Student student) {
        // 1. Önce temel kontroller
        if (!student.getCourses().contains(enrolledCourse)) {
            return "ERROR: Student is not enrolled in the source course";
        }

        if (student.getCourses().contains(transferCourse)) {
            return "ERROR: Student is already enrolled in the target course";
        }

        // 2. Hedef ders için sınıf kontrolü
        if (transferCourse.getAssignedClassroom() == null) {
            return "ERROR: Target course has no assigned classroom";
        }

        // 3. Kapasite kontrolü
        if (transferCourse.getAssignedClassroom().getCapacity() <= transferCourse.getEnrolledStudentsList().size()) {
            return "ERROR: There is no space in the target classroom";
        }

        // 4. ÖNEMLİ: Zaman çakışması kontrolü
        // Öğrenciyi geçici olarak mevcut dersten çıkar ki kendi dersiyle çakışma kontrolü yapmasın
        student.getCourses().remove(enrolledCourse);
        boolean isTimeAvailable = student.isAvailable(transferCourse);
        // Kontrolden sonra dersi geri ekle
        student.getCourses().add(enrolledCourse);

        if (!isTimeAvailable) {
            return "ERROR: Student has another course at the target course time";
        }

        // 5. Tüm kontroller başarılı, transferi gerçekleştir
        try {
            // Önce eski dersten çıkar
            student.getCourses().remove(enrolledCourse);
            enrolledCourse.getEnrolledStudentsList().remove(student);

            // Sonra yeni derse ekle
            student.getCourses().add(transferCourse);
            transferCourse.getEnrolledStudentsList().add(student);

            // Database işlemleri
            CourseDataAccessObject.updateForTransferringStudent(enrolledCourse, transferCourse, student);
            AttendenceDatabase.deleteAttendanceRecord(student, enrolledCourse);
            return "SUCCESS";
        } catch (SQLException e) {
            // Hata durumunda değişiklikleri geri al
            student.getCourses().add(enrolledCourse);
            enrolledCourse.getEnrolledStudentsList().add(student);
            student.getCourses().remove(transferCourse);
            transferCourse.getEnrolledStudentsList().remove(student);
            return "ERROR: Database error occurred during transfer";
        }
    }

    //TODO: COURSENUN CLASSROOMU, COURSEUN STUDENT LİSTİ, CLASSROOMUN CAPACİTYSİ,


}