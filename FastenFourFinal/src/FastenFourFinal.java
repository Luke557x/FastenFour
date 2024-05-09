import java.io.IOException;
import java.sql.Time;
import java.util.Scanner;

//TODO add quit game

public class FastenFourFinal {
	public static Scanner in = new Scanner(System.in);
	//add vars here
	public static int cols = 7;
	public static int rows = 6;  
	public static Board gameBoard;
	public static Player[] players;
	public static boolean[] isCPU;
	
	public static boolean DEBUG = false;

	public static void main(String[] args) throws IOException, InterruptedException {
		boolean retry = true;
	    while (retry) {
	    	//vars that get reset every game
	        int turn = 0;
	        int cpus = 0;
	        String input = "";
	        int intInput = 0;
	        int winner = 0;
	        players = new Player[2];
	        players[0] = new User();
	        players[1] = new User();
	        isCPU = new boolean[2];
	        isCPU[0] = false;
	        isCPU[1] = false;

	        //The Beginning
	        players[0] = new User();
	        players[1] = new User();
	        
	        System.out.println("Welcome to FastenFour!");
	        TextTools.pressEnterKey();

	        //Getting user's settings for the game (Start Game, Player Amount, Board Size)
	        String input0 = "";
	        while (!input0.equals("1")) {  //Game starts when input0 is "1"
	        	TextTools.clearScreen();
	        	System.out.println("What Would You Like to Do?");
	        	System.out.println("1: Start the Game!");
	        	System.out.println("2: Add a CPU.            (Currently " + cpus + " CPUs)");
	        	System.out.println("3: Change Board Size.    (Currently " + cols + "x" + rows + ")");
	        	System.out.println("4: Quit Game");



	            input0 = in.nextLine();
	            if (input0.equals("2")) { //Adds CPUS
	            	if (cpus < 2) {
	            		input0 = "";
	            		while (!input0.equals("1") && !input0.equals("2")) {
	            			
		            		System.out.println("What player should the CPU be?");
		            		System.out.println("1: Player 1");
		            		System.out.println("2: Player 2");
		            		input0 = in.nextLine();
		            		System.out.println();
		            		System.out.println("What type of CPU should it be?");
		            		System.out.println("1: Quick");
		            		System.out.println("2: Recursive");
		            		String input1 = in.nextLine();
		            		
		            		if ((input1.equals("1") || input1.equals("2") || input1.equals("3") || input1.equals("4")) && input0.equals("1")) {
		            			//Set up Player 1 as a CPU
		            			
		            			if (isCPU[0]) {
		            				System.out.println("This player is already a CPU!");
		            			} else {
		            				isCPU[0] = true;
		            				if (input1.equals("2")) {
			            				players[0] = new Cpu();
			    	            		System.out.println("CPU added!");
		            				} else if (input1.equals("1")) {
		            					players[0] = new QCpu();
		        	            		System.out.println("CPU added!");
		            				} else {
		            					isCPU[0] = false;
		            					System.out.println("Unrecognized input.");
		            				}
		            				cpus++;
		            			}
		            		} else if ((input1.equals("1") || input1.equals("2") || input1.equals("3") || input1.equals("4")) && input0.equals("2")) {
		            			//Set up Player 2 as a CPU
		            			
		            			if (isCPU[1]) {
		            				System.out.println("This player is already a CPU!");
		            			} else {
		            				isCPU[1] = true;
		            				if (input1.equals("2")) {
			            				players[1] = new Cpu();
			    	            		System.out.println("CPU added!");
		            				} else if (input1.equals("1")) {
		            					players[1] = new QCpu();
		        	            		System.out.println("CPU added!");
		            				} else {
		            					isCPU[1] = false;
		            					System.out.println("Unrecognized input.");
		            				}
		            				cpus++;
		            			}
		            		} else {
		            			System.out.println("Invalid Input!");
		    	                TextTools.pressEnterKey();
		            		}
	            		}
	            		input0 = "";
	                } else {
	                	System.out.println("Max CPUs reached!");
	                }
	                TextTools.pressEnterKey();
	            } else if (input0.equals("3")) { //Changes board size
	                System.out.println("Please Input Row Amount.");
	                rows = Integer.parseInt(in.nextLine());
	                System.out.println("Please Input Column Amount.");
	                cols = Integer.parseInt(in.nextLine());
	            } else if (input0.equals("q")) { //quickstart. Recursive CPU vs QCpu

	            	int[] inWeights0 = {3, 10, 2, 1};
	            	int[] inWeights1 = {3, 20, 2, 1};
	            	
	            	players[0] = new QCpu(inWeights0);
	            	players[1] = new QCpu(inWeights1);
	            	
	            	isCPU[0] = true;
	            	isCPU[1] = true;
	            	
	            	input0 = "1";
	            } else if (!input0.equals("1")) { //Error handling if user puts in something invalid
	                System.out.println("Invalid Input!");
	                TextTools.pressEnterKey();
	            } 
	        }
	        
            if (input0.equals("1")) {
	            //Setting up board
	            gameBoard = new Board((byte)cols, (byte)rows);
	            boolean tie = false;
	            
	            //The following while loop runs a game. Each time a loops a turn is played.
	            while (winner == 0 && !tie) {
	                TextTools.clearScreen();
	                int playerUp = (turn % 2) + 1;
	                turn++;
		            gameBoard.printBoard();
	                System.out.println(playerUp + "   Player " + playerUp  + "'s Turn!   " + playerUp);
	                players[playerUp-1].play(gameBoard, (byte)playerUp);
	                winner = gameBoard.winner();
	                Board vb = new Board(gameBoard);
	                for (int i = 0; i < gameBoard.getCols(); i++) {
	                	if (vb.drop((byte)i, (byte)3)) {
	                		break;
	                	} else if (i == gameBoard.getCols()-1) {
	                		tie = true;
	                		break;
	                	}
	                }
	            }
	            TextTools.clearScreen();
	            gameBoard.printBoard();
	            if (tie) {
	            	System.out.println("Its a tie!");
	            } else {
	                System.out.println("Player " + winner + " Wins!");
	            }
            }
            System.out.println("Type 'q' to quit, otherwise hit enter to play again.");
            input0 = in.nextLine();
            if (input0.equals("q")) {
            	System.out.println();
            	System.out.println("Thanks for Playing!");
            	retry = false;
            }
	    }
	}

}
