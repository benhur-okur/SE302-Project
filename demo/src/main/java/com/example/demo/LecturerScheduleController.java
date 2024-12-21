package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class LecturerScheduleController {

    @FXML
    private GridPane scheduleGrid = new GridPane();
    @FXML
    private static Label lecturerNameLabel;

    public static String lecturer;

    // Setter method to set the lecturer's name
    public static void setLecturerName(String lecturerName) {
        lecturer = lecturerName;
        //lecturerNameLabel.setText(lecturerName); // Öğretim üyesi adını etiketle güncelle
    }


    public void initialize() {
        setupLabels();
        populateCourses();
    }

    // Günler ve saatler için grid başlıklarını ayarla
    private void setupLabels() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] timeSlots = generateTimeSlots("08:30", "19:30", 55);

        // Gün başlıklarını (Column 0 hariç) GridPane'e ekle
        for (int col = 1; col <= days.length; col++) {
            Label dayLabel = new Label(days[col - 1]);
            scheduleGrid.add(dayLabel, col, 0); // İlk satır, her bir gün için
        }

        // Saat başlıklarını (Row 0 hariç) GridPane'e ekle
        for (int row = 1; row <= timeSlots.length; row++) {
            Label timeLabel = new Label(timeSlots[row - 1]);
            scheduleGrid.add(timeLabel, 0, row); // İlk sütun, her bir saat dilimi için
        }
    }

    // Dersleri GridPane'e ekle
    private void populateCourses() {
        if (lecturer == null) {
            System.out.println("No lecturer selected.");
            return;
        }

        // Veritabanından dersleri al
        ArrayList<VBox> vBoxes = AssignCourseClassroomDB.getAssign(CourseDataAccessObject.getCourseWhereLecturerIs(lecturer));

        for (VBox originalVBox : vBoxes) {
            Course currentCourse;
            Label courseIDLabel = (Label) originalVBox.getChildren().get(0);
            String courseID = courseIDLabel.getText();
            currentCourse = CourseDataAccessObject.getCourseByCourseID(courseID);

            int durationOfCurrentCourse = currentCourse.getDuration();
            int col = getDayColumnIndex(currentCourse.getCourseDay());
            int startRow = getTimeRowIndex(currentCourse.getStartTime().toString());

            for (int i = 0; i < durationOfCurrentCourse; i++) {
                // Orijinal VBox'ı kopyalayarak yeni bir VBox oluştur
                VBox vboxCopy = createVBoxCopy(originalVBox);

                // Yeni VBox'ı grid'e ekle
                scheduleGrid.add(vboxCopy, col, startRow + i);
            }
        }
    }

    /**
     * Mevcut bir VBox'ın içeriğini kopyalar ve yeni bir VBox oluşturur.
     */
    private VBox createVBoxCopy(VBox originalVBox) {
        VBox vboxCopy = new VBox();
        vboxCopy.setSpacing(originalVBox.getSpacing());
        for (Node child : originalVBox.getChildren()) {
            if (child instanceof Label) {
                Label originalLabel = (Label) child;
                Label labelCopy = new Label(originalLabel.getText());
                labelCopy.setStyle(originalLabel.getStyle());
                vboxCopy.getChildren().add(labelCopy);
            }
        }
        return vboxCopy;
    }


    // Haftanın günleri için sütun indekslerini döndür
    private static int getDayColumnIndex(String day) {
        switch (day) {
            case "Monday": return 1;
            case "Tuesday": return 2;
            case "Wednesday": return 3;
            case "Thursday": return 4;
            case "Friday": return 5;
            case "Saturday": return 6;
            case "Sunday": return 7;
            default: return -1;
        }
    }

    // Saat dilimleri için satır indekslerini döndür
    private static int getTimeRowIndex(String time) {
        String[] timeSlots = generateTimeSlots("08:30", "19:30", 55);
        for (int i = 0; i < timeSlots.length; i++) {
            if (timeSlots[i].equals(time)) {
                return i + 1; // İlk satır başlık için ayrılmış
            }
        }
        return -1;
    }

    // Belirli bir başlangıç saatinden itibaren zaman dilimlerini oluştur
    private static String[] generateTimeSlots(String startTime, String endTime, int intervalMinutes) {
        ArrayList<String> timeSlots = new ArrayList<>();
        java.time.LocalTime start = java.time.LocalTime.parse(startTime);
        java.time.LocalTime end = java.time.LocalTime.parse(endTime);

        while (!start.isAfter(end)) {
            timeSlots.add(start.toString());
            start = start.plusMinutes(intervalMinutes);
        }

        return timeSlots.toArray(new String[0]);
    }




}
