package studentCourseRegistration;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Scanner;

public class CourseRegistration {
	
	public void displayOptions() {
		System.out.println("----------------------------------------------------");
		System.out.println("Welcome to Student Course Registration System.");
		
		System.out.println("Select any ony one of the following options:");
		System.out.println("1. Display all courses");
		System.out.println("2. Display available courses");
		System.out.println("3. Display registered courses");
		System.out.println("4. Register course");
		System.out.println("5. Drop courses");
		System.out.println("6. Exit.");
		System.out.println("Enter your choice: ");
	}
	
	public int getValidInput(Scanner sc, String message) {
		System.out.println(message);
		
		// Only check for integer input. The individual cases for validation would be handled separately.
		while (!sc.hasNextInt()) { // Check for valid integer input
			System.out.println("Invalid input. Please enter an integer. ");
			sc.next(); // Clear the buffer
		}
		return sc.nextInt();
	}
	
	// set.next() returns true if s_id exists
	private boolean studentExists(int s_id_toRegister, Connection c) throws SQLException {
        PreparedStatement pstmt = c.prepareStatement("SELECT 1 FROM student WHERE s_id = ?");
        pstmt.setInt(1, s_id_toRegister);
        ResultSet set = pstmt.executeQuery();
        return set.next();
    }

	// set.next() returns true if c_id exists
    private boolean courseExists(int c_id_toRegister, Connection c) throws SQLException {
        PreparedStatement pstmt = c.prepareStatement("SELECT 1 FROM course WHERE c_id = ?");
        pstmt.setInt(1, c_id_toRegister);
        ResultSet set = pstmt.executeQuery();
        return set.next();
    }
    
    // Looping through the ResultSet object to check if desiredValue (s_id_toRegister or c_id_toRegister) is present
    private boolean presentInResultSet (ResultSet set, int desiredValue) throws SQLException {
    	boolean present = false;
		while(set.next()) {
			if(set.getInt("c_id") == desiredValue) {
				present = true;
				break;
			}
		}
		set.close();
    	return present;
    }
    
    public boolean isCourseAlreadyRegistered(Connection c, int s_id_toRegister, int c_id_toRegister) throws SQLException {
    	boolean courseAlreadyRegistered = false;
        PreparedStatement pstmt = c.prepareStatement("SELECT c_id FROM studentCourse WHERE s_id = ?");
        pstmt.setInt(1, s_id_toRegister);
        ResultSet set = pstmt.executeQuery(); // The resultant 'set' contains all the courses s_id_toRegister has already registered
        if (presentInResultSet(set, c_id_toRegister)) {
        	// If c_id_toRegister is present in 'set', then the course is already registered
        	courseAlreadyRegistered = true;
        }
        
        return courseAlreadyRegistered;
    }
    
    public boolean isCourseAvailable(Connection c, int c_id_toRegister, int slotNumber_toRegister) throws SQLException {
    	boolean courseAvailable = false;
        CallableStatement cstmt = c.prepareCall("{CALL checkSlotAvailability(?, ?)}");
        cstmt.setInt(1, c_id_toRegister);
        cstmt.setInt(2, slotNumber_toRegister);
        ResultSet set = cstmt.executeQuery();
        if (set.next()) {
            courseAvailable = set.getBoolean("available"); // Use column name
        }
        return courseAvailable;
    }
    
    public boolean isStudentFree(Connection c, int s_id_toRegister, Time slotTime_toRegister) throws SQLException {
        boolean overlappedSlots = false;
        String query = "SELECT course_slot FROM StudentCourse WHERE s_id = ?";

        PreparedStatement pstmt = c.prepareStatement(query);
        pstmt.setInt(1, s_id_toRegister);
        ResultSet set = pstmt.executeQuery();

        while (set.next()) {
            Time existingSlot = set.getTime("course_slot");
            LocalTime existingStartTime = existingSlot.toLocalTime();
            LocalTime existingEndTime = existingStartTime.plusHours(2);
            LocalTime newStartTime = slotTime_toRegister.toLocalTime();
            LocalTime newEndTime = newStartTime.plusHours(2);

            if (newStartTime.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime)) {
                overlappedSlots = true;
                break;
            }
        }

