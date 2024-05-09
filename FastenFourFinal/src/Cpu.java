import java.util.Random;
public class Cpu extends Player {
	private static Random rand;
	final int MAX_DEPTH = 2; //values above 3 not recommended. Determines how many times the bot recurrs before deciding on a move.
	final boolean DEBUG = false;
	float defMult; //A value 0.0 to 1.0 that determines how "defensive" a bot tries to play.
	float buildMult; //A value 0.0 to 1.0 that determines how much a bot will prioritize building coherent structures
	int randPerc; //A value from 0 to 100 denoting the % Chance that the AI has to make a random move. 0 by default.
	
	Cpu() {
		super();
		defMult = 0.2f; //default
		buildMult = 0.7f; //default
		this.randPerc = 0; //default
	}
	
	Cpu(float defMult, float buildMult) {
		super();
		this.defMult = defMult;
		this.buildMult = buildMult;
		this.randPerc = 0;
	}
	
	Cpu(int randPerc) {
		super();
		defMult = 0.2f;
		buildMult = 0.7f;
		this.randPerc = randPerc;
	}

	Cpu(float defMult, float buildMult, int randPerc) {
		super();
		this.defMult = defMult;
		this.buildMult = buildMult;
		this.randPerc = randPerc;
	}
	
	//setters for multipliers
	void setDefMult(float defMult) {
		this.defMult = defMult;
	}
	void setBuildMult(float buildMult) {
		this.buildMult = buildMult;
	}
	
	//getter for max depth
	int getMaxDepth() {
		return MAX_DEPTH;
	}
	
	//Call to have Cpu make a move on the board
	void play(Board b, byte player) {
		if (Math.random()>=randPerc/100f) {
			playWithDepth(b, player, MAX_DEPTH);
		} else {
			while (!b.drop((byte)(Math.random()*7), player)) {
			}
		}
	}
	
	//Uses a recursive mechanism (written in "getColVal" to decide where to play.
	void playWithDepth(Board b, byte player, int depth) {
		int[] cols = new int[b.getCols()];
		int[] vals = new int[b.getCols()];
		for (int i = 0; i < b.getCols(); i++) {
			cols[i] = i;
			vals[i] = getColVal(b, (byte)i, depth, true, player);
		}
		randomVals(vals, cols);
		sortByVals(vals, cols);
		
		//to print arrays if needed
		if (depth == MAX_DEPTH && DEBUG) {
			for (int i = 0; i < b.getCols(); i++) {
				System.out.print(cols[i] + "  ");
			}
			System.out.println();
			for (int i = 0; i < b.getCols(); i++) {
				System.out.print(vals[i] + "  ");
			}
			System.out.println();
		}
		b.drop((byte)cols[0], player);
	}
	
	//Main Cpu Functionality
	//assigns values to columns. heights value column gets played.
	//value of 0 means the column should not be played at all.
	//value of 1 means playing the column will lose the game
	//value of 100000 means the column should definitely be played if blocking or a winning move is not available
	//value of 100001 means the column should be played to block a player, and the method is in the top layer of computing
	//value of 100002 means the column should be played to win, and the method is in the top layer of computing
	int getColVal(Board b, byte col, int depth, boolean top, byte player) {
		int val = 0;
		Board vb = new Board(b); //vb = 'virtual board'
		if (vb.drop(col, player)) { //if it can drop...
			byte player2 = (byte)((player%2)+1);
			if (vb.winner() == player) {    //check if playing in the col gives the cpu a win
				if (top) {
					return 100002;
				} else {
					if (vb.winner(false) == player) {
						return 50000+(50000*(depth/MAX_DEPTH));
					} else {
						return 49000; //punishing CPU for aiming for a vertical win.
					}
				}
			} else if (vb.drop(col, player2) && vb.winner() == player2) { //checks if playing in the col with give other player a win
				return 1;
			}
			vb = new Board(b);
			vb.drop(col, player2);
			if (vb.winner() == player2) {//checks if the cpu needs to block the other player from winning.
				if (top) { 
					return 100001;
				} else {
					//if not in the top layer, force Cpu to only look down this line. This is a 'forced move'
					if (depth <= 0) {
						return 25000;
					} else {
						if (vb.winner(false) == player2) { //plays defensively only if the player is not aiming for a vertical win
							this.playWithDepth(vb, player2, depth-1);
							return (int)(100000*defMult + getColVal(vb, col, depth-1, false, player)*(1-defMult));
						} else {
							return (int)(getColVal(vb, col, depth-1, false, player)*(1-(defMult/2)));
						}
					}
				}
			} else if (vb.drop(col, player) && vb.winner() == player) { //checks if placing in a location would prevent the cpu from winning next turn
				return 5000;
			}
			if (depth <= 0) { //if this code runs, the search was completed and the recursive method collapses.
				val = 50000;
			} else {
				//Core of the CPU's recursive logic
				int openCols = 0;
				for (int i = 0; i < vb.getCols(); i++) {
					if (vb.drop((byte)i, player)) {
						openCols++;
						//at this point, the cpu has determined that it is possible to play in this column
						//all other checks related to immediate wins and losses have been done.
						int openCols2 = 0; //temporary used to count open vals after other players move
						int val2 = 0; //temporary val used to store value of one tree before they're added up
						for (int j = 0; j < vb.getCols(); j++) {
							vb = new Board(b);
							vb.drop((byte)i, player);
							if (vb.drop((byte)j, player2)) {
								openCols2++;
								val2+=getColVal(vb, col, depth-1, false, player);
							}
						}
					val += val2/openCols2;
					}
				}
				if (openCols == 0) {
					val = 25000;
				} else {
					val = val/openCols;
				}
			}
			//this four loop and the code within it adds value if the CPU is trying to build a line.
			vb = new Board(b);
			vb.drop(col, player);
			for (int i = 1; i < b.getFour()-2; i++) {
				if (vb.winner(false, (byte)i) == player) {
					val = val + (int)(buildMult/i*(100000-val)); //rewards CPU for trying to build a line. rewards get lesser quickly as attempted line length gets shorter
				}
				
			}
			
		} else {
			if (depth == MAX_DEPTH) {
				return 0;
			}
		}
		return val;
	}
	
	
	//simple sorting algorythm that sorts two parallel integer arrays by the order of the first one.
	static void sortByVals(int[] arr, int[] cols) {
        int temp = 0;
		for (int i = 0; i < arr.length - 1; i++) {
		    for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] < arr[j]) {
                    temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                    temp = cols[i];
                    cols[i] = cols[j];
                    cols[j] = temp;
                }
            }
		}
	}
	
	//scrambles the inputted arrays
	static void randomVals(int[] arr, int[] cols) {
        int temp = 0;
		for (int i = 0; i < arr.length - 1; i++) {
			rand = new Random();
			int j = rand.nextInt(arr.length);
            temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
            temp = cols[i];
            cols[i] = cols[j];
            cols[j] = temp;
		}
	}
}
