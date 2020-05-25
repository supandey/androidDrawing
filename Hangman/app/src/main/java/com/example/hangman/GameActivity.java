package com.example.hangman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;

import java.util.Random;

public class GameActivity extends Activity {
    //the words
    private String[] words;
    //random for word selection
    private Random rand;
    //store the current word
    private String currWord;
    //the layout holding the answer
    private LinearLayout wordLayout;
    //text views for each letter in the answer
    private TextView[] charViews;
    //letter button grid
    private GridView letters;
    //letter button adapter
    private LetterAdapter ltrAdapt;
    //body part images
    private ImageView[] bodyParts;
    //total parts
    private int numParts=6;
    //current part
    private int currPart;
    //num chars in word
    private int numChars;
    //num correct so far
    private int numCorr;
    //help
    private AlertDialog helpAlert;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Resources res = getResources();
        words = res.getStringArray(R.array.words);

        rand = new Random();
        currWord = "";

        wordLayout = (LinearLayout)findViewById(R.id.word);

        bodyParts = new ImageView[numParts];
        bodyParts[0] = findViewById(R.id.head);
        bodyParts[1] = findViewById(R.id.body);
        bodyParts[2] = findViewById(R.id.arm1);
        bodyParts[3] = findViewById(R.id.arm2);
        bodyParts[4] = findViewById(R.id.leg1);
        bodyParts[5] = findViewById(R.id.leg2);

        letters = (GridView)findViewById(R.id.letters);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        playGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_help:
                showHelp();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //play a new game
    private void playGame() {
        currPart=0;
        numChars=currWord.length();
        numCorr=0;

        for(int p = 0; p < numParts; p++) {
            bodyParts[p].setVisibility(View.INVISIBLE);
        }

        String newWord = words[rand.nextInt(words.length)];
        while(newWord.equals(currWord)) newWord = words[rand.nextInt(words.length)];
        currWord = newWord;

        charViews = new TextView[currWord.length()];
        wordLayout.removeAllViews();
        for (int c = 0; c < currWord.length(); c++) {
            charViews[c] = new TextView(this);
            charViews[c].setText(""+currWord.charAt(c));

            charViews[c].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            charViews[c].setGravity(Gravity.CENTER);
            charViews[c].setTextColor(Color.WHITE);
            charViews[c].setBackgroundResource(R.drawable.letter_bg);
            //add to layout
            wordLayout.addView(charViews[c]);
        }

        //reset adapter
        ltrAdapt=new LetterAdapter(this);
        letters.setAdapter(ltrAdapt);


    }

    public void letterPressed(View view) {
        //user has pressed a letter to guess
        String ltr=((TextView)view).getText().toString();
        char letterChar = ltr.charAt(0);

        view.setEnabled(false);
        view.setBackgroundResource(R.drawable.letter_down);

        // correct?
        boolean correct = false;
        for(int k = 0; k < currWord.length(); k++) {
            if(currWord.charAt(k)==letterChar){
                correct = true;
                numCorr++;
                charViews[k].setTextColor(Color.BLACK);
            }
        }

        if (correct) {
            //correct guess
            if (numCorr == numChars) {
                //user has won--------------------[[--
                // Disable Buttons
                disableBtns();

                // Display Alert Dialog
                AlertDialog.Builder winBuild = new AlertDialog.Builder(this);
                winBuild.setTitle("YAY");
                winBuild.setMessage("You win!\n\nThe answer was:\n\n"+currWord);
                winBuild.setPositiveButton("Play Again",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GameActivity.this.playGame();
                            }});

                winBuild.setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                GameActivity.this.finish();
                            }});

                winBuild.show();
            }
        } else if (currPart < numParts) {
            //some guesses left
            bodyParts[currPart].setVisibility(View.VISIBLE);
            currPart++;
        } else{
            //user has lost
            disableBtns();

            // Display Alert Dialog
            AlertDialog.Builder loseBuild = new AlertDialog.Builder(this);
            loseBuild.setTitle("OOPS");
            loseBuild.setMessage("You lose!\n\nThe answer was:\n\n"+currWord);
            loseBuild.setPositiveButton("Play Again",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            GameActivity.this.playGame();
                        }});

            loseBuild.setNegativeButton("Exit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            GameActivity.this.finish();
                        }});

            loseBuild.show();
        }
    }

    public void disableBtns() {
        int numLetters = letters.getChildCount();
        for (int l = 0; l < numLetters; l++) {
            letters.getChildAt(l).setEnabled(false);
        }
    }

    public void showHelp() {
        AlertDialog.Builder helpBuild = new AlertDialog.Builder(this);

        helpBuild.setTitle("Help");
        helpBuild.setMessage("Guess the word by selecting the letters.\n\n"
                + "You only have 6 wrong selections then it's game over!");
        helpBuild.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        helpAlert.dismiss();
                    }});
        helpAlert = helpBuild.create();

        helpBuild.show();
    }
}
