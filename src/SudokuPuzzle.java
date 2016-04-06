/*
 * This contains all the work to pull in and parse a sudoku puzzle that can be passed to the solver.
 */
public class SudokuPuzzle {

	SudokuSquare[][] puzzle;
	
	private int size;
	private int sectionWidth;
	private int sectionHeight;
	public String options = "";
	
	/*
	 * Purpose:
	 * 		Get an object representation of a puzzle and some relevent information such as section height and width and size
	 * Parameters:
	 * 		p:  A 2 dimensional array holding the numbers that make up the puzzle, 0 is a blank square
	 */
	public SudokuPuzzle(int[][] p)
	{
		this.size = p.length;
		this.options = "";
		
		for(int i = 0; i < this.size; i++)
		{
			this.options += i+1;
		}
		
		//create the puzzle as a 2 dimensional array of SudukoSquares and calculate the section height and width
		sectionHeight = size/3;
		sectionWidth = size/sectionHeight;
		puzzle = new SudokuSquare[size][size];
		int currentSection = 1;
		for(int y = 0; y < size; y++)
		{
			if(y >= sectionHeight && y < sectionHeight * 2)
			{
				currentSection=4;
			}
			else if(y >= sectionHeight *2)
			{
				currentSection=7;
			}else{
				currentSection = 1;
			}
			
			
			for(int x = 0; x < size; x++)
			{
				if(x == sectionWidth)
				{
					currentSection++;
				}
				if(x == sectionWidth * 2)
				{
					currentSection++;
				}
				puzzle[y][x] = new SudokuSquare(x, y, currentSection);
				if(p[y][x] != 0)
				{
					puzzle[y][x].value = p[y][x]+"";
					puzzle[y][x].options = null;
				}else{
					puzzle[y][x].options = new String(this.options);
				}
			}
		}
	}
	
	/*
	 * Getter for size
	 */
	public int getSize(){
		return this.size;
	}
	/*
	 * Getter for section width
	 */
	public int getSectionWidth(){
		return this.sectionWidth;
	}
	/*
	 * Getter for section height
	 */
	public int getSectionHeight(){
		return this.sectionHeight;
	}
	
	/*
	 * Getter for an individual square
	 */
	public SudokuSquare getSquare(int x, int y)
	{
		return this.puzzle[y][x];
	}
	
	/*
	 * Purpose:
	 * 		Check if the puzzle is solved
	 * Result:
	 * 		True if all options are filled false otherwise
	 */
	public boolean isSolved(){
		for(int y = 0; y < size; y++){
			for(int x = 0; x < size; x++){
				if(puzzle[y][x].options != null)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public String toString(){
		return toString(false);
	}
	
	/*
	 * Purpose:
	 * 		Print a text representation of the grid
	 * Parameters:
	 * 		showOptions: will show what the available options for each square are
	 * Results:
	 * 		A string representing the grid 
	 */
	public String toString(boolean showOptions){
		String output = " |------------------------------------------------------------------------------------------------------\n";
		
		for(int y = 0; y < size; y++)
		{
			output += y+"| ";
			for(int x = 0; x < size; x++)
			{
				SudokuSquare s = puzzle[y][x];
				String value = "";
				if(s.value.equals("-") && showOptions)
				{
					value = s.options;
				}else
				{
					value = s.value;
				}
				output += value;
				String spaces = "";
				for(int i = 0; i < 10-value.length(); i++)
				{
					spaces += " ";
				}
				output += spaces;
				if(x == sectionWidth-1 || x == sectionWidth * 2-1){
					output += " | ";
				}
			}

			output += "\n";
			if(y == sectionHeight-1 || y == sectionHeight * 2 -1)
			{
				output+=" |";
				output+="------------------------------------------------------------------------------------------------------\n";
			}else if(y != size -1)
			{
				output+=" |\n"; 
			}
		}
		return output;
	}
	
}
