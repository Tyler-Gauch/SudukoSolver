/*
 * Object to hold information for each individual grid square for a puzzle
 */
public class SudokuSquare {
	public int column;
	public int row;
	public int section;
	public String value;
	public String options;
	
	public SudokuSquare(int column, int row, int section)
	{
		this.column = column;
		this.row = row;
		this.section = section;
		
		this.options = "123456789";
		this.value = "-";
	}
	
	public boolean equals(SudokuSquare s)
	{
		return this.column == s.column && this.row == s.row;
	}
	
	@Override
	public String toString(){
		return "("+column+","+row+") "+value+" "+ options;
	}
}
