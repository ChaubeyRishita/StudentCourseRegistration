package studentCourseRegistration;

import java.sql.Connection;
//import java.sql.Statement;
import java.util.Scanner;


public class Main {

	public static void main(String[] args) {
		try {
			Connection c = DBConnection.getConnection();
			CourseRegistration cr = new CourseRegistration();
			Scanner sc = new Scanner(System.in);
			
			while(true) {
				cr.displayOptions();
				int choice;
//				only handle the case for non-integer input.
//				invalid integer input handled by default case.
				while (!sc.hasNextInt()) { // Check for valid integer input
					System.out.println("Invalid input. Please enter an integer. ");
					cr.displayOptions();
					sc.next(); // Clear the buffer
				}
				choice = sc.nextInt();
				if (choice == 6) { 
					System.out.println("Thank you for using the Student Course Registration System. Goodbye!");
					break;
				}
				
				switch(choice) {
				case 1:
					cr.displayAllCourses(c);
					break;
					
				case 2:
					cr.displayAvailableCourses(c);
					break;
					
				case 3:
					cr.displayRegisteredCourses(c);
					break;
					
				case 4:
					cr.registerCourse(c);
					break;
					
				case 5:
					cr.dropCourse(c);
					break;
					
				case 6:
					break;
					
				default:
					System.out.println("Invalid entry. \nEnter your choice: ");
				}
			}
			sc.close();
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
