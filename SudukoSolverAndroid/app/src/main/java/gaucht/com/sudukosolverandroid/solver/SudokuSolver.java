package gaucht.com.sudukosolverandroid.solver;

/*
 * Backtracking sudoku solver, ported from the original desktop console version
 * (see /src/SudokuSolver.java at the repo root) with the same constraint-propagation
 * + backtracking algorithm. Differences from the desktop version are limited to what
 * running inside an Android app requires: no java.awt.Point (not available on Android),
 * no System.out/System.exit side effects, and a `solved` flag instead of killing the process.
 */
public class SudokuSolver {

    private final SudokuPuzzle puzzle;
    private final boolean solved;

    /*
     * Purpose:
     * 		Creates a SudokuSolver and attempts to solve the given puzzle
     * Parameters
     * 		puzzle: SudokuPuzzle to be solved
     */
    public SudokuSolver(SudokuPuzzle puzzle) {
        this.puzzle = puzzle;
        if (!this.checkOptions(puzzle)) {
            this.solved = false;
        } else {
            this.solved = this.solve(puzzle);
        }
    }

    public boolean isSolved() {
        return solved;
    }

    public SudokuPuzzle getPuzzle() {
        return puzzle;
    }

    /*
     * Purpose:
     * 		Check if we add a number to a square if it is a valid option.  Checks the row, column, and section for an occurrence of the selected value
     * Parameters:
     * 		s: A SudokuSquare with updated value to check
     * 		puzzle: SudokuPuzzle to check against
     * Returns:
     * 		true: if safe to put number in that square
     * 		false: if not safe to put number in that square
     */
    private boolean checkSafe(SudokuSquare s, SudokuPuzzle puzzle) {
        //Step 1: check current row for numbers
        //Step 2: check current column for numbers
        //Step 3: check current section for numbers

        //Step 1: row
        for (int x2 = 0; x2 < puzzle.getSize(); x2++) {

            SudokuSquare s2 = puzzle.getSquare(x2, s.row);
            if (!s2.equals(s) && !s2.value.equals("-") && s2.value.equals(s.value)) {
                return false;
            }
        }
        //Step 2: column
        for (int y2 = 0; y2 < puzzle.getSize(); y2++) {
            SudokuSquare s2 = puzzle.getSquare(s.column, y2);
            if (!s2.equals(s) && !s2.value.equals("-") && s2.value.equals(s.value)) {
                return false;
            }
        }
        //Step 3: section
        int[] start = this.getStartPointForSection(s.section, puzzle);

        for (int y2 = start[1]; y2 < start[1] + puzzle.getSectionHeight(); y2++) {
            for (int x2 = start[0]; x2 < start[0] + puzzle.getSectionWidth(); x2++) {
                SudokuSquare s2 = puzzle.getSquare(x2, y2);
                if (!s2.equals(s) && !s2.value.equals("-") && s2.value.equals(s.value)) {
                    return false;
                }
            }
        }

        return true;
    }

    /*
     * Purpose:
     * 		This is the initial test to find what options each square has available to it.  If we reach a square with only 1 option left we recurrsivly run the function
     * Parameters:
     * 		puzzle: SudokuPuzzle to parse
     * Return:
     * 		true: if we were able to find options for all squares
     * 		false: if we were able to find a square with no possible options.  if this happens the puzzle is not solveable.
     */
    private boolean checkOptions(SudokuPuzzle puzzle) {
        for (int y = 0; y < puzzle.getSize(); y++) {
            for (int x = 0; x < puzzle.getSize(); x++) {
                SudokuSquare s = puzzle.getSquare(x, y);
                String options = new String(puzzle.options);
                //TODO: check for options
                //Step 1: check current row for numbers
                //Step 2: check current column for numbers
                //Step 3: check current section for numbers

                //Step 1: row
                for (int x2 = 0; x2 < puzzle.getSize(); x2++) {

                    SudokuSquare s2 = puzzle.getSquare(x2, y);
                    if (!s2.value.equals("-")) {
                        options = options.replace(s2.value, "");
                    } else if (!s2.equals(s) && !s.value.equals("-") && s2.value.equals(s.value)) {
                        return false;
                    }
                }
                //Step 2: column
                for (int y2 = 0; y2 < puzzle.getSize(); y2++) {
                    SudokuSquare s2 = puzzle.getSquare(x, y2);
                    if (!s2.value.equals("-")) {
                        options = options.replace(s2.value, "");
                    } else if (!s2.equals(s) && !s.value.equals("-") && s2.value.equals(s.value)) {
                        return false;
                    }
                }
                //Step 3: section
                int[] start = this.getStartPointForSection(s.section, puzzle);

                for (int y2 = start[1]; y2 < start[1] + puzzle.getSectionHeight(); y2++) {
                    for (int x2 = start[0]; x2 < start[0] + puzzle.getSectionWidth(); x2++) {
                        SudokuSquare s2 = puzzle.getSquare(x2, y2);
                        if (!s2.value.equals("-")) {
                            options = options.replace(s2.value, "");
                        } else if (!s2.equals(s) && !s.value.equals("-") && s2.value.equals(s.value)) {
                            return false;
                        }
                    }
                }

                if (options.length() == 1 && s.value.equals("-")) {  //if we have a square that could only have 1 possibility recursively check options again with the new value
                    s.options = null;
                    s.value = options;
                    return this.checkOptions(puzzle);
                } else if (options.length() == 0 && s.value.equals("-")) { //we found a square with no options we have an invalid puzzle
                    return false;
                }

                if (s.value.equals("-")) {
                    s.options = options;
                } else {
                    s.options = null;
                }

            }
        }
        return true;
    }

