import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A Board implementation that holds cards and maintain their states.
 * 
 * @author Yasser Ghamlouch <a
 *         href="mailto:yasman8@gmail.com">mailto:yasman8@gmail.com</a>
 * 
 */
public class Board extends JPanel implements ActionListener {

 ////////////////////////////////////////////////////////////////////////////
 // Constants
 ////////////////////////////////////////////////////////////////////////////

 private static final String TAG = "Board: ";

 // Serial ID
 private static final long serialVersionUID = 1L;
 // Logic constants
 private static final int BOARD_BORDER_WIDTH = 20;
 private static final int MAX_NUM_OF_CARDS = 24;
 private static final int MIN_NUM_OF_CARDS = 1;
 private static final int NUMBER_OF_ROWS = 4;
 private static final int NUMBER_OF_COLUMNS = 6;
 private static final int NUMBER_OF_PAIRS = 12;

 private static final int MAX_SELECTED_CARDS = 2;
 private static final int FIRST = 0;
 private static final int SECOND = 1;
 private static final int VISIBLE_DELAY = (int) 2 * 1000;
 private static final int PEEK_DELAY = (int) 2 * 1000;

 // Card types
 private static final int EMPTY_CELL_TYPE = 0;
 private static final int HIDDEN_CARD_TYPE = 26;
 private static final int EMPTY_CARD_TYPE = 25;

 // Card image file properties
 private static final String DEFAULT_IMAGE_FILENAME_SUFFIX = ".jpg";
 private static final String DEFAULT_IMAGE_FILENAME_PREFIX = "img-";
 private static final String DEFAULT_IMAGE_FOLDER = "/images/";
 private static final String HIDDEN_IMAGE_PATH = DEFAULT_IMAGE_FOLDER
   + DEFAULT_IMAGE_FILENAME_PREFIX + "26"
   + DEFAULT_IMAGE_FILENAME_SUFFIX;
 private static final String EMPTY_IMAGE_PATH = DEFAULT_IMAGE_FOLDER
   + DEFAULT_IMAGE_FILENAME_PREFIX + "25"
   + DEFAULT_IMAGE_FILENAME_SUFFIX;

 ////////////////////////////////////////////////////////////////////////////
 // Static variables
 ////////////////////////////////////////////////////////////////////////////

 private static ArrayList<Cell> chosenCards = new ArrayList<Cell>();
 private static int numOfMatchedPairs = 0;
 private static int numOfFailedAttempts = 0;
 private static int selectedCards = 0;

 ////////////////////////////////////////////////////////////////////////////
 // Instance variables
 ////////////////////////////////////////////////////////////////////////////

 private Cell[][] mBoard = null;
 private String[] mCardStorage = initCardStorage();
 private Cell[] mCardChecker = new Cell[MAX_SELECTED_CARDS];

 ////////////////////////////////////////////////////////////////////////////
 // Constructor
 ////////////////////////////////////////////////////////////////////////////
 /**
  * Initialize a Board ready to be used for a game.
  */
 public Board() {
  super();

  setBackground(Color.WHITE);
  setBorder(BorderFactory.createEmptyBorder(BOARD_BORDER_WIDTH,
    BOARD_BORDER_WIDTH, BOARD_BORDER_WIDTH, BOARD_BORDER_WIDTH));
  setLayout(new GridLayout(NUMBER_OF_ROWS, NUMBER_OF_COLUMNS));

  mBoard = new Cell[NUMBER_OF_ROWS][NUMBER_OF_COLUMNS];

  for (int row = 0; row < NUMBER_OF_ROWS; row++) {
   for (int column = 0; column < NUMBER_OF_COLUMNS; column++) {
    mBoard[row][column] = new Cell(EMPTY_CELL_TYPE);
    mBoard[row][column].addActionListener(this);
    add(mBoard[row][column]);
   }
  }

  init();
 }

 ////////////////////////////////////////////////////////////////////////////
 // Public Interface
 ////////////////////////////////////////////////////////////////////////////

 /**
  * This method initializes the board with a new set of cards
  */
 public void init() {

  resetMatchedImages();
  resetBoardParam();
  peek();
  mCardStorage = initCardStorage();
  setImages();

 }

 /**
  * This method reinitializes the board with the current set of cards i.e.
  * replay
  */
 public void reInit() {

  resetMatchedImages();
  resetBoardParam();
  peek();
  setImages();

 }

