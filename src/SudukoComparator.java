import java.util.Comparator;


public class SudukoComparator implements Comparator<SudukoSquare>{

	@Override
	public int compare(SudukoSquare a, SudukoSquare b) {
		
		return a.options.length() - b.options.length();
		
	}
	
}