    /*
     * Purpose:
     * 		Loop through the puzzle and find the next square with the least amount of options
     * Parameters:
     * 		puzzle: The SudokuPuzzle to search through
     * Return:
     * 		the next square to attempt to fill in.  If this is null there are no square left to fill.
     */
    private SudokuSquare getNextSquare(SudokuPuzzle puzzle) {
        SudokuSquare next = null;
        for (int y = 0; y < puzzle.getSize(); y++) {
            for (int x = 0; x < puzzle.getSize(); x++) {
                if (puzzle.getSquare(x, y).options == null) {
                    continue;
                }

                if (next == null || puzzle.getSquare(x, y).options.length() < next.options.length()) {
                    next = puzzle.getSquare(x, y);
                }
            }
        }
        return next;
    }

    /*
     * Purpose:
     * 		Solve the puzzle
     * Parameters:
     * 		puzzle: SudokuPuzzle to solve
     * Returns:
     * 		true: if we were able to solve the puzzle
     * 		false: if we wern't able to solve the puzzle
     */
    private boolean solve(SudokuPuzzle puzzle) {
        //return if we solved the puzzle
        if (puzzle.isSolved()) {
            return true;
        } else { //if we aren't solved
            //get the next square that has the lowest number of options available
            SudokuSquare next = this.getNextSquare(puzzle);
            if (next == null) {
                //if its null we have no squares to fill
                return false;
            }
            //get the available options
            String options = new String(next.options);
            for (int i = 0; i < options.length(); i++) {
                next.value = options.charAt(i) + ""; //get next available option
                next.options = null;                //mark it as set
                if (checkSafe(next, puzzle))    //see if we can put that number there
                {
                    //if we can try and solve the puzzle with that number in that position
                    if (solve(puzzle)) {
                        //if we solved woohoo return true
                        return true;
                    } else {
                        //if we didn't solve backtrack resetting the options and value
                        next.value = "-";
                        next.options = options;
                    }
                } else {
                    //if the number was not safe there backtrack resetting the options and value
                    next.value = "-";
                    next.options = options;
                }
            }
        }
        return false;
    }

    /*
     * Purpose:
     * 		Helper function to find the starting position of the square given what section we want
     * Parameters:
     * 		section: the integer value of which section, for example a 9x9 is broken into 9 sections of 3x3 grids
     * 		puzzle: the puzzle in which we are getting the section for
     * Result:
     * 		a {x, y} pair containing the indexes of the puzzle that start the section
     */
    private int[] getStartPointForSection(int section, SudokuPuzzle puzzle) {
        int startX = 0;
        int startY = 0;
        switch (section) {
            case 2:
                startX = puzzle.getSectionWidth();
                break;
            case 3:
                startX = puzzle.getSectionWidth() * 2;
                break;
            case 4:
                startY = puzzle.getSectionHeight();
                break;
            case 5:
                startX = puzzle.getSectionWidth();
                startY = puzzle.getSectionHeight();
                break;
            case 6:
                startX = puzzle.getSectionWidth() * 2;
                startY = puzzle.getSectionHeight();
                break;
            case 7:
                startY = puzzle.getSectionHeight() * 2;
                break;
            case 8:
                startX = puzzle.getSectionWidth();
                startY = puzzle.getSectionHeight() * 2;
                break;
            case 9:
                startX = puzzle.getSectionWidth() * 2;
                startY = puzzle.getSectionHeight() * 2;
                break;
        }
        return new int[]{startX, startY};
    }

}
