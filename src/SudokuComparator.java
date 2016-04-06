import java.util.Comparator;


public class SudokuComparator implements Comparator<SudokuSquare>{

	@Override
	public int compare(SudokuSquare s, SudokuSquare s2) {
		// TODO Auto-generated method stub
		if(s.options == null)
		{
			return -100;
		}else if(s2.options == null)
		{
			return 0;
		}
		return s.options.length() - s2.options.length();
	}
}
