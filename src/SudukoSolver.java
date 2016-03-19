import java.util.PriorityQueue;


public class SudukoSolver {

	public SudukoSolver(SudukoPuzzle puzzle)
	{		
		System.out.println("Attempting to Solve: ");
		System.out.println(puzzle.toString());
		if(!this.checkOptions(puzzle)){
			System.err.println("Invalid Puzzle");
			System.exit(-1);
		}
		
		System.out.println(puzzle.toString(true));
		
		long start = System.nanoTime();
		
		if(this.solve(puzzle)){
			long end = System.nanoTime();
			System.out.println("Finshed in " + (end-start)/1000000.0);
			System.out.println("Solved");
		}else{
			System.out.println("Unable to Solve");
		}
		System.out.println(puzzle);
	}
	
	private boolean checkSafe(SudukoSquare s, SudukoPuzzle puzzle)
	{
		//TODO: check for options
		//Step 1: check current row for numbers
		//Step 2: check current column for numbers
		//Step 3: check current section for numbers
		
		//Step 1: row
		for(int x2 = 0; x2 < puzzle.getSize(); x2++){
		
			 SudukoSquare s2 = puzzle.getSquare(x2, s.row);
			 if(!s2.equals(s) && !s2.value.equals('-') && s2.value.equals(s.value))
			 {
				 return false;
			 }
		}
		//Step 2: column
		for(int y2 = 0; y2 < puzzle.getSize(); y2++){
			 SudukoSquare s2 = puzzle.getSquare(s.column, y2);
			 if(!s2.equals(s) && !s2.value.equals('-') && s2.value.equals(s.value))
			 {
				 return false;
			 }
		}
		//Step 3: section
		int startX = 0;
		int startY = 0;
		switch(s.section){
			case 2:
				startX = 3;
				break;
			case 3:
				startX = 6;
				break;
			case 4:
				startY = 3;
				break;
			case 5:
				startX = 3;
				startY = 3;
				break;
			case 6:
				startX = 6;
				startY = 3;
				break;
			case 7:
				startY = 6;
				break;
			case 8:
				startX = 3;
				startY = 6;
				break;
			case 9:
				startX = 6;
				startY = 6;
				break;
		}
		
		for(int y2 = startY; y2 < startY + puzzle.getSectionHeight(); y2++)
		{
			for(int x2 = startX; x2 < startX + puzzle.getSectionWidth(); x2++)
			{
				SudukoSquare s2 = puzzle.getSquare(x2, y2);
				if(!s2.equals(s) && !s2.value.equals('-') && s2.value.equals(s.value))
				 {
					 return false;
				 }
			}
		}

		return true;
	}
	
	private boolean checkOptions(SudukoPuzzle puzzle){
		for(int y = 0; y < puzzle.getSize(); y++)
		{
			for(int x = 0; x < puzzle.getSize(); x++)
			{
				SudukoSquare s = puzzle.getSquare(x, y);
				String options = "123456789";
				//TODO: check for options
				//Step 1: check current row for numbers
				//Step 2: check current column for numbers
				//Step 3: check current section for numbers
				
				//Step 1: row
				for(int x2 = 0; x2 < puzzle.getSize(); x2++){
				
					 SudukoSquare s2 = puzzle.getSquare(x2, y);
					 if(!s2.value.equals("-")){
						 options = options.replace(s2.value, "");
					 }else if(!s2.equals(s) && !s.value.equals("-") && s2.value.equals(s.value)){
						 return false;
					 }
				}
				//Step 2: column
				for(int y2 = 0; y2 < puzzle.getSize(); y2++){
					 SudukoSquare s2 = puzzle.getSquare(x, y2);
					 if(!s2.value.equals("-")){
						options = options.replace(s2.value, "");
					 }else if(!s2.equals(s) && !s.value.equals("-") && s2.value.equals(s.value)){
						 return false;
					 }
				}
				//Step 3: section
				int startX = 0;
				int startY = 0;
				switch(s.section){
					case 2:
						startX = 3;
						break;
					case 3:
						startX = 6;
						break;
					case 4:
						startY = 3;
						break;
					case 5:
						startX = 3;
						startY = 3;
						break;
					case 6:
						startX = 6;
						startY = 3;
						break;
					case 7:
						startY = 6;
						break;
					case 8:
						startX = 3;
						startY = 6;
						break;
					case 9:
						startX = 6;
						startY = 6;
						break;
				}
				
				for(int y2 = startY; y2 < startY + puzzle.getSectionHeight(); y2++)
				{
					for(int x2 = startX; x2 < startX + puzzle.getSectionWidth(); x2++)
					{
						SudukoSquare s2 = puzzle.getSquare(x2, y2);
						 if(!s2.value.equals("-")){
							options = options.replace(s2.value, "");
						 }else if(!s2.equals(s) && !s.value.equals("-") && s2.value.equals(s.value)){
							 return false;
						 }
					}
				}
				
				if(options.length() == 1 && s.value.equals("-"))
				{
					s.options = null;
					s.value = options;
					return this.checkOptions(puzzle);
				}else if(options.length() == 0 && s.value.equals("-"))
				{
					return false;
				}
				
				if(s.value.equals("-"))
				{
					s.options = options;
				}else{
					s.options = null;
				}
			
			}
		}
		return true;
	}
	
	private SudukoSquare getNextSquare(SudukoPuzzle puzzle){
		SudukoSquare next = null;
		for(int y = 0; y < puzzle.getSize(); y++)
		{
			for(int x = 0; x < puzzle.getSize(); x++){
				if(puzzle.getSquare(x, y).options == null)
				{
					continue;
				}
				
				if(next == null || puzzle.getSquare(x, y).options.length() < next.options.length())
				{
					next = puzzle.getSquare(x, y);
				}
			}
		}
		return next;
	}
	
	private boolean solve(SudukoPuzzle puzzle)
	{
		if(puzzle.isSolved())
		{
			return true;
		}
		else{
			SudukoSquare next = this.getNextSquare(puzzle);
			String options = new String(next.options);
			for(int i = 0; i < options.length(); i++)
			{
				next.value = options.charAt(i)+"";
				next.options = null;
				if(checkSafe(next, puzzle))
				{
					if(solve(puzzle))
					{
						return true;
					}else{
						next.value = "-";
						next.options = options;
					}
				}else{
					next.value = "-";
					next.options = options;
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		
//		SudukoPuzzle s = new SudukoPuzzle(new int[][]{
//				{0,0,2,0,0,5,0,7,9},
//				{1,0,5,0,0,3,0,0,0},
//				{0,0,0,0,0,0,6,0,0},
//				{0,1,0,4,0,0,9,0,0},
//				{0,9,0,0,0,0,0,8,0},
//				{0,0,4,0,0,9,0,1,0},
//				{0,0,9,0,0,0,0,0,0},
//				{0,0,0,1,0,0,3,0,6},
//				{6,8,0,3,0,0,4,0,0},
//				
//		});	
		
		SudukoPuzzle s = new SudukoPuzzle(new int[][]{
				{1,9,3,0,0,8,0,0,0},
				{0,0,0,3,0,0,0,0,0},
				{2,0,5,0,0,9,0,0,3},
				{0,0,0,9,3,0,4,0,0},
				{0,0,9,0,0,0,3,0,0},
				{0,0,7,0,2,6,0,0,0},
				{6,0,0,8,0,0,9,0,2},
				{0,0,0,0,0,4,0,0,0},
				{0,0,0,2,0,0,5,8,7},
				
		});	
		
		
		
		SudukoSolver solver = new SudukoSolver(s);
	}

}
