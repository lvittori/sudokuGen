package generator;

import java.util.*;


public class SudokuGenerator {
	/**
	 * Erzeugt 4x4 or 9x9 Sudokus
	 * @param small erzeugt ein 4x4 Sudoku, falls der Parameter true ist, sonst ein 9x9 Sudoku
	 * @return das erzeugte Sudoku
	 */
	public static int[][] generate(boolean small) {
		int size = small?4:9;
		int sectionSize = small?2:3;
		int[][] sudoku = new int[size][size];
		List<Integer> zahlenListe = new ArrayList<>();
		for(int i = 1; i <= size; i++) {
			zahlenListe.add(i);
		}
		// Für 4x4 Sudokus darf nur 1 Teilquadrat befüllt werden
		for(int i = 0; i < sectionSize - (small?1:0); i++) {			
			//Zufälliges Befüllen eines Teilquadrates
			List<Integer> shuffleList = new ArrayList<>(zahlenListe);
			Collections.shuffle(shuffleList);
			for(int r = 0; r < sectionSize; r++) {
				for(int c = 0; c < sectionSize; c++) {
					sudoku[r + i*sectionSize][c + i*sectionSize] = shuffleList.remove(0);
				}
			}
		}		
		solve(sudoku);
		return sudoku;
	}
	

	/**
	 * Überprüft, ob ein bestimmtes Feld hinsichtlich aller Sudoku-Bedingungen (Zeilenbedingung,
	 * Spaltenbedingung und Teilbereich-Bedingung einen gültigen Wert enthält
	 * @param sudoku das zu überprüfende Sudoku
	 * @param row der Zeilenindex des zu überprüfenden Feldes 
	 * @param col der Spaltenindex des zu überprüfenden Feldes 
	 * @return true, wenn alle Bedingungen erfüllt sind
	 */
	public static boolean isValid(int[][] sudoku, int row, int col) {
		return SudokuGenerator.isRowValid(sudoku, row) &&
				SudokuGenerator.isColValid(sudoku, col) &&
				SudokuGenerator.isSectionValid(sudoku, row, col);
	}

