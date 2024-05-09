import java.util.Random;
public class QCpu extends Player {
	private static Random rand;
	final int MAX_DEPTH = 3; //values above 3 not recommended. This CPU is also recursive, but uses other metrics to be more effective than
							 //the standard cpu. See comments in Cpu.java for more.
	final boolean DEBUG = false;
	int randPerc;
	int randExtra;
	int randSpecial;
	int[] weights; //default building weights
	
	QCpu() {
		super();
		this.randPerc = 0;
		this.randExtra = 0;
		this.randSpecial = 0;
		this.weights = new int[4];
		this.weights[0] = 3;
		this.weights[1] = 20;
		this.weights[2] = 2;
		this.weights[3] = 1;
		
	}
	
	QCpu(int[] inweights) {
		super();
		this.randPerc = 0;
		this.randExtra = 0;
		this.randSpecial = 0;
		this.weights = inweights.clone();
	}
	
	QCpu(int randPerc, int randExtra, int randSpecial) {
		this.randPerc = randPerc;
		this.randExtra = randExtra;
		this.randSpecial = randSpecial;
		this.weights = new int[4];
		this.weights[0] = 3;
		this.weights[1] = 20;
		this.weights[2] = 2;
		this.weights[3] = 1;
	}
	
	void play(Board b, byte player) {
		b.drop((byte)getBestCol(b, player), player);
	}
	
	//assigns values to columns. heights value column gets played.
	//value of 0 means the column should not be played at all.
	//value of 1 means playing the column will lose the game
	//value of 100000 means the column should definitely be played if blocking or a winning move is not available
	//value of 100001 means the column should be played to block a player, and the method is in the top layer of computing
	//value of 100002 means the column should be played to win, and the method is in the top layer of computing
	int getBestCol(Board b, byte player) {
		int vals[] = new int[b.getCols()]; // list of columns' values
		int cols[] = new int[b.getCols()]; // list of columns
		byte notPlayer = (byte)(player%2+1); //the player that is not the inputted one (if p1 is inputed then p2 would be this var)
		
		for (byte i = 0; i < cols.length; i++) {
			cols[i] = i;
			Board vb = new Board(b);
			vals[i] = -1;
			if (b.getGrid()[i][0] != 2 && b.getGrid()[i][0] != 1) { //checks if column is full w/o dropping
				
				//most important checks at bottom, least important at top.
				vb.drop(i, notPlayer);
				if (vb.winner(false, (byte)1) == notPlayer) {
					vals[i] = 100_000;
				}
				
				//Checks "if the other player places here, will i lose?" if yes, then block it.
				//otherwise. if placing here would cause a loss, (other player places ontop of me and wins) then sets this to a last resort column (if all others are full)
				if (vb.winner() == notPlayer) {
					vals[i] = 100_001;
				} else {
					vb.drop(i, notPlayer);
					if (vb.winner(false) == notPlayer) {
						vals[i] = 1;
					}
				}
				
				//resets the virtual board and drops a token as the ai
				vb = new Board(b);
				vb.drop(i, player);
				
				//if dropping in the given col causes a win, play it above all else
				if (vb.winner()==player) {
					vals[i] = 100_002;
				}
				
				//this is the aforementioned "bottom". below is conditions for not simple win/lose scenarios.
				
				//here the value is chosen by looking for whatever dropping spot has the most area to build off of
				if (vals[i] < 0) {
					vals[i] = getBuildVal(b, i, player);
				}
				
			} else {
				vals[i] = 0;
			}
		}

		//to print arrays
		if (DEBUG) {
			for (int i = 0; i < b.getCols(); i++) {
				System.out.print(cols[i] + "  ");
			}
			System.out.println();
			for (int i = 0; i < b.getCols(); i++) {
				System.out.print(vals[i] + "  ");
			}
			System.out.println();
		}
		
		//sorts the vals array so the highest value col is first
		sortByVals(vals, cols);
		
		//if the first x values are the same, randomly pick one of those values. otherwise just return the first value.
		if (vals[0] == vals[1]) {
			int maxRand = 0;
			boolean check = true;
			
			while (check) {
				if (maxRand != cols.length-2 && vals[maxRand+1] == vals[maxRand]) {
					maxRand++;
				} else {
					check = false;
				}
			}
			maxRand+=(randExtra+1);
			if (maxRand > 7) {
				maxRand = 7;
			}
			
			int theColIndex = 0;
			
			do {
				theColIndex = (byte)(Math.random()*maxRand);
				maxRand--;
			} while (theColIndex > 0 && vals[theColIndex] == 0);
			
			return cols[theColIndex];
			
		} else {
			return cols[0];
		}
	}
	
