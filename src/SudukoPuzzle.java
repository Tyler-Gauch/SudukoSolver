
public class SudukoPuzzle {

	SudukoSquare[][] puzzle;
	
	private int size;
	private int sectionWidth;
	private int sectionHeight;
	public String options = "";
	
	public SudukoPuzzle(int[][] p)
	{
		this.size = p.length;
		this.options = "";
		
		for(int i = 0; i < this.size; i++)
		{
			this.options += i+1;
		}
		
		sectionHeight = size/3;
		sectionWidth = size/sectionHeight;
		puzzle = new SudukoSquare[size][size];
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
				puzzle[y][x] = new SudukoSquare(x, y, currentSection);
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
		String output = " |------------------------------------------------------------------------------------------------------\n";
		
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
