/*
 * String Game - Scrabble
 * Name:    Harry Zhang
 * Class:   ICS3U1-03
 * Teacher: Ms.Strelkovska
 * Date:    Dec. 9
 * 
 */

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class ScrabblePlayer extends ScrabbleMain {

	int score = 0;
	ArrayList<Point> newThisTurn = new ArrayList<Point>();
	Character[] hand = {GB_NOCHAR, GB_NOCHAR, GB_NOCHAR, GB_NOCHAR, GB_NOCHAR, GB_NOCHAR, GB_NOCHAR};
	
	int drawCards() {
		boolean gameOver = true;
		for (int i = 0; i < 7; i++) {
			if (hand[i] != GB_NOCHAR) 
				gameOver = false;
			if (hand[i] == GB_NOCHAR && bag.size() > 0) {
				hand[i] = bag.remove((int)(Math.random() * bag.size()));
				gameOver = false;
			}
		}
		return gameOver ? PL_GAMEOVER : PL_SUCCESS;
	}	
		
	int endTurn() {
		int code = PL_SUCCESS;
		if (newThisTurn.size() >= 1) {
			TreeSet<Integer> playedV = getPlayedCoords(true);
			TreeSet<Integer> playedH = getPlayedCoords(false);

			boolean leftRight = isLeftRight(playedV, playedH);
			int topLeft = leftRight ? playedH.first() : playedV.first();
			int bottomRight = leftRight ? playedH.last() : playedV.last();
			boolean legalPlay = checkDirectionLegal(playedV, playedH);
			
			// Only continue if the direction is legal
			if (legalPlay) {
				legalPlay = checkPositionConnected(playedV, playedH, leftRight);
	
				// Only continue if the word position is legal
				if (legalPlay) {
					ArrayList<String> words = formWords(playedV, playedH, topLeft, bottomRight, leftRight);
					if (words.get(0).equals(ER_ILEGAL_POSITION + "")) {
						 code = ER_ILEGAL_POSITION;
						 legalPlay = false;
					} else if (words.size() <= 1) {
						 code = ER_ILEGAL_WORDS;
						 legalPlay = false;
					} else {
						int scoreThisTurn = Integer.parseInt(words.remove(words.size() - 1));
						ArrayList<String> wrong = checkWordsLegal(words, dict);
						legalPlay = (wrong.size() == 0 ? true : false);
						if (legalPlay) {
							score += scoreThisTurn;
							boolean bingo = false;
							if (newThisTurn.size() >= 7)
								bingo = true;
							for (int b = 0; b < 7; b++)
								if (hand[b] != GB_NOCHAR)
									bingo = false;
							score += bingo ? 50 : 0;
							claimBonus();
							code = drawCards();
						} else
							code = ER_ILEGAL_WORDS;
						System.out.println("Wrong words: " + (wrong.size() == 0 ? "None" : wrong));
					}
				} else
					code = ER_ILEGAL_POSITION;
			} else
				code = ER_ILEGAL_DIRECTION;
			
			// Catch any errors above
			if (!legalPlay) {
				turn--;
				undoTurn();
			}
		} else {
			if (turn == 1) {
				code = ER_SKIP_FRIST;
				turn--;
			} else
				code = PL_SKIP;
		}
		turn++;
		newThisTurn.removeAll(newThisTurn);
		return code;
	}
	
	int playChar(int v, int h, int i) {
		if (board[v][h] == 0) {
			board[v][h] = hand[i];
			newThisTurn.add(new Point(v, h));
			hand[i] = GB_NOCHAR;
			return PL_SUCCESS;
		}
		return ER_ILEGAL_PLACEMENT;
	}
	
	int deleteChar(int v, int h) {
		if (board[v][h] != 0 && newThisTurn.contains(new Point(v, h))) {
			char curChar = board[v][h];
			board[v][h] = 0;
			newThisTurn.remove(new Point(v, h));
			for (int i = 0; i < hand.length; i++)
				if (hand[i] == GB_NOCHAR) {
					hand[i] = curChar;
					break;
				}
			return PL_SUCCESS;
		} 
		if (board[v][h] == 0)
			return ER_ILEGAL_DELNOCHAR;
		return ER_ILEGAL_DELNOPERM;
	}

	TreeSet<Integer> getPlayedCoords (boolean vertical) {
		TreeSet<Integer> played = new TreeSet<Integer>();
		for (int i = 0 ; i < newThisTurn.size(); i++) {
			played.add(vertical ? newThisTurn.get(i).x : newThisTurn.get(i).y);
		}
		return played;
	}

	boolean checkDirectionLegal (TreeSet<Integer> playedV, TreeSet<Integer> playedH) {
		if (playedH.size() == 1 || playedV.size() == 1) {
			return true; 
		}
		return false;
	}
	
	boolean isLeftRight (TreeSet<Integer> playedV, TreeSet<Integer> playedH) {
		if (playedH.size() == 1)
			return false;
		return true;
	}
	
	boolean checkPositionConnected(TreeSet<Integer> playedV, TreeSet<Integer> playedH, boolean leftRight) {
		if (turn == 1) {
			if (playedV.contains(8) && playedH.contains(8)) {
				return true;
			}
		} else {
			ArrayDeque<Point> queue = new ArrayDeque<Point>();
			boolean[][] visited = new boolean[17][17];
			queue.add(newThisTurn.get(0));
			while(!queue.isEmpty()) {
				for (int a = 0 ; a < queue.size(); a++) {
					Point p = queue.pop();
					if (board[p.x][p.y] != 0 && !visited[p.x][p.y]) {
						if (!newThisTurn.contains(new Point(p.x, p.y))) {
							return true;
						}
						queue.add(new Point(p.x + 1, p.y));
						queue.add(new Point(p.x, p.y + 1));
						queue.add(new Point(p.x - 1, p.y));
						queue.add(new Point(p.x, p.y - 1));
					}
					visited[p.x][p.y] = true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @return ArrayList<String>, where Integer.parseInt(ArrayList<String>().get(ArrayList<String>().size() - 1)) is the score gained from those words
	 */
	ArrayList<String> formWords(TreeSet<Integer> playedV, TreeSet<Integer> playedH, int topLeft, int bottomRight, boolean leftRight) {
		ArrayList<String> words = new ArrayList<String>();
		int totalScore = 0;
		
		int fristWordScore = 0;
		int fristWordMultiplyer = 1;
		// Detect formed word
		String orderedLetters= "";
		int oC = leftRight ? playedV.first() : playedH.first();
		for (int i = topLeft; i <= bottomRight; i++) {
			int bonusAtLoc = leftRight ? BOARDBONUS[oC][i] : BOARDBONUS[i][oC];
			char letterAtLoc = leftRight ? board[oC][i] : board[i][oC];
			if (letterAtLoc == 0) {
				return new ArrayList<>(Arrays.asList(new String[]{ ER_ILEGAL_POSITION + "" }));
			}
			orderedLetters += letterAtLoc;
			if (!(leftRight ? boardUsed[oC][i] : boardUsed[i][oC]) && bonusAtLoc != 0) {
				if (bonusAtLoc == GB_3W) {
					fristWordMultiplyer *= 3;
					fristWordScore += WORTH.get(letterAtLoc);
				} else if (bonusAtLoc == GB_2W) {
					fristWordMultiplyer *= 2;
					fristWordScore += WORTH.get(letterAtLoc);
				} else if (bonusAtLoc == GB_3L) {
					fristWordScore += WORTH.get(letterAtLoc) * 3;
				} else if (bonusAtLoc == GB_2L) {
					fristWordScore += WORTH.get(letterAtLoc) * 2;
				}
			} else {
				fristWordScore += WORTH.get(letterAtLoc);
			}
		}
		
		// Find word extensions
		String wordProc = orderedLetters;
		int wPC = topLeft;
		while (true) {
			wPC--;
			int bonusAtLoc = leftRight ? BOARDBONUS[oC][wPC] : BOARDBONUS[oC][wPC];
			char letterAtLoc = (leftRight ? board[oC][wPC] : board[wPC][oC]);
			if (letterAtLoc != 0) {
				wordProc = letterAtLoc + wordProc;
				if (!(leftRight ? boardUsed[oC][wPC] : boardUsed[wPC][oC]) && bonusAtLoc != 0) {
					if (bonusAtLoc == GB_3W) {
						fristWordMultiplyer *= 3;
						fristWordScore += WORTH.get(letterAtLoc);
					} else if (bonusAtLoc == GB_2W) {
						fristWordMultiplyer *= 2;
						fristWordScore += WORTH.get(letterAtLoc);
					} else if (bonusAtLoc == GB_3L) {
						fristWordScore += WORTH.get(letterAtLoc) * 3;
					} else if (bonusAtLoc == GB_2L) {
						fristWordScore += WORTH.get(letterAtLoc) * 2;
					}
				} else {
					fristWordScore += WORTH.get(letterAtLoc);
				}
			} else
				break;
		}
		wPC = bottomRight;
		while (true) {
			wPC++;
			int bonusAtLoc = (leftRight ? BOARDBONUS[oC][wPC] : BOARDBONUS[wPC][oC]);
			char letterAtLoc = (leftRight ? board[oC][wPC] : board[wPC][oC]);
			if (letterAtLoc != 0) {
				wordProc += leftRight ? board[oC][wPC] : board[wPC][oC];
				if (!(leftRight ? boardUsed[oC][wPC] : boardUsed[wPC][oC]) && bonusAtLoc != 0) {
					if (bonusAtLoc == GB_3W) {
						fristWordMultiplyer *= 3;
						fristWordScore += WORTH.get(letterAtLoc);
					} else if (bonusAtLoc == GB_2W) {
						fristWordMultiplyer *= 2;
						fristWordScore += WORTH.get(letterAtLoc);
					} else if (bonusAtLoc == GB_3L) {
						fristWordScore += WORTH.get(letterAtLoc) * 3;
					} else if (bonusAtLoc == GB_2L) {
						fristWordScore += WORTH.get(letterAtLoc) * 2;
					}
				} else {
					fristWordScore += WORTH.get(letterAtLoc);
				}
			} else
				break;
		}

		if (wordProc.length() > 1) {
			words.add(wordProc);
			totalScore += fristWordScore * fristWordMultiplyer;
		}
		
		// Form the rest of the words
		for(int i = 0; i < newThisTurn.size(); i++) {
			int search = (leftRight ? newThisTurn.get(i).x : newThisTurn.get(i).y);
			int staticDir = (leftRight ? newThisTurn.get(i).y : newThisTurn.get(i).x);
			// Find most top-left letter
			while (true) {
				if ((leftRight ? board[search][staticDir] : board[staticDir][search]) != 0) {
					search--;
				} else
					break;
			}
			search++;
			// Create the word going form the top-left coordinate
			String makeWord = "";
			int curWordScore = 0;
			int curWordMultiplyer = 1;
			while (true) {
				int bonusAtLoc = (leftRight ? BOARDBONUS[search][staticDir] : BOARDBONUS[staticDir][search]);
				char letterAtLoc = (leftRight ? board[search][staticDir] : board[staticDir][search]);
				if (letterAtLoc != 0) {
					makeWord += letterAtLoc;
					if (!(leftRight ? boardUsed[search][staticDir] : boardUsed[staticDir][search]) && bonusAtLoc != 0) {
						if (bonusAtLoc == GB_3W) {
							curWordMultiplyer *= 3;
							curWordScore += WORTH.get(letterAtLoc);
						} else if (bonusAtLoc == GB_2W) {
							curWordMultiplyer *= 2;
							curWordScore += WORTH.get(letterAtLoc);
						} else if (bonusAtLoc == GB_3L) {
							curWordScore += WORTH.get(letterAtLoc) * 3;
						} else if (bonusAtLoc == GB_2L) {
							curWordScore += WORTH.get(letterAtLoc) * 2;
						}
					} else {
						curWordScore += WORTH.get(letterAtLoc);
					}
					search++;
				} else
					break;
			}
			// Remove any 1 letter words
			if (makeWord.length() > 1) {
				words.add(makeWord);
				totalScore += curWordScore * curWordMultiplyer;
			}
		}
		words.add(totalScore + "");
		return words;
	}
	
	void undoTurn() {
		for (int i = 0; i < newThisTurn.size(); i++) {
			char curChar = board[newThisTurn.get(i).x][newThisTurn.get(i).y];
			for (int c = 0; c < hand.length; c++)
				if (hand[c] == GB_NOCHAR) {
					hand[c] = curChar;
					break;
				}			
			board[newThisTurn.get(i).x][newThisTurn.get(i).y] = 0;
		}
	}
	
	ArrayList<String> checkWordsLegal (ArrayList<String> words, TreeSet<String> dict) {
		ArrayList<String> wrongWords = new ArrayList<String>();
		for (int i = 0; i < words.size(); i++)
			if (!dict.contains(words.get(i)))
				wrongWords.add(words.get(i));
		return wrongWords;
	}

	void claimBonus() {
		for (int i = 0; i < newThisTurn.size(); i++) {
			Point proc = newThisTurn.get(i);
			boardUsed[proc.x][proc.y] = true;
		}
	}
	
}