 /**
  * This method checks if the board is solved or not.
  * 
  * @return true if the board is solved, false if there remains cards that
  *         have to be matched
  */
 public boolean isSolved() {

  // No check for null, the method can't be called out of
  // an instance, and the constructor initialize the mBoard

  for (int row = 0; row < NUMBER_OF_ROWS; row++) {
   for (int column = 0; column < NUMBER_OF_COLUMNS; column++) {
    if (!mBoard[row][column].isEmpty()) {
     return false;
    }
   } // column loop
  } // row loop

  return true;
 }

 /**
  * This method adds a selected card to the chosen card list
  * 
  * @param aCard
  *            is the card to be added to the list
  */

 private void addToChose(Cell aCard) {

  if (aCard != null) {
   if (!chosenCards.contains(aCard)) {
    chosenCards.add(aCard);
   }
  } else {
   error("addToChose( Cell ) received null.", true);
  }

 }

 /**
  * This method is the action performed when a card is clicked it represents
  * the main user interface of the game
  * 
  * @param e
  *            an ActionEvent
  */
 public void actionPerformed(ActionEvent e) {

  if (e == null) {
   error("actionPermormed(ActionEvent) received null", false);
   return;
  }

  // Flush out cases where we don't care
  if (!(e.getSource() instanceof Cell)) {
   return;
  }

  if (!isCardValid((Cell) e.getSource())) {
   return;
  }

  // Proceed with cases we want to cover

  ++selectedCards;

  if (selectedCards <= MAX_SELECTED_CARDS) {
   Point gridLoc = getCellLocation((Cell) e.getSource());
   setCardToVisible(gridLoc.x, gridLoc.y);
   mCardChecker[selectedCards - 1] = getCellAtLoc(gridLoc);
   addToChose(getCellAtLoc(gridLoc));
  }

  if (selectedCards == MAX_SELECTED_CARDS) {

   if (!sameCellPosition(mCardChecker[FIRST].getLocation(),
     mCardChecker[SECOND].getLocation())) {

    setSelectedCards(mCardChecker[FIRST], mCardChecker[SECOND]);
   } else {
    --selectedCards;
   }
  } // if selectedCards == MAX
 }

 ////////////////////////////////////////////////////////////////////////////
 // Utils Methods
 ////////////////////////////////////////////////////////////////////////////

 // This method returns the location of a Cell object on the board
 private Cell getCellAtLoc(Point point) {
  if (point == null) {
   error("getCellAtLoc( Point ) received null", true);
   return null;
  }

  return mBoard[point.x][point.y];
 }

 // This method sets a card to visible at a certain location
 private void setCardToVisible(int x, int y) {

  mBoard[x][y].setSelected(true);
  showCardImages();
 }

 // This method delays the setCards method, so the user can peek at the cards
 // before the board resets them
 private void peek() {

  Action showImagesAction = new AbstractAction() {

   private static final long serialVersionUID = 1L;

   public void actionPerformed(ActionEvent e) {
    showCardImages();
   }
  };

  Timer timer = new Timer(PEEK_DELAY, showImagesAction);
  timer.setRepeats(false);
  timer.start();
 }

 // This method sets the images on the board
 private void setImages() {

  ImageIcon anImage;

  for (int row = 0; row < NUMBER_OF_ROWS; row++) {
   for (int column = 0; column < NUMBER_OF_COLUMNS; column++) {

    URL file = getClass().getResource(
      DEFAULT_IMAGE_FOLDER
        + DEFAULT_IMAGE_FILENAME_PREFIX
        + mCardStorage[column
          + (NUMBER_OF_COLUMNS * row)]
        + DEFAULT_IMAGE_FILENAME_SUFFIX);

    if (file == null) {
     System.err.println(TAG
       + "setImages() reported error \"File not found\".");
     System.exit(-1);
    }

    anImage = new ImageIcon(file);

    mBoard[row][column].setIcon(anImage);

   } // column loop
  } // row loop
 }

