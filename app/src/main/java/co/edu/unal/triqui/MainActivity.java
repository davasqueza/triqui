package co.edu.unal.triqui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    GameBoard gameBoard;
    private SharedPreferences mPrefs;

    static final int DIALOG_SETTINGS = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_ABOUT = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
            case R.id.quit:
                return true;
            case R.id.reset:
                resetScore();
                return true;
            case R.id.about:
                showDialog(DIALOG_ABOUT);
                return true;
        }

        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_QUIT_ID:
                // Create the quit confirmation dialog

                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
            case DIALOG_ABOUT:
                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, null);
                builder.setView(layout);
                builder.setPositiveButton("OK", null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Context context = getApplicationContext();

        gameBoard.onResumeMainActivity(context);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameBoard.onPauseMainActivity();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntArray("board", gameBoard.getBoardState());
        outState.putInt("mHumanWins", gameBoard.getHumanWin());
        outState.putInt("mComputerWins", gameBoard.getComputerWin());
        outState.putInt("mTies", gameBoard.getTies());
        outState.putInt("mDifficulty", gameBoard.getDifficultyLevel().ordinal());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        gameBoard.setBoardState(savedInstanceState.getIntArray("board"));
        gameBoard.setHumanWin(savedInstanceState.getInt("mHumanWins"));
        gameBoard.setComputerWin(savedInstanceState.getInt("mComputerWins"));
        gameBoard.setTies(savedInstanceState.getInt("mTies"));
        gameBoard.setDifficultyLevel(GameBoard.DifficultyLevel.values()[savedInstanceState.getInt("mDifficulty")]);
        gameBoard.displayScores();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startNewGame();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", gameBoard.getHumanWin());
        ed.putInt("mComputerWins", gameBoard.getComputerWin());
        ed.putInt("mTiesWins", gameBoard.getTies());
        ed.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CANCELED) {
            // Apply potentially new settings
            loadSettings();
        }
    }

    private void loadSettings(){
        Boolean soundEnabled = mPrefs.getBoolean("sound", true);
        String difficultyLevel = mPrefs.getString("difficulty_level", getString(R.string.difficulty_easy));
        String victoryMessage = mPrefs.getString("victory_message", getString(R.string.result_human_wins));

        if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
            gameBoard.setDifficultyLevel(GameBoard.DifficultyLevel.Easy);
        else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
            gameBoard.setDifficultyLevel(GameBoard.DifficultyLevel.Harder);
        else
            gameBoard.setDifficultyLevel(GameBoard.DifficultyLevel.Expert);

        gameBoard.setSound(soundEnabled);
        gameBoard.setVictoryMessage(victoryMessage);
    }

    private void resetScore(){
        gameBoard.setHumanWin(0);
        gameBoard.setComputerWin(0);
        gameBoard.setTies(0);

        gameBoard.displayScores();
    }

    private void initializeScores(){
        gameBoard.setHumanTextView((TextView) findViewById(R.id.human_wins));
        gameBoard.setComputerTextView((TextView) findViewById(R.id.computer_wins));
        gameBoard.setTiesTextView((TextView) findViewById(R.id.ties_wins));

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        int humanWins = mPrefs.getInt("mHumanWins", 0);
        int computerWins = mPrefs.getInt("mComputerWins", 0);
        int tiesWins = mPrefs.getInt("mTiesWins", 0);

        gameBoard.setHumanWin(humanWins);
        gameBoard.setComputerWin(computerWins);
        gameBoard.setTies(tiesWins);

        gameBoard.displayScores();
    }

    private void startNewGame(){
        gameBoard = findViewById(R.id.board);
        initializeScores();
        loadSettings();
    }
}
