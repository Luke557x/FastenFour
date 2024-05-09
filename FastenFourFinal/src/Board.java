import java.util.ArrayList;

public class Board {
	
	//Coordinates start from UPPER LEFT hand corner of board, starting at 0, 0
	//A framework to hold information about any board-state.
	
	byte[][] grid; //stores all plays made
	int four = 4; // minimum chain length to win
	private byte latestX;
	private byte latestY;
	
	//CONSTRUCTOR
	//Makes a new, empty board when given its size. Empty spaces are marked by 0s in the grid array.
	Board(byte cols, byte rows) {
		this.grid = new byte[cols][rows]; // IMPORTANT <-- columns THEN rows
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; rows++) {
				grid[i][j] = 0;
			}
		}
	    this.latestX = 0;
	    this.latestY = 0;
	}
	
	//CONSTRUCTOR
	//Takes a board and makes a copy of it
	Board(Board b) {
		this.grid = new byte[b.getCols()][b.getRows()]; // IMPORTANT <-- columns THEN rows (first choose x, then y)
		for (int i = 0; i < b.getCols(); i++) {
			for (int j = 0; j < b.getRows(); j++) {
				grid[i][j] = b.getGrid()[i][j];
			}
		}
	    this.latestX = b.getLatestX();
	    this.latestY = b.getLatestY();
	}
	
	
	
	//GENERAL METHODS
	
	//Inputs: Column selected from dropping, 0 indexed. Player trying to drop a token.
	//Outputs: Whether it is possible to drop in the selected column (true/false)
	//Description: Attempts to place a token in the lowest available (highest indexed) slot in grid
	boolean drop(byte col, byte player) {
		boolean canDrop = this.getCols() > col && 0 <= col && /*containsByte(grid[col], (byte)0)*/ (int)grid[col][0] == 0;
		if (canDrop) {
			for (int row = this.getRows() - 1; row >= 0; row--) {
				if (grid[col][row] == 0) {
					grid[col][row] = player;
			        this.latestX = (byte)col;
			        this.latestY = (byte)row;
					break;
				}
			}
		}
		return canDrop;
	}
	
	//Inputs: None
	//Outputs: None
	//Description: Gives a small graphical display of the board
	void printBoard() {
		for (int i = 0; i < this.getRows(); i++) {
			System.out.print("|");
			for (int j = 0; j < this.getCols(); j++) {
				System.out.print(" ");
				if (grid[j][i] == 1) {
					System.out.print((char)0x25CB);
				} else if (grid[j][i] == 2) {
					System.out.print((char)0x25CF);
				} else {
					System.out.print(" ");
				}
			}
			System.out.println(" |");
		}
		for (int i = 0; i < this.getCols() + 1; i++) {
			System.out.print("- ");
		}
		System.out.println("-");
		System.out.print("| ");
		if (this.getCols() > 10) { //makes a second row for numbering if board is reasonably large
			for (int i = 0; i < this.getCols(); i++) {
				System.out.print(((i+1)/10) + " ");
			}
			System.out.println("|");
			System.out.print("| ");
		}
		for (int i = 0; i < this.getCols(); i++) {
			System.out.print((i%10 + 1) + " ");
		}
		System.out.println("|");
	}
	
	//Inputs: Array, desired value (byte)
	//Outputs: if the desired value is in array
	boolean containsByte(byte[] arr, byte item) {
		boolean contains = false;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == item) {
				contains = true;
				break;
			}
		}
		return contains;
	}
	
	//looks to see who won.
	public int winner() {
		return winner(true);
	}
	
	//See below Comments
	public int winner(boolean checkVert) {
		return winner(checkVert, (byte)0);
	}
	
	//Extra parameters used for Cpu calculations. Checks when four - subFromFour pieces are connected
	public int winner(boolean checkVert, byte subFromFour) { 
		//if checkVert is false, don't check to see if a player has won on a vertical axis.
		//subFromFour is the number that will be subtracted from four. useful for 'teaching' AIs to build.
		
		//Method for checking wins: Start at center, move in a direction, starting positive. When no more tokens of
		//the winner being checked are present, turn around and count up to 'four'
		/*
			 xo/yo direction system.
		     -----------------------
		     x and y can either be -1 (-), 0 (0), or 1 (+).
		     
		              xy
		         \    |     /
		          --  0-  +-      
		      --  -0  00  +0  --
		          -+  0+  ++
		         /    |     \
		     
		     only need to start moving in -+ and ++.
		     for vertical check just start counting straight down.
		 */
		byte dir = -1; //determines what direction is being checked
		byte linePos; //determines how far alone the directional line to start checking -starts 1 from origin.
		byte playerCheck = this.grid[latestX][latestY]; //what player is being checked for a win
		byte counted;
		boolean hasSpace = false; //used for when subFromFour is >0. If a win is being checked under four it should have space to continue to build.

		while(dir < 2) { // -1 = down left, 0 = down, 1 = down right. REMEMBER "DOWN" IS UP! Y=0 IS AT TOP OF BOARD
			linePos = 1;
			counted = 0;
			while (inBounds(latestX+dir*linePos,latestY+linePos) && playerCheck == this.grid[latestX+dir*linePos][latestY+linePos]) {
				linePos++;
			}
			if (inBounds(latestX+dir*linePos,latestY+linePos) && 0 == this.grid[latestX+dir*linePos][latestY+linePos]) {
				hasSpace = true;
			}
			linePos--; //to correct for going to far while counting
			while (inBounds(latestX+dir*linePos,latestY+linePos) && playerCheck == this.grid[latestX+dir*linePos][latestY+linePos] && counted < four) {
				linePos--; //now moving opposite direction
				counted++;
			}
			if (inBounds(latestX+dir*linePos,latestY+linePos) && 0 == this.grid[latestX+dir*linePos][latestY+linePos]) {
				hasSpace = true;
			}if (counted >= four-subFromFour && hasSpace) {
				return (int)playerCheck;
			}
			if (counted >= four) {
				return (int)playerCheck;
			}
			dir++;
			if (!checkVert) {
				dir++;
			}
		}
		
		//now checking for left/right
		linePos = 1;
		counted = 0;
		while (inBounds(latestX+linePos,latestY) && playerCheck == this.grid[latestX+linePos][latestY]) {
			linePos++;
		}
		if (inBounds(latestX+linePos,latestY) && 0 == this.grid[latestX+linePos][latestY]) {
			hasSpace = true;
		}
		linePos--; //to correct for going to far while counting
		while (inBounds(latestX+linePos,latestY) && playerCheck == this.grid[latestX+linePos][latestY] && counted < four) {
			linePos--; //now moving opposite direction
			counted++;
		}
		if (inBounds(latestX+linePos,latestY) && 0 == this.grid[latestX+linePos][latestY]) {
			hasSpace = true;
		}
		if (counted >= four-subFromFour && hasSpace) {
			return (int)playerCheck;
		}
		if (counted >= four) {
			return (int)playerCheck;
		}
		return 0;
	}
	
	public boolean inBounds(int x, int y) { //makes sure a given coordinate is actually on the board
		return (0 <= x && x < this.grid.length && 0 <= y && y < this.grid[0].length);
	}

	
	//GETTERS
	byte getRows() {
		return (byte)this.grid[0].length;
	}

	byte getCols() {
		return (byte)this.grid.length;
	}

	byte[][] getGrid() {
		return this.grid;
	}
	
	byte getLatestX() {
		return (byte)this.latestX;
	}
	byte getLatestY() {
		return (byte)this.latestY;
	}
	byte getFour() {
		return (byte)four;
	}
	
	//SETTERS
	//no setters
}
