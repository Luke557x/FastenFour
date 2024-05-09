import java.util.Scanner;

class TextTools {
  public static Scanner in = new Scanner(System.in);

  //Takes the user's input and does nothing with it.
  public static void pressEnterKey()
  { 
    System.out.println("Press Enter key to continue...");
    String input = in.nextLine();
  }

  //psedou-clears the console. Pushes text up beyond sight. Can be seen if you scroll up though.
  public static void clearScreen()
  {  
	  for (int i = 0; i < 10; i++) {  
		  System.out.println();
	  }
		
  }
}