 // This method shows a specific image at a certain location
 private void showImage(int x, int y) {

  URL file = getClass().getResource(
    DEFAULT_IMAGE_FOLDER + DEFAULT_IMAGE_FILENAME_PREFIX
      + mCardStorage[y + (NUMBER_OF_COLUMNS * x)]
      + DEFAULT_IMAGE_FILENAME_SUFFIX);

  if (file == null) {
   System.err.println(TAG
     + "showImage(int, int) reported error \"File not found\".");
   System.exit(-1);
  }

  ImageIcon anImage = new ImageIcon(file);
  mBoard[x][y].setIcon(anImage);

 }

 // This method sets all the images on the board
 private void showCardImages() {

  // For each card on the board
  for (int row = 0; row < NUMBER_OF_ROWS; row++) {
   for (int column = 0; column < NUMBER_OF_COLUMNS; column++) {

    // Is card selected ?
    if (!mBoard[row][column].isSelected()) {

     // If selected, verify if the card was matched by the user
     if (mBoard[row][column].isMatched()) {
      // It was matched, empty the card slot
      mBoard[row][column].setIcon(new ImageIcon(getClass()
        .getResource(EMPTY_IMAGE_PATH)));
      mBoard[row][column].setType(EMPTY_CARD_TYPE);
     } else {
      // It was not, put the "hidden card" image
      mBoard[row][column].setIcon(new ImageIcon(getClass()
        .getResource(HIDDEN_IMAGE_PATH)));
      mBoard[row][column].setType(HIDDEN_CARD_TYPE);
     }

    } else {
     // The card was not selected
     showImage(row, column);

     String type = mCardStorage[column
       + (NUMBER_OF_COLUMNS * row)];
     int parsedType = Integer.parseInt(type);

     mBoard[row][column].setType(parsedType);

    } // Is card selected?
   } // inner loop - columns
  } // outer loop - rows
 }

 // This method generates a random image, i.e. a random integer representing
 // the type of the image

 private String generateRandomImageFilename(int max, int min) {

  Random random = new Random();
  Integer aNumber = (min + random.nextInt(max));

  if (aNumber > 0 && aNumber < 10) {
   return "0" + aNumber;
  } else {
   return aNumber.toString();
  }
 }

 // This method creates an array of string holding the indices of 24 random
 // images grouped in pairs.

 private String[] initCardStorage() {

  String[] cardStorage = new String[MAX_NUM_OF_CARDS];
  String[] firstPair = new String[NUMBER_OF_PAIRS];
  String[] secondPair = new String[NUMBER_OF_PAIRS];

  firstPair = randomListWithoutRep();

  for (int i = 0; i < NUMBER_OF_PAIRS; i++) {
   cardStorage[i] = firstPair[i];
  }

  Collections.shuffle(Arrays.asList(firstPair));

  for (int j = 0; j < NUMBER_OF_PAIRS; j++) {
   secondPair[j] = firstPair[j];
  }

  for (int k = NUMBER_OF_PAIRS; k < MAX_NUM_OF_CARDS; k++) {
   cardStorage[k] = secondPair[k - NUMBER_OF_PAIRS];
  }

  return cardStorage;
 }

 // this method is to generate a list of NUMBER_OF_PAIRS images (types)
 // without repetition

 private String[] randomListWithoutRep() {

  String[] generatedArray = new String[NUMBER_OF_PAIRS];
  ArrayList<String> generated = new ArrayList<String>();

  for (int i = 0; i < NUMBER_OF_PAIRS; i++) {
   while (true) {
    String next = generateRandomImageFilename(MAX_NUM_OF_CARDS,
      MIN_NUM_OF_CARDS);

    if (!generated.contains(next)) {
     generated.add(next);
     generatedArray[i] = generated.get(i);
     break; // breaks back to "for" loop
    }
   } // inner loop - for every random card, ensure its not already
    // existing
  } // outer loop - we want NUMBER_OF_PAIRS different pairs

  return generatedArray;
 }

 // This method gets the location of a cell on the board and returns that
 // specific point
 private Point getCellLocation(Cell aCell) {

  if (aCell == null) {
   error("getCellLocation(Cell) received null", true);
   return null;
  }

  Point p = new Point();

  for (int column = 0; column < NUMBER_OF_ROWS; column++) {

   for (int row = 0; row < NUMBER_OF_COLUMNS; row++) {

    if (mBoard[column][row] == aCell) {
     p.setLocation(column, row);
     return p;
    }
   } // row for
  } // column for
  return null;
 }

