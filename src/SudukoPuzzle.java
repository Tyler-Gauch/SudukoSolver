
public class SudukoPuzzle {

	SudukoSquare[][] puzzle;
	
	private int size = 9;
	private int sectionWidth = 3;
	private int sectionHeight = 3;
	
	public SudukoPuzzle(int[][] p)
	{
		puzzle = new SudukoSquare[size][size];
		int yVar = 0;
		int xVar = 0;
		int currentSection = 1;
		for(int y = 0; y < size; y++)
		{
			yVar = 0;
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
				xVar = 0;
				if(x == sectionWidth)
				{
					currentSection++;
				}
				if(x == sectionWidth * 2)
				{
					currentSection++;
				}
				puzzle[y][x] = new SudukoSquare(x, y, currentSection);
				if(p[y][x] != 0)
				{
					puzzle[y][x].value = p[y][x]+"";
					puzzle[y][x].options = null;
				}
			}
		}
	}
	
	public int getSize(){
		return this.size;
	}
	
	public int getSectionWidth(){
		return this.sectionWidth;
	}
	
	public int getSectionHeight(){
		return this.sectionHeight;
	}
	
	public SudukoSquare getSquare(int x, int y)
	{
		return this.puzzle[y][x];
	}
	
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
	
	public String toString(boolean showOptions){
		String output = "   0         1         2            3         4         5            6         7         8\n |------------------------------------------------------------------------------------------------------\n";
		for(int y = 0; y < size; y++)
		{
			output += y+"| ";
			for(int x = 0; x < size; x++)
			{
				SudukoSquare s = puzzle[y][x];
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