        set.close();
        pstmt.close();

        return !overlappedSlots;
    }
    
	public void displayAllCourses(Connection c) throws SQLException {
		String query = "SELECT * FROM course";
		Statement stmt = c.createStatement();
		ResultSet set = stmt.executeQuery(query);
		
		while(set.next()) {
			int c_id = set.getInt("c_id");
			String c_title = set.getString("c_title");
			String c_description = set.getString("c_description");
			int c_capacity_slot1 = set.getInt("c_capacity_slot1");
			Time c_startTime_slot1 = set.getTime("c_startTime_slot1");
			int c_capacity_slot2 = set.getInt("c_capacity_slot2");
			Time c_startTime_slot2 = set.getTime("c_startTime_slot2");
			int c_duration = set.getInt("c_duration (in hours)");
			
			SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
	        String startTimeSlot1Formatted = formatter.format(c_startTime_slot1);
	        String startTimeSlot2Formatted = formatter.format(c_startTime_slot2);
			
	        System.out.println("----------------------------------------------------");
			System.out.println("Course id: " + c_id);
			System.out.println("Course title: " + c_title);
			System.out.println("Course description: " + c_description);
			System.out.println("Course capacity (Slot 1): " + c_capacity_slot1);
			System.out.println("Course start time (Slot 1): " + startTimeSlot1Formatted);
			System.out.println("Course capacity (Slot 2): " + c_capacity_slot2);
			System.out.println("Course start time (Slot 2): " + startTimeSlot2Formatted);
			System.out.println("Course duration (same for each slot): " + c_duration);
		}
	}
	
	public void displayAvailableCourses(Connection c) throws SQLException {
//		System.out.println("Display Available Courses!");
		
		// If even a single slot is available, the course will be available.
		String query = "SELECT * FROM course WHERE c_capacity_slot1 > 0 OR c_capacity_slot2 > 0";
		Statement stmt = c.createStatement();
		ResultSet set = stmt.executeQuery(query);
		
		if(set.next()) {
			while(set.next()) {
				int c_id = set.getInt("c_id");
				String c_title = set.getString("c_title");
				String c_description = set.getString("c_description");
				int c_capacity_slot1 = set.getInt("c_capacity_slot1");
				Time c_startTime_slot1 = set.getTime("c_startTime_slot1");
				int c_capacity_slot2 = set.getInt("c_capacity_slot2");
				Time c_startTime_slot2 = set.getTime("c_startTime_slot2");
				int c_duration = set.getInt("c_duration (in hours)");
				
				SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
		        String startTimeSlot1Formatted = formatter.format(c_startTime_slot1);
		        String startTimeSlot2Formatted = formatter.format(c_startTime_slot2);
				
		        System.out.println("----------------------------------------------------");
				System.out.println("Course id: " + c_id);
				System.out.println("Course title: " + c_title);
				System.out.println("Course description: " + c_description);
				System.out.println("Course capacity (Slot 1): " + c_capacity_slot1);
				System.out.println("Course start time (Slot 1): " + startTimeSlot1Formatted);
				System.out.println("Course capacity (Slot 2): " + c_capacity_slot2);
				System.out.println("Course start time (Slot 2): " + startTimeSlot2Formatted);
				System.out.println("Course duration (same for each slot): " + c_duration);
			}
		}
		else {
			System.out.println("No courses available!");
		}
		
		
	}
	
	public void displayRegisteredCourses(Connection c) throws SQLException {
 		Scanner sc = new Scanner(System.in);

		int s_id_Input, c_id_toDisplay, c_duration_toDisplay;// Only require input for the s_id, since registered courses for a particular will be displayed
		Time c_slotTime_toDisplay;
		String c_title_toDisplay, c_description_toDisplay;
		// Check if the student ID is an integer and if it exists
		s_id_Input = getValidInput(sc, "Enter your student ID: ");
		if (!studentExists(s_id_Input, c)) {
		    System.out.println("Student ID does not exist.");
		    return;
		}
		
		PreparedStatement pstmt = c.prepareStatement("SELECT c_id, course_slot FROM studentCourse WHERE s_id = ?");
		pstmt.setInt(1, s_id_Input);
		ResultSet set = pstmt.executeQuery();
		
		if(set.next()) {
			while(set.next()) {
				// Student might have registered for multiple courses
				c_id_toDisplay = set.getInt("c_id");
				c_slotTime_toDisplay = set.getTime("course_slot");
				
				PreparedStatement pstmt1 = c.prepareStatement("SELECT c_title, c_description, `c_duration (in hours)` FROM course WHERE c_id = ?");
				pstmt1.setInt(1, c_id_toDisplay);
				ResultSet set1 = pstmt1.executeQuery();
				
				while(set1.next()) {
					c_title_toDisplay = set1.getString("c_title");
					c_description_toDisplay = set1.getString("c_description");
					c_duration_toDisplay = set1.getInt("c_duration (in hours)");
					
			        System.out.println("----------------------------------------------------");
			        System.out.println("Course id: " + c_id_toDisplay);
					System.out.println("Course title: " + c_title_toDisplay);
					System.out.println("Course description: " + c_description_toDisplay);
					System.out.println("Course slot time: " + c_slotTime_toDisplay);
					System.out.println("Course duration (same for each slot): " + c_duration_toDisplay);
				}
			}
		}
		else {
			System.out.println("You have not registered for any course!");
		}	
	}
	
	public void registerCourse(Connection c) throws SQLException {
		Scanner sc = new Scanner(System.in);
		
		// getValidInput only ensures that the input values are integer
		
		int s_id_toRegister, c_id_toRegister, slotNumber_toRegister;
		// Check if the student ID is an integer and if it exists
		s_id_toRegister = getValidInput(sc, "Enter your student ID: ");
		if (!studentExists(s_id_toRegister, c)) {
		    System.out.println("Student ID does not exist.");
		    return;
		}
        
        // Check if the course ID is an integer and of it exists
        c_id_toRegister = getValidInput(sc, "Enter the ID of the course you want to register in: ");
		if (!courseExists(c_id_toRegister, c)) {
		    System.out.println("Course ID does not exist.");
		    return;
		}
		
        // Check that slotNumber is only either 1 or 2
        slotNumber_toRegister = getValidInput(sc, "Select slot number. (1 or 2)");
        if (!(slotNumber_toRegister == 1 || slotNumber_toRegister == 2)){
        	System.out.println("The course is only conducted in 2 slots.\nPlease enter a valid slot number. ");
        	return;
        }
    	String query_FetchSlotTime;
    	Time slotTime_toRegister = null;
        
        if(slotNumber_toRegister == 1) {
        	query_FetchSlotTime = "SELECT c_startTime_slot1 FROM course WHERE c_id = ?";
        }
        else {
        	query_FetchSlotTime = "SELECT c_startTime_slot2 FROM course WHERE c_id = ?";
        }
        PreparedStatement pstmt = c.prepareStatement(query_FetchSlotTime);
        pstmt.setInt(1, c_id_toRegister);
        ResultSet set = pstmt.executeQuery();
        while(set.next()) {
        	if(slotNumber_toRegister == 1) {
            	slotTime_toRegister = set.getTime("c_startTime_slot1");       
            }
            else {
            	slotTime_toRegister = set.getTime("c_startTime_slot2");       
            }
        }
        
		// Relation for (s_id, c_id, slotNUmber)
        // (s_id, c_id) Check if the entered course is already registered or not
        boolean courseAlreadyRegistered = isCourseAlreadyRegistered(c, s_id_toRegister, c_id_toRegister);
        
        if (courseAlreadyRegistered) {
        	pstmt = c.prepareStatement("SELECT c_id, c_title FROM course WHERE c_id = ?");
        	pstmt.setInt(1, c_id_toRegister);
        	ResultSet set1 = pstmt.executeQuery();
        	while (set1.next()) {
        		int c_id = set1.getInt("c_id");
        		String c_title = set1.getString("c_title");
        		
        		System.out.println("You have already registered " + c_title + " course with " + c_id + " course ID.");
        		System.out.println("You can only register any course once.");
        	}
        }
                
        // (c_id, slotNumber) Check if the course is available for the selected slot or not (in terms of capacity)?
        boolean courseAvailable = isCourseAvailable(c, c_id_toRegister, slotNumber_toRegister);
        
        if(!courseAvailable) {
        	// If course is not available for selected slot give error message
        	pstmt = c.prepareStatement("SELECT c_id, c_title FROM course WHERE c_id = ?");
        	pstmt.setInt(1, c_id_toRegister);
        	ResultSet set1 = pstmt.executeQuery();
        	while (set1.next()) {
        		int c_id = set1.getInt("c_id");
        		String c_title = set1.getString("c_title");
        		
        		System.out.println("The course " + c_title + " with ID " + c_id + " is not available for slot " + slotNumber_toRegister + ".");
        	}

        }
        
        // -----------------------------------------------------------------------

        // (s1, slotNumber) Check if the student is free i.e has no other course for selected slot so that courses are not overlapped
        boolean studentFree = isStudentFree(c, s_id_toRegister, slotTime_toRegister); // studentFree True means there are no overlapping slots. We are good to go. 
        
        if(!studentFree) {
        	pstmt = c.prepareStatement("SELECT c_id, c_title FROM course WHERE c_id = ?");
        	pstmt.setInt(1, c_id_toRegister);
        	ResultSet set1 = pstmt.executeQuery();
        	while (set1.next()) {
        		int c_id = set1.getInt("c_id");
        		String c_title = set1.getString("c_title");
        		
        		System.out.println("You have already registered for " + c_title + " with ID " + c_id + " for slot " + slotTime_toRegister + ".");
        	}

        }
        
        
        // Check if all 3 conditions courseAlreadyRegistered(s1, c1), courseCapacityFull(c1, slot1), studentFree(s1,slot1) are true.
        // If true, register. Else, 'Registration unsuccessful'
        if(!courseAlreadyRegistered && courseAvailable && studentFree) {
        	System.out.println("Inserting values - s_id: " + s_id_toRegister + ", c_id: " + c_id_toRegister + ", course_slot: " + slotTime_toRegister + "slotnumber" + slotNumber_toRegister);
        	 PreparedStatement insertStmt = c.prepareStatement("INSERT INTO StudentCourse (s_id, c_id, course_slot) VALUES (?, ?, ?)");
             insertStmt.setInt(1, s_id_toRegister);
             insertStmt.setInt(2, c_id_toRegister);
             insertStmt.setTime(3, slotTime_toRegister);
             insertStmt.executeUpdate();

             PreparedStatement updateStmt = c.prepareStatement("UPDATE course SET c_capacity_slot" + slotNumber_toRegister + " = c_capacity_slot" + slotNumber_toRegister + " - 1 WHERE c_id = ?");
             updateStmt.setInt(1, c_id_toRegister);
             updateStmt.executeUpdate();

             insertStmt.close();
             updateStmt.close();
             System.out.println("Course registered successfully!");
        }
        else {
            System.out.println("Course registeration unsuccessful!");
        }    
	}
	
	public void dropCourse(Connection c) throws SQLException {
		Scanner sc = new Scanner(System.in);
		
		// getValidInput only ensures that the input values are integer
		
		int s_id_toDrop, c_id_toDrop;
		// Check if the student ID is an integer and if it exists
		s_id_toDrop = getValidInput(sc, "Enter your student ID: ");
		if (!studentExists(s_id_toDrop, c)) {
		    System.out.println("Student ID does not exist.");
		    return;
		}
        
        // Check if the course ID is an integer and if it exists
		c_id_toDrop = getValidInput(sc, "Enter the ID of the course you want to drop: ");
		if (!courseExists(c_id_toDrop, c)) {
		    System.out.println("Course ID does not exist.");
		    return;
		}
		
        // Since the student can only register any course for just a single slot, there is no need to ask the student to enter slotNumber while dropping a course.
		// (s1, c1, slot1)
		
		// (s1, c1) Check if the course student wants to drop is registered or not
		boolean courseRegistered = isCourseAlreadyRegistered(c, s_id_toDrop, c_id_toDrop);
        
        if (courseRegistered) {
        	// If course is registered, then it can be dropped. Make the necessary changes.
        	PreparedStatement getSlotStmt = c.prepareStatement("SELECT course_slot FROM studentCourse WHERE s_id = ? AND c_id = ?");
            getSlotStmt.setInt(1, s_id_toDrop);
            getSlotStmt.setInt(2, c_id_toDrop);
            ResultSet slotSet = getSlotStmt.executeQuery();

            if (slotSet.next()) {
                Time courseSlot = slotSet.getTime("course_slot");
                
                // Fetch the course start times for comparison
                PreparedStatement getStartTimeStmt = c.prepareStatement("SELECT c_startTime_slot1, c_startTime_slot2 FROM course WHERE c_id = ?");
                getStartTimeStmt.setInt(1, c_id_toDrop);
                ResultSet startTimeSet = getStartTimeStmt.executeQuery();
                
                if (startTimeSet.next()) {
                    Time c_startTime_slot1 = startTimeSet.getTime("c_startTime_slot1");
                    Time c_startTime_slot2 = startTimeSet.getTime("c_startTime_slot2");

                 // Update course capacity based on the slot
                    String updateCapacityQuery = null;
                    if (courseSlot.equals(c_startTime_slot1)) {
                        updateCapacityQuery = "UPDATE course SET c_capacity_slot1 = c_capacity_slot1 + 1 WHERE c_id = ?";
                    } 
                    else if (courseSlot.equals(c_startTime_slot2)) {
                        updateCapacityQuery = "UPDATE course SET c_capacity_slot2 = c_capacity_slot2 + 1 WHERE c_id = ?";
                    }
                    
                    if (updateCapacityQuery != null) {
                        PreparedStatement updateCapacityStmt = c.prepareStatement(updateCapacityQuery);
                        updateCapacityStmt.setInt(1, c_id_toDrop);
                        updateCapacityStmt.executeUpdate();

                     // Delete the registration
                        PreparedStatement deleteStmt = c.prepareStatement("DELETE FROM StudentCourse WHERE s_id = ? AND c_id = ?");
                        deleteStmt.setInt(1, s_id_toDrop);
                        deleteStmt.setInt(2, c_id_toDrop);
                        deleteStmt.executeUpdate();

                        System.out.println("Course dropped successfully!");
                    }
//                    else {
//			        	// If course is not already registered, it cannot be deleted. Hence give error message.
//			        	PreparedStatement pstmt = c.prepareStatement("SELECT c_id, c_title FROM course WHERE c_id = ?");
//			        	pstmt.setInt(1, c_id_toDrop);
//			        	ResultSet set1 = pstmt.executeQuery();
//			        	while (set1.next()) {
//			        		int c_id = set1.getInt("c_id");
//			        		String c_title = set1.getString("c_title");
//			        		
//			        		System.out.println("You have not registered for course " + c_title + " with " + c_id + " course ID.");
//			        		System.out.println("An error occurred while dropping the course.");
//			        	}
//                    }
		
					// (c1, slot1) No need to check. Because if course is registered, it can only be registered in any one slot
					// (s1, slot1) The student is free for the duration of slot1. No need to check.
                    
                } // startTime set: get startTime for slot 1 and 2 to check and decrement course slot capacity
            } // slotSet: get course_slot from studentCourse for course to be dropped
        } // if (courseRegistered)
        else {
        	// If course is not already registered, it cannot be deleted. Hence give error message.
        	PreparedStatement pstmt = c.prepareStatement("SELECT c_id, c_title FROM course WHERE c_id = ?");
        	pstmt.setInt(1, c_id_toDrop);
        	ResultSet set1 = pstmt.executeQuery();
        	while (set1.next()) {
        		int c_id = set1.getInt("c_id");
        		String c_title = set1.getString("c_title");
        		
        		System.out.println("You have not registered for course " + c_title + " with " + c_id + " course ID.");
        		System.out.println("An error occurred while dropping the course.");
        	}
        }
	} // dropCourse()
} // class courseRegistration
