# Syllabus Manager

Welcome to the Syllabus Manager Wiki! This system optimizes course scheduling, classroom assignments, and attendance tracking for academic institutions.

## Overview

Syllabus Manager is a standalone Windows desktop application designed to handle up to 25,000 student records efficiently. It provides separate interfaces for administrators, lecturers, and students, all while maintaining data in a SQLite database.

## Prerequisites
Operating system: Windows
Ensure you have Java Runtime Environment (JRE) installed (version 8 or higher). Download JRE here.

## Features and Usages

Data import: The system imports data automatically. The imported courses are processed and assigned classrooms to each automatically.

Create New Courses: Create new courses by entering details such as course ID, start time, duration, lecturer, and classroom, and also check the availability of selected classrooms before assigning.

Enrollment Students: Allows students to be added to the selected course, removed from the selected course, and transferred from one enrolled course to another.

Change Classroom of the Course: Allows manually changing the selected classroom of the chosen course if the classroom is available.

View Weekly Schedules: Users can view dynamic weekly schedules for classrooms, lecturers, and students.

View Enrolled Courses: Users can view the enrolled courses of a specific student.

Attendance Management: Users can increment the absence count for courses of a specific student, and also view all attendance records.

Search and Refresh: Quickly refresh schedules and search for specific data.

## User Interface

1. **Course Management Panel
   ![image](https://github.com/user-attachments/assets/5fb99a69-e380-459b-9db7-f941fc039ddd)


2. **Student Management Panel
  

3. **Scheduling Panel



## Contributing

When contributing to this project, please ensure:
1. Code follows the established object-oriented design patterns
2. New features maintain compatibility with existing components
3. Database schema modifications are properly documented
4. UI changes follow the established design guidelines

## Support

For additional support or information about the system, please contact the development team or refer to the system's informational website.
