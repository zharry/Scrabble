
/*
 * String Game - Scrabble
 * Name:    Harry Zhang
 * Class:   ICS3U1-03
 * Teacher: Ms.Strelkovska
 * Date:    Dec. 9
 * 
 */

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ScrabbleMain {

	// Dictionary Constants
	static final String DICT_LOC = "dictionaries/dict";
	static final int DICT_NUM = 81;
	static final String DICT_TYPE = ".txt";

	// Game Board Constants
	static final int GB_3W = 4472;
	static final int GB_2W = 2983;
	static final int GB_3L = 8522;
	static final int GB_2L = 3723;
	static final int ER_ILEGAL_DIRECTION = 384924;
	static final int ER_ILEGAL_POSITION = 927482;
	static final int ER_ILEGAL_WORDS = 836472;
	static final int PL_SKIP = 738427;
	static final int PL_SUCCESS = 482775;
	static final int PL_GAMEOVER = 937598;
	static final int ER_ILEGAL_DELNOPERM = 347174;
	static final int ER_ILEGAL_DELNOCHAR = 328626;
	static final int ER_ILEGAL_PLACEMENT = 758395;
	static final int ER_SKIP_FRIST = 829652;
	static final char GB_NOCHAR = 62472;
	static final char GB_NOSEL = 38293;
	static final String GAME_TITLE = "Scrabble Game by Harry";

	// Game Variables
	static TreeSet<String> dict;
	static char[][] board = new char[17][17]; // Vertical, Horizontal
	static boolean[][] boardUsed = new boolean[17][17];
	static int turn = 1;
	static ArrayList<ScrabblePlayer> players = new ArrayList<ScrabblePlayer>();
	static int numPlayers = 0;
	static int curPlayer = 0;
	static int selected = GB_NOSEL; // In terms of 0-7 as in hand[]

	// GUI Variables
	static JButton[][] guiBoard = new JButton[17][17];
	static JButton[] actionTray = new JButton[11];
	static final int BUTTONWIDTH = 50, BUTTONHEIGHT = 50;
	static final int TRAYWIDTH = 75, TRAYHEIGHT = 75;
	static Font buttonNormal = new Font(Font.MONOSPACED, Font.BOLD, 13);
	static Font buttonBonus = new Font(Font.MONOSPACED, Font.ITALIC, 10);
	static JEditorPane leftDisplay = new JEditorPane("text/html", "");
	static JScrollPane leftDisplayScroll = new JScrollPane();

	public static void main(String[] args) throws Exception {

		// Show Welcome message
		showWelcome();

		// Get Players
		getPlayers();

		// Construct Dictionary
		dict = constructDictionary();

		// Run Main Game
		createWindow();
		redrawGame();
	}

	static void showWelcome() {
		JFrame temp = new JFrame(GAME_TITLE);
		temp.setUndecorated(true);
		temp.setVisible(true);
		temp.setLocationRelativeTo(null);
		Object[] options = { "Ok" };
		int returnCode = JOptionPane.showOptionDialog(null,
				"Welcome to " + GAME_TITLE + "!\n\n" + "Here's how to play:\n"
						+ "1. Click a letter in your hand to select or unselect it.\n"
						+ "2. Click a location on the board to use it or bring it back to your hand.\n"
						+ "3. When you are satisfied with your word, click 'Play'.\n"
						+ "4. If there is a problem, your letters will be returned to your hand.\n"
						+ "5. If the word is correct, pass the game to the next player.\n" + "\n\n"
						+ "In the next dialog, you will be able to select the number of players.\n"
						+ "Following your selection, it may take up to a 30 seconds for the dictionaries to load.\n\n"
						+ "Press 'Ok' to continue.\n\n",
				GAME_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		if (returnCode == -1)
			System.exit(0);
		temp.dispose();
	}

	static void getPlayers() {
		JFrame temp = new JFrame(GAME_TITLE);
		temp.setUndecorated(true);
		temp.setVisible(true);
		temp.setLocationRelativeTo(null);
		Object[] options = { 2, 3, 4 };
		int selectPlayers = JOptionPane.showOptionDialog(null, "Select number of players (pass and play):", GAME_TITLE,
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		if (selectPlayers == -1)
			System.exit(0);
		numPlayers = selectPlayers + 2;
		for (int i = 0; i < numPlayers; i++) {
			players.add(new ScrabblePlayer());
			players.get(i).drawCards();
		}
		temp.dispose();
	}

	static void endGame() {
		for (int i = 0; i < numPlayers; i++)
			for (int c = 0; c < 7; c++)
				if (players.get(i).hand[c] != GB_NOCHAR) {
					// Following National Scrabble Association Rules for ending
					// game
					players.get(curPlayer).score += WORTH.get(players.get(i).hand[c]) * 2;
				}
		Object[] options = { "Play Again", "Close" };
		int playAgain = JOptionPane.showOptionDialog(null, "Game Over", GAME_TITLE, JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		if (playAgain == 0) {
			board = new char[17][17]; // Vertical, Horizontal
			boardUsed = new boolean[17][17];
			bag = new ArrayList<Character>(Arrays.asList(STARTBAG));
			turn = 1;
			players = new ArrayList<ScrabblePlayer>();
			getPlayers();
			curPlayer = 0;
		} else {
			System.exit(0);
		}
	}

	static void createWindow() {
		JFrame frame = new JFrame(GAME_TITLE);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(15, 15));

		leftDisplay.setEditable(false);
		leftDisplay.setPreferredSize(new Dimension(300, 500));
		leftDisplayScroll.setViewportView(leftDisplay);
		leftDisplayScroll.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(1, 11));
		actionTray[1] = new JButton();
		actionTray[1].setText("<html>Player " + curPlayer + "<br/>Hand:</html>");
		// Letters in Hand
		for (int i = 2; i < 9; i++) {
			actionTray[i] = new JButton();
			actionTray[i].setBackground(new Color(194, 150, 110));
			final int trayButton = i - 2;
			actionTray[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (selected == trayButton)
						selected = GB_NOSEL;
					else
						selected = trayButton;
					redrawGame();
				}
			});
		}
		actionTray[9] = new JButton();
		actionTray[9].setEnabled(false);
		actionTray[10] = new JButton();
		actionTray[10].setText("Play");
		actionTray[10].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnCode = players.get(curPlayer).endTurn();
				if (returnCode == PL_GAMEOVER) {
					endGame();
				} else {
					if (returnCode == PL_SKIP || returnCode == PL_SUCCESS) {
						curPlayer = (curPlayer + 1) % numPlayers;
					}
					displayMessage(returnCode, true);
				}
				selected = GB_NOSEL;
				redrawGame();
			}
		});
		for (int i = 1; i < 11; i++) {
			bottomPanel.add(actionTray[i]);
			actionTray[i].setPreferredSize(new Dimension(TRAYWIDTH, TRAYHEIGHT));
		}

		for (int y = 1; y < 16; y++) {
			for (int x = 1; x < 16; x++) {
				guiBoard[y][x] = new JButton();
				panel.add(guiBoard[y][x]);
				guiBoard[y][x].setPreferredSize(new Dimension(BUTTONWIDTH, BUTTONHEIGHT));
				int pressedY = y, pressedX = x;
				guiBoard[y][x].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int returnCode;
						if (selected == GB_NOSEL)
							returnCode = players.get(curPlayer).deleteChar(pressedY, pressedX);
						else {
							returnCode = players.get(curPlayer).playChar(pressedY, pressedX, selected);
							selected = GB_NOSEL;
						}
						displayMessage(returnCode, false);
						redrawGame();
					}
				});
			}
		}

		frame.add(panel);
		frame.add(bottomPanel, "South");
		frame.add(leftDisplayScroll, "West");
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	static void redrawGame() {
		displayBoard();
		updateLeftDisplay();
		updateTray();
	}

	static void displayBoard() {
		for (int v = 1; v <= 15; v++) {
			for (int h = 1; h <= 15; h++) {
				char boardChar = board[v][h];
				guiBoard[v][h].setText("");
				guiBoard[v][h].setBackground(new Color(240, 240, 240));
				if (boardChar != 0) {
					guiBoard[v][h].setBackground(new Color(226, 192, 146));
					guiBoard[v][h].setFont(buttonNormal);
					guiBoard[v][h].setText(
							"<html>" + (boardChar + "").toUpperCase() + "<br/>" + WORTH.get(boardChar) + "</html>");
				} else if (BOARDBONUS[v][h] != 0) {
					guiBoard[v][h].setFont(buttonBonus);
					if (BOARDBONUS[v][h] == GB_3W) {
						guiBoard[v][h].setBackground(new Color(255, 160, 160));
						guiBoard[v][h].setText("3W");
					} else if (BOARDBONUS[v][h] == GB_2W) {
						guiBoard[v][h].setBackground(new Color(255, 210, 210));
						guiBoard[v][h].setText("2W");
					} else if (BOARDBONUS[v][h] == GB_3L) {
						guiBoard[v][h].setBackground(new Color(160, 160, 255));
						guiBoard[v][h].setText("3L");
					} else if (BOARDBONUS[v][h] == GB_2L) {
						guiBoard[v][h].setBackground(new Color(210, 210, 255));
						guiBoard[v][h].setText("2L");
					}
				}
			}
		}
	}

	static void updateLeftDisplay() {
		String output = "<html><div style='font-family:\"Trebuchet MS\", Helvetica, sans-serif; font-size: 9px;'>"
				+ "			<hr/>" + "			<div style='padding: 10px 10px 10px 15px;'>" + "				<table>"
				+ "					<tr><td><b style='font-size: 11px'>Scoreboard</b> (Turn " + turn + ")</td></tr>"
				+ "					<tr>" + "						<td>Player</td>"
				+ "						<td>Current Score</td>" + "					</tr>";
		for (int i = 0; i < players.size(); i++)
			output += "				<tr>" + "					<td>" + ((i == curPlayer) ? "<b><u>" : "") + (i)
					+ ((i == curPlayer) ? " (Current)</u></b>" : "") + "</td>" + "					<td><i>"
					+ players.get(i).score + "</i></td>" + "				</tr>";
		output += "				</table>" + "			</div>" + "			<hr/>"
				+ "			<div style='padding: 10px 10px 10px 15px;'>"
				+ "				<table style='border-collapse: collapse; border: 1px solid grey;'>"
				+ "					<tr><td><b style='font-size: 11px'>Letters</b></td></tr>"
				+ "					<tr>" + "						<td>Letter</td>"
				+ "						<td>Letter Score</td>" + "						<td>Amount in Bag</td>"
				+ "					</tr>";
		for (char i = 'a'; i <= 'z'; i++)
			output += "				<tr>" + "					<td>" + i + "</td>" + "					<td>"
					+ WORTH.get(i) + "</td>" + "					<td>" + Collections.frequency(bag, i) + "</td>"
					+ "				</tr>";
		output += "					<tr>" + "						<td>Letter</td>"
				+ "						<td>Letter Score</td>" + "						<td>Amount in Bag</td>"
				+ "					</tr>" + "				</table>" + "			</div>" + "			<hr/>"
				+ "		</div></html>";
		leftDisplay.setText(output);
		leftDisplay.setCaretPosition(0);
	}

	static void updateTray() {
		actionTray[1].setText("<html>Player " + curPlayer + "<br/>Hand:</html>");
		Character[] hand = players.get(curPlayer).hand;
		for (int i = 2; i <= 8; i++) {
			if (i - 2 == selected) {
				actionTray[i].setBackground(new Color(150, 115, 75));
			} else if (hand[i - 2] == GB_NOCHAR) {
				actionTray[i].setBackground(new Color(240, 240, 240));
				actionTray[i].setEnabled(false);
				actionTray[i].setText("");
			} else {
				actionTray[i].setBackground(new Color(194, 150, 110));
				actionTray[i].setEnabled(true);
				actionTray[i].setText(
						"<html>" + (hand[i - 2] + "").toUpperCase() + "<br/>" + WORTH.get(hand[i - 2]) + "</html>");
			}
		}
	}

	static TreeSet<String> constructDictionary() throws IOException {
		System.out.print("Loading Dictionary.");
		TreeSet<String> dict = new TreeSet<String>();
		for (int i = 1; i <= DICT_NUM; i++) {
			System.out.print(".");
			BufferedReader in = new BufferedReader(new FileReader(DICT_LOC + " (" + i + ")" + DICT_TYPE));
			//BufferedReader in = new BufferedReader(new InputStreamReader(
			//		ScrabbleMain.class.getClass().getResourceAsStream(DICT_LOC + " (" + i + ")" + DICT_TYPE)));
			String str = "";
			while ((str=in.readLine())!=null)
				dict.add(str);
			in.close();
		}
		System.out.println("Complete!");
		return dict;
	}

	static void displayMessage(int code, boolean forceIfNoErr) {
		String msg = "";
		if (code == ER_ILEGAL_DIRECTION) {
			msg = ("Error: Ilegal Direction or non interconnected pieces!\nPlease try again!");
		} else if (code == ER_ILEGAL_POSITION) {
			msg = ("Error: Ilegal Position, word is not connected to other words!\nPlease try again!");
		} else if (code == ER_ILEGAL_WORDS) {
			msg = ("Error: One or more words are incorrect!\nPlease try again!");
		} else if (code == PL_SKIP) {
			msg = ("No pieces played, skipping turn!\nPlease pass control to player " + curPlayer + ".");
		} else if (code == ER_ILEGAL_DELNOPERM) {
			msg = ("Error: Character is already permenantly placed and cannot be modified!\nPlease try again!");
		} else if (code == ER_ILEGAL_DELNOCHAR) {
			msg = ("Error: Please select a character form your hand to play at that location!\nPlease try again!");
		} else if (code == ER_ILEGAL_PLACEMENT) {
			msg = ("Error: There is already a character at that location!\nPlease try again!");
		} else if (code == PL_SUCCESS) {
			msg = ("Please pass control to player " + curPlayer + ".");
		} else if (code == ER_SKIP_FRIST) {
			msg = ("Error: You must play a word on the frist turn!\nPlease try again!");
		}
		if (!(code == PL_SUCCESS || code == PL_SKIP) || forceIfNoErr) {
			Object[] options = { "Ok" };
			JOptionPane
					.showOptionDialog(null, msg, GAME_TITLE,
							JOptionPane.DEFAULT_OPTION, (code == PL_SUCCESS || code == PL_SKIP)
									? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE,
							null, options, options[0]);
		}
	}

	static final HashMap<Character, Integer> WORTH = new HashMap<Character, Integer>() {
		{
			put('a', 1);
			put('b', 3);
			put('c', 3);
			put('d', 2);
			put('e', 1);
			put('f', 4);
			put('g', 2);
			put('h', 4);
			put('i', 1);
			put('j', 8);
			put('k', 5);
			put('l', 1);
			put('m', 3);
			put('n', 1);
			put('o', 1);
			put('p', 3);
			put('q', 10);
			put('r', 1);
			put('s', 1);
			put('t', 1);
			put('u', 1);
			put('v', 4);
			put('w', 4);
			put('x', 8);
			put('y', 4);
			put('z', 10);
		}
	};

	static final int[][] BOARDBONUS = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, GB_3W, 0, 0, GB_2L, 0, 0, 0, GB_3W, 0, 0, 0, GB_2L, 0, 0, GB_3W, 0 },
			{ 0, 0, GB_2W, 0, 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0, 0, GB_2W, 0, 0 },
			{ 0, 0, 0, GB_2W, 0, 0, 0, GB_2L, 0, GB_2L, 0, 0, 0, GB_2W, 0, 0, 0 },
			{ 0, GB_2L, 0, 0, GB_2W, 0, 0, 0, GB_2L, 0, 0, 0, GB_2W, 0, 0, GB_2L, 0 },
			{ 0, 0, 0, 0, 0, GB_2W, 0, 0, 0, 0, 0, GB_2W, 0, 0, 0, 0, 0 },
			{ 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0 },
			{ 0, 0, 0, GB_2L, 0, 0, 0, GB_2L, 0, GB_2L, 0, 0, 0, GB_2L, 0, 0, 0 },
			{ 0, GB_3W, 0, 0, GB_2L, 0, 0, 0, GB_2W, 0, 0, 0, GB_2L, 0, 0, GB_3W, 0 },
			{ 0, 0, 0, GB_2L, 0, 0, 0, GB_2L, 0, GB_2L, 0, 0, 0, GB_2L, 0, 0, 0 },
			{ 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0 },
			{ 0, 0, 0, 0, 0, GB_2W, 0, 0, 0, 0, 0, GB_2W, 0, 0, 0, 0, 0 },
			{ 0, GB_2L, 0, 0, GB_2W, 0, 0, 0, GB_2L, 0, 0, 0, GB_2W, 0, 0, GB_2L, 0 },
			{ 0, 0, 0, GB_2W, 0, 0, 0, GB_2L, 0, GB_2L, 0, 0, 0, GB_2W, 0, 0, 0 },
			{ 0, 0, GB_2W, 0, 0, 0, GB_3L, 0, 0, 0, GB_3L, 0, 0, 0, GB_2W, 0, 0 },
			{ 0, GB_3W, 0, 0, GB_2L, 0, 0, 0, GB_3W, 0, 0, 0, GB_2L, 0, 0, GB_3W, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

	static final Character[] STARTBAG = { 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'b', 'b', 'c', 'c', 'd', 'd',
			'd', 'd', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'f', 'f', 'g', 'g', 'g', 'h', 'h',
			'i', 'i', 'i', 'i', 'i', 'i', 'i', 'i', 'i', 'j', 'k', 'l', 'l', 'l', 'l', 'm', 'm', 'n', 'n', 'n', 'n',
			'n', 'o', 'o', 'o', 'o', 'o', 'o', 'o', 'o', 'p', 'p', 'q', 'r', 'r', 'r', 'r', 'r', 'r', 's', 's', 's',
			's', 't', 't', 't', 't', 't', 't', 'u', 'u', 'u', 'u', 'v', 'v', 'w', 'w', 'x', 'y', 'z' };

	static ArrayList<Character> bag = new ArrayList<Character>(Arrays.asList(STARTBAG));

}