	//returns a score between 0 and 100_000 of how good a spot is to propagate future building.
	//in short, the method looks at a given spot and looks in 6 directions (every one but up/down) and counts how many empty spots or owned spots
	//there are. The more there are, the better the building opportunity. Has some extra checks too.
	int getBuildVal(Board b, byte col, byte player) {
		int val = 0;
		byte[][] vG = b.getGrid(); //copy of board grid for easy use. do not alter.
		byte notPlayer = (byte)(player%2+1); //the player that is not the inputted one (if p1 is inputed then p2 would be this var)
		
		Board vb = new Board(b);
		vb.drop(col, player);

		//get the XY position of the center testing zone.
		int centX = vb.getLatestX();
		int centY = vb.getLatestY();
		
		//count total building space left and right of given coordinate and count total player tokens already at the location
		byte space = 0; // amount of open space  in building space
		byte bonus = 0; // amount of tokens currently belonging to the player that are in building space
		byte shadow = 0; // amount of spaces checked that have nothing right underneath them
		byte tokenShadow = 0; // amount of spaces checked that have the currently player's token right underneath them
		byte height = 0; //how far from the ground the token is
		
		for (byte i = -1; i < 2; i++) {
			byte[] data = getLineData(i, centX, centY, vG, b, player);
			if (data[0] > 2) {
				space+= data[0];
				bonus+= data[1];
				shadow+= data[2];
				tokenShadow+= data[3];
				height = data[4];
			}
			
		}
		
		//most possible space: 16
		//most possible shadow: 14
		int spaceWeight = weights[0];
		int bonusWeight = weights[1];
		int shadowWeight = weights[2];
		int tokenShadowWeight = weights[3];
		int totalWeight = (int)(spaceWeight+bonusWeight+shadowWeight+tokenShadowWeight);
		float heightMult = 1-(float)Math.sqrt(((float)height) / ((float)b.getRows()));
		val = 2+(int)(99_997f*Math.sqrt((space*spaceWeight/32f+
					 bonus*bonusWeight/32f+
					 shadow*shadowWeight/28f+
					 tokenShadow*tokenShadowWeight/28f)/(float)totalWeight));
		val *= heightMult;
		
		if (val < 0 || val > 100_000) {
			System.out.println("Strange build value found! value: " + val);
		}
		
		return val;
	}
	
	//returns data in order - space, bonus, shadow, tokShadow.
	//dir = -1 is going from 0 m to m 0... i think...
	//       0 is going from 0 c to m c.
	//       1 is going from 0 0 to m m...  i think...
	// c = center, m = max.
	private byte[] getLineData(byte dir, int inX, int inY, byte[][] g, Board b, byte player) {
		byte notPlayer = (byte)(player%2+1); //the player that is not the inputted one (if p1 is inputed then p2 would be this var)
		
		int curX = inX;
		int curY = inY;
		
		//count total building space left and right of given coordinate and count total player tokens already at the location
		byte space = 0; // amount of space or player tokens that are in building space
		byte bonus = 0; // amount of tokens currently belonging to the player that are in building space
		byte shadow = 0; // amount of spaces checked that have nothing right underneath them
		byte tokenShadow = 0; // amount of spaces checked that have the currently player's token right underneath them
		//finds left limit
		for (int x = 0; x < 3; x++) {
			if (!b.inBounds(curX-1, curY-dir) || g[curX-1][curY-dir] == notPlayer) {
				break;
			} 
			curX--;
			curY-=dir;
			if (b.inBounds(curX, curY) && g[curX][curY] == player) {
				bonus++;
			} else {
				space++;
			}
			if (b.inBounds(curX, curY-1) && g[curX][curY-1] == 0) {
				shadow++;
			} else if (b.inBounds(curX, curY-1) && g[curX][curY-1] == player) {
				tokenShadow++;
			}
		}
		
		curX = inX;
		curY = inY;
		
		//finds right limit
		for (int x = 0; x < 3; x++) {
			if (!b.inBounds(curX+1, curY+dir) || g[curX+1][curY+dir] == notPlayer) {
				break;
			} 
			curX++;
			curY+=dir;
			if (b.inBounds(curX, curY) && g[curX][curY] == player) {
				bonus++;
			} else {
				space++;
			}
			if (b.inBounds(curX, curY-1) && g[curX][curY-1] == 0) {
				shadow++;
			} else if (b.inBounds(curX, curY-1) && g[curX][curY-1] == player) {
				tokenShadow++;
			}
		}
		
		byte[] data = new byte[5];
		
		data[0] = space;
		data[1] = bonus;
		data[2] = shadow;
		data[3] = tokenShadow;
		data[4] = (byte)(g[0].length-b.getLatestY());//height
		
		return data;
	}
	
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
	
	int[] getWeights() {
		return weights;
	}
	
	String getWeightsString() {
		String data = "[";
		for (int i = 0; i < weights.length-1; i++) {
			data += weights[i] + ",";
			int whiteSpace = 10-(int)Math.log10(weights[i]);
			for (int j = 0; j < whiteSpace; j++) {
				data+= " ";
			}
		}
		data+= weights[weights.length-1] + "]";
		return data;
	}
}
