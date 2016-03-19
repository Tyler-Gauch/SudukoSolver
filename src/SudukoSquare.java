
public class SudukoSquare {
	public int column;
	public int row;
	public int section;
	public String value;
	public String options;
	
	public SudukoSquare(int column, int row, int section)
	{
		this.column = column;
		this.row = row;
		this.section = section;
		
		this.options = "123456789";
		this.value = "-";
	}
	
	public boolean equals(SudukoSquare s)
	{
		return this.column == s.column && this.row == s.row;
	}
	
	@Override
	public String toString(){
		return "("+column+","+row+") "+value+" "+ options;
	}
}