 // This methods checks if 2 cards are the same
 private boolean sameCellPosition(Point firstCell, Point secondCell) {

  if (firstCell == null || secondCell == null) {
   if (secondCell == firstCell) {
    // They're equal if both are null
    return true;
   }

   if (firstCell == null) {
    error("sameCellPosition(Point, Point) received (null, ??)",
      true);
   }
   if (secondCell == null) {
    error("sameCellPosition(Point, Point) received (??, null)",
      true);
   }

   return false;
  }

  if (firstCell.equals(secondCell)) {
   return true;
  }
  return false;
 }

 // This method check if any 2 selected cards are the same so it replaces
 // them with a blank cell or if they're different it flips them back,
 // it also check if the board is solved
 private void setSelectedCards(Cell firstCell, Cell secondCell) {

  if (firstCell == null || secondCell == null) {

   if (firstCell == null) {
    error("setSelectedCards(Cell, Cell) received (null, ??)", true);
   }
   if (secondCell == null) {
    error("setSelectedCards(Cell, Cell) received (??, null)", true);
   }
   return;
  }

  if (firstCell.sameType(secondCell)) {

   firstCell.setMatched(true);
   secondCell.setMatched(true);
   firstCell.setSelected(false);
   secondCell.setSelected(false);
   showImage(getCellLocation(secondCell).x,
     getCellLocation(secondCell).y);
   peek();
   numOfMatchedPairs++;
   finalMessage();
  } else {

   firstCell.setMatched(false);
   secondCell.setMatched(false);
   firstCell.setSelected(false);
   secondCell.setSelected(false);
   showImage(getCellLocation(secondCell).x,
     getCellLocation(secondCell).y);
   peek();
   numOfFailedAttempts++;
  }
  resetSelectedCards();
 }

 // This method checks if a selected card is valid, the user isn't allowed to
 // select blank cells again
 private boolean isCardValid(Cell aCard) {

  if (aCard == null) {
   error("isCardValid(Cell) received null", false);
   return false;
  }

  if (!aCard.isEmpty()) {
   return true;
  } else {
   return false;
  }

 }

 // This method displays the results when the game is solved
 private void finalMessage() {

  @SuppressWarnings("serial")
  // Anonymous class are not to be serialized
  Action showImagesAction = new AbstractAction() {

   public void actionPerformed(ActionEvent e) {
    if (isSolved()) {

     Float numeralScore = (((float) numOfFailedAttempts) / ((float) MAX_NUM_OF_CARDS)) * 100;
     String textualScore = numeralScore.toString();

     JOptionPane.showMessageDialog(null,
       "Solved!! Your results:\n" + " Failed Attempts: "
         + numOfFailedAttempts
         + "\n Error percentage : " + textualScore
         + " %", "RESULTS",
       JOptionPane.INFORMATION_MESSAGE);
    } // if solved
   } // actionPerformed()
  }; // class implementation

  Timer timer = new Timer(VISIBLE_DELAY, showImagesAction);
  timer.setRepeats(false);
  timer.start();

 }

 // this method resets all the matched images, used in the replay method and
 // new game
 private void resetMatchedImages() {
  for (int row = 0; row < NUMBER_OF_ROWS; row++) {
   for (int column = 0; column < NUMBER_OF_COLUMNS; column++) {
    if (mBoard[row][column].isMatched()) {
     mBoard[row][column].setMatched(false);
    } // if
   } // for column
  } // for row
 }

 ////////////////////////////////////////////////////////////////////////////
 // Static methods
 ////////////////////////////////////////////////////////////////////////////

 /**
  * Error reporting.
  */
 private static void error(String message, boolean crash) {
  System.err.println(TAG + message);
  if (crash) {
   System.exit(-1);
  }
 }

 // This method resets the number of selected cards to 0 after 2 cards have
 // been chosen and checked
 private static void resetSelectedCards() {
  selectedCards = 0;
 }

 // This method resets the number of matched pairs on the board
 private static void resetNumMatchedCards() {
  numOfMatchedPairs = 0;
 }

 // This method resets the number of failed attempts
 private static void resetFailedAttempts() {
  numOfFailedAttempts = 0;
 }

 // This method resets the parameters of the board
 // used when replaying or when starting a new game
 private static void resetBoardParam() {

  resetFailedAttempts();
  resetNumMatchedCards();
 }

}
