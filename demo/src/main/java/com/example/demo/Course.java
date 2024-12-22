package com.example.demo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Course {
    private String courseID;
    private String timeToStart;
    private int duration;
    private String lecturerName;
    private ArrayList<String> studentNames;
    private String courseDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Lecturer lecturer;
    private Classroom assignedClassroom;
    private ArrayList<Student> enrolledStudentsList;
    private ArrayList<Attendance> attendanceRecordList;
    private CourseDataAccessObject courseDAO;
    private String classroomName;


    public Course(String courseID, String timeToStart,int duration, String lecturerName, ArrayList<String> studentNames) {
        this.courseID = courseID;
        this.duration = duration;
        this.lecturerName = lecturerName;
        this.studentNames = studentNames;
        this.timeToStart = timeToStart;

        String[] timeParts = timeToStart.split(" ");
        this.courseDay = timeParts[0];
        this.startTime = LocalTime.parse(timeParts[1], DateTimeFormatter.ofPattern("H:mm"));
        this.endTime = this.getEndTime(startTime);

        createLecturer(lecturerName);
        this.enrolledStudentsList = new ArrayList<>();
        createStudents(studentNames);
        this.attendanceRecordList = new ArrayList<>();
        courseDAO = new CourseDataAccessObject();


    }

    public Course(String courseID, String timeToStart, int duration, String lecturerName) {
        this.courseID = courseID;
        this.timeToStart = timeToStart;
        this.duration = duration;
        this.lecturerName = lecturerName;
    }

    public static ArrayList<Classroom> findSuitableClassrooms(Course course, ArrayList<Classroom> classrooms) {
        ArrayList<Classroom> suitableClassrooms = new ArrayList<>();

        // Kurs bilgilerini al
        int studentCount = course.getEnrolledStudentsList().size();
        String courseDay = course.getCourseDay();
        LocalTime startTime = course.getStartTime();
        LocalTime endTime = course.getEndTime();

        // Her sınıfı kontrol et
        for (Classroom classroom : classrooms) {
            // Kapasite ve zaman uygunluğunu kontrol et
            if (classroom.getCapacity() >= studentCount &&
                    classroom.isAvailable(courseDay, startTime, endTime)) {
                suitableClassrooms.add(classroom);
            }
        }

        return suitableClassrooms;
    }

    public void assignClassroom(ArrayList<Classroom> classrooms) {
        // Kontrol: Sınıf listesi boş veya null ise
        if (classrooms == null || classrooms.isEmpty()) {
            System.out.println("No classrooms available for assignment.");
            return;
        }

        // Uygun sınıfları bul
        ArrayList<Classroom> suitableClassrooms = findSuitableClassrooms(this, classrooms);

        // Kontrol: Uygun sınıf yoksa
        if (suitableClassrooms.isEmpty()) {
            System.out.println("No suitable classroom found for course: " + this.getCourseID());
            return;
        }

        // Kapasiteye göre en büyükten küçüğe sırala ve ilk uygun sınıfı seç
        suitableClassrooms.sort(Comparator.comparingInt(Classroom::getCapacity).reversed());
        Classroom assignedClassroom = suitableClassrooms.get(0);

        // Sınıfı ata ve kursu sınıfın ders listesine ekle
        this.assignedClassroom = assignedClassroom;
        assignedClassroom.getCourses().add(this);
        AssignCourseClassroomDB.initializeAssigning(this, assignedClassroom.getClassroomName());

        classroomName = assignedClassroom.getClassroomName();

        // Test için log yazdır
        //System.out.println(this.getCourseID() + ": Assigned to " + assignedClassroom.getClassroomName() +
        //        " (Capacity: " + assignedClassroom.getCapacity() + ")");
        //System.out.println(this.getCourseDay() + " " + this.getStartTime() + " - " + this.getEndTime(this.getStartTime()));
    }

    public void createLecturer(String lecturerName) {
        lecturer = Lecturer.findLecturerByName(lecturerName);

        // Eğer ders adı zaten hocanın ders listesinde varsa ekleme
        boolean courseExists = lecturer.getCourses().stream()
                .anyMatch(course -> course.getCourseID().equals(this.getCourseID()));

        if (!courseExists) {
            lecturer.getCourses().add(this);
        } else {

        }
    }

    public void createStudents(ArrayList<String> studentNames) {
        for (String studentName : studentNames) {
            Student student = Student.findStudentByName(studentName);

            boolean courseExists = student.getCourses().stream()
                    .anyMatch(course -> course.getCourseID().equals(this.getCourseID()));

            if (!courseExists) {
                student.getCourses().add(this); // Öğrenciye dersi ekle
            } else {
            }
            this.enrolledStudentsList.add(student);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return courseID.equals(course.courseID); // Burada yalnızca courseID'yi karşılaştırmak yeterli olabilir
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseID); // Burada da courseID'yi kullanmak yeterlidir
    }


    public LocalTime getEndTime(LocalTime startTime) {
        int totalDuration = duration * 45 + (duration - 1) * 10;
        endTime = startTime.plusMinutes(totalDuration);
        return endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public String getCourseDay() {
        return courseDay;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getTimeToStart() {
        return timeToStart;
    }

    public int getDuration() {
        return duration;
    }

    public String getLecturerName() {
        return lecturerName;
    }


    public ArrayList<String> getStudentNames() {
        return studentNames;
    }

    public void setStudentNames(ArrayList<String> studentNames) {
        this.studentNames = studentNames;
    }


    public LocalTime getEndTime() {
        return endTime;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public Classroom getAssignedClassroom() {
        return assignedClassroom;
    }

    public void setAssignedClassroom(Classroom assignedClassroom) {
        this.assignedClassroom = assignedClassroom;
    }

    public ArrayList<Student> getEnrolledStudentsList() {
        return enrolledStudentsList;
    }

    public void setEnrolledStudentsList(ArrayList<Student> enrolledStudentsList) {
        this.enrolledStudentsList = enrolledStudentsList;
    }

    /*
    @Override
    public String toString() {
        return "Course{" +
               "courseID='" + courseID + '\'' +
               ", timeToStart='" + timeToStart + '\'' +
               ", duration=" + duration +
               ", lecturerName='" + lecturerName +
               '}';
    }

     */

    public ArrayList<Attendance> getAttendanceRecordList() {
        return attendanceRecordList;
    }

    public void setAttendanceRecordList(ArrayList<Attendance> attendanceRecordList) {
        this.attendanceRecordList = attendanceRecordList;
    }

    public ArrayList<Student> getStudents() {
        return enrolledStudentsList;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

}
