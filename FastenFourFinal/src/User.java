import java.util.Scanner;

public class User extends Player {
	public static Scanner in = new Scanner(System.in);
	
	//Handles inputs from Human Users.
	
	User() {
		super();
		in = new Scanner(System.in);
	}
	void play(Board b, byte player) {
		System.out.println("Please enter the column you'd like to drop a piece in.");
		String input = "";
		input = in.nextLine();
		while (!isNumber(input) || !b.drop((byte)(Integer.valueOf(input) - 1), player)) {
            System.out.println("Invalid Number! Please enter a column number.");
            input = in.nextLine();
        }
	}

	public static boolean isNumber(String x) {
	    boolean tempBool = true;
	    try {
	        Integer.valueOf(x);
	    } catch(Exception e) {
	        tempBool = false;
	    }
	    return tempBool;
	}
}
