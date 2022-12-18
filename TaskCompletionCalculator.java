/**
 * Problem statement: Calculating estimated completion date and time of a task
 *
 * Assumptions made for solving this problem are listed as follows:
 *     1. Weekends and public holidays are not considered
 *        i.e The employee will work every day (except on leave days) until the task is completed.
 *     2. No breaks are taken by the employee during his working hours.
 *     3. Working hour slot timing of the employee starts and ends with (integer) hour only.
 *        i.e start: 10 AM, end: 6 PM, etc
 *     4. Employee cannot work 24 hours continuously
 *        i.e working slot timing (start, end) of the employee cannot be the same
 *
 * @author Suraj Verma
 * @since 2022-12-19
 */

//package kapturecrmassignment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class TaskCompletionCalculator {

    private static final int TOTAL_HOURS_IN_A_DAY = 24;

    // Driver Code
    public static void main(String[] args) {
        Calendar endTime;

        // Taking input from user
        //endTime = getCompletionTimeFromUser();

        // Hardcoding the Input
        Calendar startTime = Calendar.getInstance();
        startTime.set(2022, 11, 17, 22, 00, 00);
        int timeRequiredForTask = 18;
        String workingHourStart = "11 PM";
        String workingHourEnd = "07 AM";

        List<Calendar> leaves = new ArrayList<>();
        Calendar l1 = Calendar.getInstance();
        l1.set(2022, 11, 19);
        leaves.add(l1);

        endTime = getEndTime(startTime, timeRequiredForTask, workingHourStart, workingHourEnd, leaves);

        // Showing the output
        System.out.println(getFormattedString(endTime));
    }

    /**
     * This method returns the Calendar date and time when the task is completed by the employee
     * @param startTime the calendar time of assigning the task
     * @param timeRequired the total time required to complete the given task
     * @param workingHourStart the start time of employees working hour
     * @param workingHourEnd the end time of employees working hour
     * @param leaves the list of calendar dates when the employee is on leave
     * @return a Calendar time of the task completion
     */
    private static Calendar getEndTime(Calendar startTime, int timeRequired, String workingHourStart, String workingHourEnd, List<Calendar> leaves){
        // validating inputs and initializing the variables
        if(timeRequired < 0){
            throw new IllegalArgumentException("Time required must be a positive integer");
        }

        Integer workingHourStartTime;
        Integer workingHourEndTime;

        try{
            workingHourStartTime = convertTo24HourFormat(workingHourStart);
            workingHourEndTime = convertTo24HourFormat(workingHourEnd);
        } catch (Exception e) {
            throw new IllegalArgumentException("12 hour time format (HH AM/PM) is required");
        }

        Calendar endTime = (Calendar) startTime.clone();
        int remainingTime = timeRequired;

        // If the task cannot be performed
        if(workingHourStartTime == workingHourEndTime){
            throw new IllegalArgumentException("Working hour slots of the employee cannot be same");
        }

        if(remainingTime == 0){
            return endTime;
        }

        // Calculating the number of hours the employee can work on the first day
        int hoursWorkedOnFirstDay = 0;
        if(workingHourEndTime < workingHourStartTime){
            hoursWorkedOnFirstDay = TOTAL_HOURS_IN_A_DAY - Math.max(endTime.get(Calendar.HOUR_OF_DAY), workingHourStartTime);
        }
        else{
            if(endTime.get(Calendar.HOUR_OF_DAY) < workingHourEndTime){
                hoursWorkedOnFirstDay = workingHourEndTime - Math.max(endTime.get(Calendar.HOUR_OF_DAY), workingHourStartTime);
            }
        }

        // Subtract the number of hours worked from the time require and check for the next day
        remainingTime -= Math.min(hoursWorkedOnFirstDay, remainingTime);
        moveToNextDay(endTime, workingHourStartTime, workingHourEndTime);

        // Iterate through each day until the task is completed
        while(remainingTime > 0){
            // Checking if the employee is on leave on this day
            if(isEmployeeOnLeave(leaves, endTime)){
                moveToNextDay(endTime, workingHourStartTime, workingHourEndTime);
                continue;
            }

            // Calculating the number of hours the employee can work on this day
            int hoursAvailable = getHoursAvailable(endTime, workingHourStartTime, workingHourEndTime);

            // If the task can be completed on this day, return the end time
            if (remainingTime <= hoursAvailable) {
                endTime.add(Calendar.HOUR, remainingTime);
                return endTime;
            }

            // If the task cannot be completed on this day
            // subtract the number of hours worked from the time require and move to the next day
            remainingTime -= hoursAvailable;
            moveToNextDay(endTime, workingHourStartTime, workingHourEndTime);
        }
        return endTime;
    }

    /**
     * This method moves the current Calendar time one day ahead to the start of an employee's working hour but,
     * if the work ending hour of an employee is less than the start hour then this method just sets the current
     * Calendar time to midnight, for handling the case where an employee working hour crosses the midnight
     * @param curTime the current Calendar time
     * @param workingHourStartTime the starting work time of an employee
     * @param workingHourEndTime the ending work time of an employee
     */
    private static void moveToNextDay(Calendar curTime, Integer workingHourStartTime, Integer workingHourEndTime) {
        curTime.add(Calendar.DAY_OF_MONTH, 1);
        if(workingHourEndTime < workingHourStartTime){
            curTime.set(Calendar.HOUR_OF_DAY, 0);
        } else {
            curTime.set(Calendar.HOUR_OF_DAY, workingHourStartTime);
        }
    }

    /**
     * This methods calculates the number of hours an employee can work on the current day to complete the task
     * @param curTime the current calendar date, time
     * @param workingHourStartTime the starting work time of an employee
     * @param workingHourEndTime the ending work time of an employee
     * @return the number of hours an employee can work on this day
     */
    private static int getHoursAvailable(Calendar curTime, Integer workingHourStartTime, Integer workingHourEndTime) {
        if(workingHourEndTime < workingHourStartTime){
            return workingHourEndTime + (TOTAL_HOURS_IN_A_DAY - workingHourStartTime);
        }
        return workingHourEndTime - workingHourStartTime;
    }

    /**
     * This method converts the 12-hour string time into 24-hour integer time format
     * @param workingHour the string time
     * @return the integer time in 24-hour format
     * @throws Exception
     */
    private static Integer convertTo24HourFormat(String workingHour) throws Exception {
        String[] time = workingHour.split(" ");
        Integer hour;
        try{
            // Handling the case of workingHour with format HH:MM
            hour = Integer.parseInt(time[0]);
        } catch (NumberFormatException e) {
            throw new Exception(e);
        }
        String hourFormat = time[1];

        if(!isTimeValid(hour, hourFormat)){
            throw new Exception("12 hour time format is required");
        }
        if(hourFormat.equalsIgnoreCase("PM")){
            hour += 12;
        }
        return hour;
    }

    /**
     * This method finds out if the current day is a leave day for an employee
     * @param leaves the list of calendar dates on which an employee doesn't work at all
     * @param curTime the current calendar date, time
     * @return the boolean value whether the current day is a leave day
     */
    private static boolean isEmployeeOnLeave(List<Calendar> leaves, Calendar curTime) {
        boolean isLeaveDay = false;
        // Hashing data structure can be used to reduce the look-up time
        for(Calendar leave: leaves){
            if (leave.get(Calendar.YEAR) == curTime.get(Calendar.YEAR) && leave.get(Calendar.MONTH) == curTime.get(Calendar.MONTH) && leave.get(Calendar.DAY_OF_MONTH) == curTime.get(Calendar.DAY_OF_MONTH)) {
                isLeaveDay = true;
                break;
            }
        }
        return isLeaveDay;
    }

    private static boolean isTimeValid(Integer hour, String hourFormat){
        // if the time is in 24 hour format or any invalid time, then return false
        if(!isHourValid(hour) || !isHourFormatValid(hourFormat)){
            return false;
        }
        return true;
    }

    private static boolean isHourValid(Integer hour){
        if(hour < 1 || hour > 12){
            return false;
        }
        return true;
    }

    private static boolean isHourFormatValid(String hourFormat){
        if(!(hourFormat.equalsIgnoreCase("AM") || hourFormat.equalsIgnoreCase("PM"))){
            return false;
        }
        return true;
    }

    private static String getFormattedString(Calendar endTime){
        StringBuilder output = new StringBuilder("\n");
        output.append("Task Completion Date: " + endTime.get(Calendar.DAY_OF_MONTH) + "/" + endTime.get(Calendar.MONTH) + "/" + endTime.get(Calendar.YEAR) + "\n");
        output.append("Task Completion Time: " + endTime.get(Calendar.HOUR_OF_DAY) + ":" + endTime.get(Calendar.MINUTE) + ":" + endTime.get(Calendar.SECOND) + "\n");
        return output.toString();
    }

    private static Calendar getCompletionTimeFromUser() {
        Calendar startTime = Calendar.getInstance();

        // Taking input for the start date and time
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the start date (YYYY MM (0-11) DD): ");
        int year = scanner.nextInt();
        int month = scanner.nextInt();
        int day = scanner.nextInt();

        System.out.print("Enter the start time (HH (0-23) MM (0-59) SS)): ");
        int hour = scanner.nextInt();
        int minute = scanner.nextInt();
        int second = scanner.nextInt();

        // Setting the start time using the input values
        startTime.set(year, month, day, hour, minute, second);

        // Taking input for the time required for the task
        System.out.print("Enter the time required for the task (in hours): ");
        int timeRequiredForTask = scanner.nextInt();
        scanner.nextLine();

        // Taking input for the working hours
        System.out.print("Enter the start of the working hours (HH am/pm): ");
        String workingHourStart = scanner.nextLine();
        System.out.print("Enter the end of the working hours (HH am/pm): ");
        String workingHourEnd = scanner.nextLine();

        // Create a list for the leaves
        List<Calendar> leaves = new ArrayList<>();

        // Taking input for the number of leaves
        System.out.print("Enter the number of leaves: ");
        int numLeaves = scanner.nextInt();

        // Taking input for each leave date
        for (int i = 0; i < numLeaves; i++) {
            Calendar leave = Calendar.getInstance();
            System.out.print("Enter leave date (YYYY MM (0-11) DD): ");
            int y = scanner.nextInt();
            int m = scanner.nextInt();
            int d = scanner.nextInt();
            leave.set(y, m, d);
            leaves.add(leave);
        }
        return getEndTime(startTime, timeRequiredForTask, workingHourStart, workingHourEnd, leaves);
    }

}
