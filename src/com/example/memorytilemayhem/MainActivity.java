/**
 * Welcome to Memory Tile Mayhem!
 *
 * This is a tile matching game where the user clicks on a 2 tiles and sees if
 * they match. If they do, they disappear, if they don't, they flip back over.
 * Game-play ends when the board is empty of tiles, which then a pop-up comes up
 * to display how well the user did. The user can see their score on the pop-up,
 * and the time it took them on the main board. The user can then choose to play
 * a new game with the same set-up, or quit the game. The user can switch to
 * another set-up of tiles at any time by selecting the drop down menu on the
 * board.
 *
 * Author: Ryan McLeod
 */
package com.example.memorytilemayhem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity {

    //Variables that are used for the rows and columns
    private static int ROW_COUNT = -1;
    private static int COL_COUNT = -1;
    private Context context;

    //Keep track of the images, including the array that holds the cards and the images
    private Drawable backImage;
    private int[][] cards;
    private List<Drawable> images;

    //Keeping track of the cards, and their listener
    private Card firstCard;
    private Card secondCard;
    private ButtonListener buttonListener;

    //Varaibles that keep coiunt for the number of matches the player needs to make
    private int matchCount;
    private int matchMax;

    //Varaible that keeps track of what package of pictures will be loaded when the game is played
    private String imageSelect = "sample";

    //Variables that keep track of the time, as well as a milliseconds to seconds modifier
    private long startTime;
    private long endTime;
    private long timeDifference;
    private long secondsInMilli = 1000;

    //Keeps track of the score, and sets the starting score for the user (used later)
    private int score;
    private int startingScore;
    private double difficulty;

    //Global variables used for setting up new games
    private int x;
    private int y;

	//Locks the screen when the user selects two images so they can't select more until the screen
    //resets
    private static Object lock = new Object();

    //The number of turns they user has taken playing the game
    int turns;

    //Creates the layout for the table on the game screen
    private TableLayout mainTable;

    //Runs the handler when the user selects two images
    private UpdateCardsHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creates the handler that gets used when playing the game
        handler = new UpdateCardsHandler();

        //Sets the image used for the 'tiles' to the icon image in the drawable
        backImage = getResources().getDrawable(R.drawable.icon);

        //Sets the button listener for wach card
        buttonListener = new ButtonListener();

        //Creates the main table layout that holds the cards
        mainTable = (TableLayout) findViewById(R.id.TableLayoutMain);
        context = mainTable.getContext();

        //Creates the picture package spinner and populates it with the string array 'pack'
        Spinner p = (Spinner) findViewById(R.id.packageSpinner);
        ArrayAdapter<?> packageadapter = ArrayAdapter.createFromResource(
                this, R.array.pack, android.R.layout.simple_spinner_item);
        packageadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        p.setAdapter(packageadapter);

        //Sets the package array so the options can change which images will be loaded in the image
        //array
        p.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> arg0,
                    View arg1, int pos, long arg3) {

                ((Spinner) findViewById(R.id.GameSpinner)).setSelection(0);

                switch (pos) {
                    case 1:
                        imageSelect = "sample";
                        break;
                    case 2:
                        imageSelect = "pokemon";
                        break;

                    default:
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

            }
        });

        //Creates the game package spinner and populates it with the string array 'type'
        Spinner s = (Spinner) findViewById(R.id.GameSpinner);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                this, R.array.type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        //Sets the game array so the options can choose which set-up of tiles the user want
        s.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> arg0,
                    View arg1, int pos, long arg3) {

                ((Spinner) findViewById(R.id.GameSpinner)).setSelection(0);

                //Selects the counts for the grid layout, the amount of matches the player needs,
                //and the difficulty multiplier used in the score
                switch (pos) {
                    case 1:
                        x = 4;
                        y = 4;
                        matchMax = 8;
                        difficulty = 1.0;
                        break;
                    case 2:
                        x = 4;
                        y = 5;
                        matchMax = 10;
                        difficulty = 1.1;
                        break;
                    case 3:
                        x = 4;
                        y = 6;
                        matchMax = 12;
                        difficulty = 1.3;
                        break;
                    case 4:
                        x = 5;
                        y = 6;
                        matchMax = 15;
                        difficulty = 1.5;
                        break;
                    case 5:
                        x = 6;
                        y = 6;
                        matchMax = 18;
                        difficulty = 1.7;
                        break;
                    case 6:
                        x = 6;
                        y = 7;
                        matchMax = 21;
                        difficulty = 2.0;
                        break;
                    default:
                        return;
                }
                
                //Creates a new game set-up with the x and y co-ordinates
                newGame(x, y);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

            }

        });
    }

    private void newGame(int c, int r) {
    	//Sets up the board with the x and y coordinates
        ROW_COUNT = r;
        COL_COUNT = c;
        matchCount = 0;
        cards = new int[COL_COUNT][ROW_COUNT];
        
        //Gets the images for the game loaded into the array
        loadImages();
        
        //Gets the time the user starts the game, and sets the base score to 1000
        startTime = new Date().getTime();
        startingScore = 1000;
        
        //Removes everything but the new game row from the table, then creates the game board
        //in its place
        mainTable.removeView(findViewById(R.id.TableRow03));
        mainTable.removeView(findViewById(R.id.TableRow04));

        TableRow tr = ((TableRow) findViewById(R.id.TableRow05));
        tr.removeAllViews();

        mainTable = new TableLayout(context);
        tr.addView(mainTable);

        for (int y = 0; y < ROW_COUNT; y++) {
            mainTable.addView(createRow(y));
        }

        firstCard = null;
        loadCards();

        turns = 0;
        ((TextView) findViewById(R.id.TextViewBlank)).setText("Tries: " + turns);

    }

    //Sets which images load into the images array, used when the game is created
    private void loadImages() {
        images = new ArrayList<Drawable>();

        if (imageSelect == "pokemon") {
            images.add(getResources().getDrawable(R.drawable.pcard1));
            images.add(getResources().getDrawable(R.drawable.pcard2));
            images.add(getResources().getDrawable(R.drawable.pcard3));
            images.add(getResources().getDrawable(R.drawable.pcard4));
            images.add(getResources().getDrawable(R.drawable.pcard5));
            images.add(getResources().getDrawable(R.drawable.pcard6));
            images.add(getResources().getDrawable(R.drawable.pcard7));
            images.add(getResources().getDrawable(R.drawable.pcard8));
            images.add(getResources().getDrawable(R.drawable.pcard9));
            images.add(getResources().getDrawable(R.drawable.pcard10));
            images.add(getResources().getDrawable(R.drawable.pcard11));
            images.add(getResources().getDrawable(R.drawable.pcard12));
            images.add(getResources().getDrawable(R.drawable.pcard13));
            images.add(getResources().getDrawable(R.drawable.pcard14));
            images.add(getResources().getDrawable(R.drawable.pcard15));
            images.add(getResources().getDrawable(R.drawable.pcard16));
            images.add(getResources().getDrawable(R.drawable.pcard17));
            images.add(getResources().getDrawable(R.drawable.pcard18));
            images.add(getResources().getDrawable(R.drawable.pcard19));
            images.add(getResources().getDrawable(R.drawable.pcard20));
            images.add(getResources().getDrawable(R.drawable.pcard21));
        } else {
            images.add(getResources().getDrawable(R.drawable.card1));
            images.add(getResources().getDrawable(R.drawable.card2));
            images.add(getResources().getDrawable(R.drawable.card3));
            images.add(getResources().getDrawable(R.drawable.card4));
            images.add(getResources().getDrawable(R.drawable.card5));
            images.add(getResources().getDrawable(R.drawable.card6));
            images.add(getResources().getDrawable(R.drawable.card7));
            images.add(getResources().getDrawable(R.drawable.card8));
            images.add(getResources().getDrawable(R.drawable.card9));
            images.add(getResources().getDrawable(R.drawable.card10));
            images.add(getResources().getDrawable(R.drawable.card11));
            images.add(getResources().getDrawable(R.drawable.card12));
            images.add(getResources().getDrawable(R.drawable.card13));
            images.add(getResources().getDrawable(R.drawable.card14));
            images.add(getResources().getDrawable(R.drawable.card15));
            images.add(getResources().getDrawable(R.drawable.card16));
            images.add(getResources().getDrawable(R.drawable.card17));
            images.add(getResources().getDrawable(R.drawable.card18));
            images.add(getResources().getDrawable(R.drawable.card19));
            images.add(getResources().getDrawable(R.drawable.card20));
            images.add(getResources().getDrawable(R.drawable.card21));
        }
    }

    //Sets up the cards in their position, making sure there is a match of numbers for there to
    //be a pair, then logs them so the user can make sure that everything works
    @SuppressLint("UseValueOf")
    private void loadCards() {
        try {
            int size = ROW_COUNT * COL_COUNT;

            Log.i("loadCards()", "size=" + size);

            ArrayList<Integer> list = new ArrayList<Integer>();

            for (int i = 0; i < size; i++) {
                list.add(new Integer(i));
            }

            Random r = new Random();

            for (int i = size - 1; i >= 0; i--) {
                int t = 0;

                if (i > 0) {
                    t = r.nextInt(i);
                }

                t = list.remove(t).intValue();
                cards[i % COL_COUNT][i / COL_COUNT] = t % (size / 2);

                Log.i("loadCards()", "card[" + (i % COL_COUNT)
                        + "][" + (i / COL_COUNT) + "]=" + cards[i % COL_COUNT][i / COL_COUNT]);
            }
        } catch (Exception e) {
            Log.e("loadCards()", e + "");
        }

    }

    //Creates a new row in the game, as well as populate them with buttons
    private TableRow createRow(int y) {
        TableRow row = new TableRow(context);
        row.setHorizontalGravity(Gravity.CENTER);

        for (int x = 0; x < COL_COUNT; x++) {
            row.addView(createImageButton(x, y));
        }
        return row;
    }
    
    //Creates the buttons that will appear in the row
    @SuppressWarnings("deprecation")
    private View createImageButton(int x, int y) {
        Button button = new Button(context);
        button.setBackgroundDrawable(backImage);
        button.setId(100 * x + y);
        button.setOnClickListener(buttonListener);
        return button;
    }

    //Implements the listener for each button to flip cards
    class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            synchronized (lock) {
                if (firstCard != null && secondCard != null) {
                    return;
                }
                int id = v.getId();
                int x = id / 100;
                int y = id % 100;
                turnCard((Button) v, x, y);
            }

        }

        //Checks to see if the user clicks the first card or the second card
        //If the user clicks a second button, it sets it up to check the two
        //cards. If it hits this option, it will use a timer to keep the user
        //from doing anything until it's over
        @SuppressLint("HandlerLeak")
        @SuppressWarnings("deprecation")
        private void turnCard(Button button, int x, int y) {
            button.setBackgroundDrawable(images.get(cards[x][y]));

            if (firstCard == null) {
                firstCard = new Card(button, x, y);
            } else {

                if (firstCard.x == x && firstCard.y == y) {
                    return; //the user pressed the same card
                }

                secondCard = new Card(button, x, y);

                turns++;
                ((TextView) findViewById(R.id.TextViewBlank)).setText("Tries: " + turns);

                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            synchronized (lock) {
                                handler.sendEmptyMessage(0);
                            }
                        } catch (Exception e) {
                            Log.e("E1", e.getMessage());
                        }
                    }
                };

                Timer t = new Timer(false);
                t.schedule(tt, 1300);
            }

        }

    }

    class UpdateCardsHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            synchronized (lock) {
                checkCards();
            }
        }

        //This checks whether or not the cards match. If they do, they will disappear. If they don't,
        //THey flip back over. If the user has found all matches, the pop-up will appear to say
        //how well the user did. Then they can select to play again, or leave the game
        @SuppressWarnings("deprecation")
        public void checkCards() {
            if (cards[secondCard.x][secondCard.y] == cards[firstCard.x][firstCard.y]) {
                firstCard.button.setVisibility(View.INVISIBLE);
                secondCard.button.setVisibility(View.INVISIBLE);
                matchCount++;
                if (matchCount == matchMax) {
                    endTime = new Date().getTime();
                    timeDifference = (endTime - startTime) / secondsInMilli;
                    score = (int) ((difficulty * startingScore) - (turns * 5) - (timeDifference));
                    ((TextView) findViewById(R.id.TextViewBlank)).setText("Tries: " + turns + "     Time: " + timeDifference + " seconds");
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("You Win!")
                            .setMessage("Your score was: " + score + "\nWould you like to play again?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    newGame(x, y);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    System.exit(0);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();

                }
            } else {
                secondCard.button.setBackgroundDrawable(backImage);
                firstCard.button.setBackgroundDrawable(backImage);
            }

            firstCard = null;
            secondCard = null;
        }
    }

}
