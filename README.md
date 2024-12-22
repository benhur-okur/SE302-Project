# Syllabus Manager

Welcome to the Syllabus Manager Wiki! This system optimizes course scheduling, classroom assignments, and attendance tracking for academic institutions.

# Overview

Syllabus Manager is a standalone Windows desktop application designed to handle up to 25,000 student records efficiently. It provides separate interfaces for administrators, lecturers, and students, all while maintaining data in a SQLite database.

# Prerequisites
Operating system: Windows
Ensure you have Java Runtime Environment (JRE) installed (version 8 or higher). Download JRE here.

# Features and Usages

Data import: The system imports data automatically. The imported courses are processed and assigned classrooms to each automatically.

Create New Courses: Create new courses by entering details such as course ID, start time, duration, lecturer, and classroom, and also check the availability of selected classrooms before assigning.

Enrollment Students: Allows students to be added to the selected course, removed from the selected course, and transferred from one enrolled course to another.

Change Classroom of the Course: Allows manually changing the selected classroom of the chosen course if the classroom is available.

View Weekly Schedules: Users can view dynamic weekly schedules for classrooms, lecturers, and students.

View Enrolled Courses: Users can view the enrolled courses of a specific student.

Attendance Management: Users can increment the absence count for courses of a specific student, and also view all attendance records.

Search and Refresh: Quickly refresh schedules and search for specific data.

# User Interface
## Course Management Panel
![image](https://github.com/user-attachments/assets/c981adec-e8ab-44cf-ae10-dd1d870632b9)
![image](https://github.com/user-attachments/assets/bf692e16-d8f4-4619-8d1a-0a24ae0175aa)


## Student Management Panel
![image](https://github.com/user-attachments/assets/15d94a4f-8fe0-482b-90e3-6d9f0a788d28)


## Lecturer Panel
![image](https://github.com/user-attachments/assets/08d4e54c-b4c8-47c7-bb8b-5a2c5c61d81c)


## Classroom Panel
![image](https://github.com/user-attachments/assets/354261dd-06dc-4e0d-ae10-8d91cfa903c2)


## Attendance Panel
![image](https://github.com/user-attachments/assets/3315c580-558c-4ea9-819a-a587c4fc1db1)

## File Structure
Database: The system stores data in an SQLite database file located in the user's Documents folder.
CSV Support: The application supports importing classroom and course data from CSV files.



## Project Management

We used Notion to manage tasks and show the changes in the implementation part from the Design document, also tracking the progress of this project. You can view our Notion board here: https://www.notion.so/12c955c0ee1380908f9fe7b0abe77b23?v=12c955c0ee13803cb84e000c28d9b10e&pvs=4

## Milestone 1 - Requirements
The Requirements Document: [se-302-Milestone_1.pdf](https://github.com/user-attachments/files/18223000/se-302-Milestone_1.pdf)

## Milestone 2 - Design Document
The Design Document: [se-302-Milestone_2.pdf](https://github.com/user-attachments/files/18223002/se-302-Milestone_2.pdf)

## Support
For additional support or information about the system, please contact the development team or refer to the system's informational website.