	/**
	 * Überprüft, ob eine bestimmte Zeile eines Sudokus nur gültig gesetzte Werte enthält.
	 * @param sudoku das Sudoku
	 * @param row der Index der zu überprüfenden Zeile
	 * @return true, wenn in der zu überprüfenden Zeile keine doppelten Werte außer 0
	 * 						vorkommen.
	 */
	public static boolean isRowValid(int[][] sudoku, int row) {
		boolean[] numberPresent = new boolean[sudoku.length];
		for(int i = 0; i < sudoku[row].length; i++) {
			if(!SudokuGenerator.isFieldValid(sudoku, numberPresent, row, i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Überprüft, ob eine bestimmte Spalte eines Sudokus nur gültig gesetzte Werte enthält.
	 * @param sudoku das Sudoku
	 * @param col der Index der zu überprüfenden Spalte
	 * @return true, wenn in der zu überprüfenden Spalte keine doppelten Werte außer 0
	 * 						vorkommen.
	 */
	public static boolean isColValid(int[][] sudoku, int col) {
		boolean[] numberPresent = new boolean[sudoku.length];
		for(int i = 0; i < sudoku.length; i++) {
			if(!SudokuGenerator.isFieldValid(sudoku, numberPresent, i, col)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Überprüft, ob ein quadratischer Teilbereich eines Sudokus nur gültig gesetzte Werte
	 * enthält. Funktioniert nicht für nicht-quadratische Teilbereiche
	 * @param sudoku das Sudoku
	 * @param row der Zeilenindex eines Feldes, innerhalb des zu überprüfenden Teilbereichs
	 * @param col der Spaltenindex eines Feldes, innerhalb des zu überprüfenden Teilbereichs
	 * @return true, wenn in dem zu überprüfenden Teilbereich keine doppelten Werte außer 0
	 * 						vorkommen.
	 */
	public static boolean isSectionValid(int[][] sudoku, int row, int col) {
		int sectionSize = (int)Math.sqrt(sudoku.length); // funktioniert nur mit quadratischen Teilbereichen (also keine 6x6 - Sudokus)
		int minRowIndex = (row/sectionSize)*sectionSize;
		int minColIndex = (col/sectionSize)*sectionSize;
		boolean[] numberPresent = new boolean[sudoku.length]; // alle am Anfang false
		for(int r = minRowIndex; r < minRowIndex + sectionSize; r++) {
			for(int c = minColIndex; c < minColIndex + sectionSize; c++) {
				if(!SudokuGenerator.isFieldValid(sudoku, numberPresent, r, c)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Überprüft anhand eines gegebenen Sudokus und einer Liste an schon verwendeten Zahlen, ob 
	 * auf dem Feld eine schon verwendete Zahl steht
	 * @param sudoku das zu überprüfende Sudoku
	 * @param numberPresent gibt an welche Zahlen schon bei einer Prüfung als "vorhanden" 
	 * 						(= true) markiert wurden, wobei der um 1 verschobene Index die Zahl darstellt.
	 * 						D.h. wenn bei Index 0 true angegeben ist, dann war schon ein 1er in der
	 * 						Überprüfung vorhanden. Wenn dieser noch einmal auftaucht, ist das Sudoku-Feld
	 * 						(und damit das ganze Sudoku) nicht gültig.
	 * @param row der Zeilenindex des zu überprüfenden Feldes
	 * @param col der Spaltenindex des zu überprüfenden Feldes
	 * @return true, wenn auf dem Feld die Zahl 0 steht (für ein leeres Feld) oder wenn auf
	 * 						dem Feld eine Zahl steht, die im numberPresent-Array noch nicht als true
	 * 						(für "vorhanden") markiert ist.
	 */
	public static boolean isFieldValid(int[][] sudoku, boolean[] numberPresent, int row, int col) {
		int n = sudoku[row][col];
		if(n!=0) {
			if(numberPresent[n-1]) {
				return false;
			} else {
				numberPresent[n-1]=true;
			}
		}
		return true;
	}

	
	/**
	 * Versucht zu einem quadratischen Sudoku mit Hilfe eines rekursiven backtracking-
	 * Algorithmus eine Lösung zu finden. ACHTUNG! Dieser Algorithmus erkennt nicht, ob die
	 * Lösung eindeutig ist
	 * @param sudoku das zu lösende Sudoku
	 * @return true, wenn eine Lösung gefunden wurde.
	 */
	public static boolean solve(int[][] sudoku) {
		// Suche eine noch leere Stelle im Sudoku
		for(int r = 0; r < sudoku.length; r++) {
			for(int c = 0; c < sudoku[r].length; c++) {
				if(sudoku[r][c] == 0) {
					// Ausprobieren aller möglichen Werte auf Gültigkeit
					for(int n = 1; n<= sudoku.length; n++) {
						sudoku[r][c] = n;
						// Falls dieser gesetzte Wert gültig ist...
						if(SudokuGenerator.isValid(sudoku, r, c) &&
								//...und auch das weitere Lösen des Sudokus eine gültige Lösung ergibt
								SudokuGenerator.solve(sudoku)) {
							// kann die Methode mit einer Erfolgsmeldung aufhören
							return true;
						} // sonst versuche es mit einer neuen Zahl n
					}
					// Zurücksetzen des Wertes, da hier kein gültiger Wert gefunden wurde ..
					sudoku[r][c] = 0;
					// ... und Abbruch mit einer Falschmeldung, so dass im vorherigen Schritt
					// eine neue Zahl versucht werden kann (ein Schritt zurück in der Rekursion)
					return false;
				}
			}
		}
		// Falls wir bis hierher kommen, haben wir es geschafft
		return true;
	}
}
