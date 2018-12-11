package co.edu.unal.triqui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class GameBoard extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Random genericRandom;
    private Bitmap player1Bitmap;
    private Bitmap player2Bitmap;
    private MediaPlayer player1Player;
    private MediaPlayer player2Player;
    private Context activity;
    private int humanWin;
    private int computerWin;
    private int ties;
    private TextView humanTextView;
    private TextView computerTextView;
    private TextView tiesTextView;
    private Boolean soundEnabled;
    private String victoryMessage;

    public enum BoardStatus {
        FREE,
        FIRST,
        SECOND
    }

    public enum GameStatus {
        WON,
        DRAW_GAME,
        UNFINISHED
    }

    public enum DifficultyLevel {
        Easy,
        Harder,
        Expert
    };

    BoardStatus[][] board = new BoardStatus[][] {
            { BoardStatus.FREE, BoardStatus.FREE, BoardStatus.FREE},
            { BoardStatus.FREE, BoardStatus.FREE, BoardStatus.FREE},
            { BoardStatus.FREE, BoardStatus.FREE, BoardStatus.FREE}
    };

    BoardStatus currentPlayer = BoardStatus.FIRST;
    private DifficultyLevel currentDifficulty = DifficultyLevel.Easy;

    public GameBoard(Context context) {
        super(context);
        activity = context;

        init();
    }

    public GameBoard(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        activity = context;

        init();
    }

    void init() {
        humanWin = 0;
        computerWin = 0;
        ties = 0;

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);

        genericRandom = new Random();

        player1Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.player1);
        player2Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.player2);
    }

    public void onResumeMainActivity(Context context) {
        player1Player = MediaPlayer.create(context, R.raw.player1);
        player2Player = MediaPlayer.create(context, R.raw.player2);
    }

    public void onPauseMainActivity() {
        player1Player.release();
        player2Player.release();
    }

    public int getHumanWin() {
        return humanWin;
    }

    public void setHumanWin(int humanWin){
        this.humanWin = humanWin;
    }

    public int getComputerWin() {
        return computerWin;
    }

    public void setComputerWin(int computerWin){
        this.computerWin = computerWin;
    }

    public int getTies() {
        return ties;
    }

    public void setTies(int ties){
        this.ties = ties;
    }

    public DifficultyLevel getDifficultyLevel() {
        return currentDifficulty;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        currentDifficulty = difficultyLevel;
    }

    public void setSound(Boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public void setVictoryMessage(String victoryMessage) {
        this.victoryMessage = victoryMessage;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = canvas.getWidth();
        int viewHeight = canvas.getHeight();

        for(int line = 1; line < 3; line++){
            int linePositionX = viewWidth/3 * line;
            int linePositionY = viewHeight/3 * line;

            canvas.drawLine(0, linePositionY, viewWidth, linePositionY, paint);
            canvas.drawLine(linePositionX, 0, linePositionX, viewHeight, paint);
        }

        for(int row = 1; row <= 3; row++){
            for(int column = 1; column <= 3; column++){
                BoardStatus status = board[row-1][column-1];
                drawTurn(canvas, status, row, column);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event ) {
        if (event.getAction() != MotionEvent.ACTION_UP){
            return true;
        }

        int x = (int)event.getX();
        int y = (int)event.getY();
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        for(int row = 1; row <= 3; row++){
            int rowLine = viewHeight/3 * row;
            for(int column = 1; column <= 3; column++){
                int columnLine = viewWidth/3 * column;

                if(x < columnLine && y < rowLine){
                    if(board[row-1][column-1] != BoardStatus.FREE){return true;}

                    playTurn(row, column);
                    playComputerTurn();
                    return true;
                }
            }
        }

        return true;
    }

    void playTurn(int row, int column){
        if(board[row-1][column-1] != BoardStatus.FREE){return;}
        board[row-1][column-1] = currentPlayer;

        if(soundEnabled){
            if(currentPlayer == BoardStatus.FIRST){
                player1Player.start();
            }
            else {
                player2Player.start();
            }
        }

        invalidate();

        GameStatus status = validateWin();

        switch (status){
            case WON:
                String winner;
                if(currentPlayer == BoardStatus.FIRST){
                    winner = victoryMessage;
                    humanWin += 1;
                }
                else{
                    winner = "Ups, ha ganado el computador";
                    computerWin += 1;
                }
                showAlert("Victoria", winner);
                break;
            case DRAW_GAME:
                ties += 1;
                showAlert("Empate", "¡Mejor suerte para la próxima!");
                break;
            case UNFINISHED:
                currentPlayer = (currentPlayer == BoardStatus.FIRST) ?
                        BoardStatus.SECOND : BoardStatus.FIRST;
                break;
        }

        displayScores();
    }

    void playComputerTurn(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                GameStatus gameStatus = validateWin();
                if(gameStatus == GameStatus.UNFINISHED){
                    Position computerMove = getComputerMove();
                    playTurn(computerMove.getRow(), computerMove.getColumn());
                }
            }
        }, 1000);
    }

    void drawTurn(Canvas canvas, BoardStatus status, int row, int column){
        int boxWidth = getWidth() / 3;
        int boxHeight = getHeight() / 3;
        int x1 = boxWidth * (column - 1);
        int x2 = boxWidth * column;
        int y1 = boxHeight * (row - 1);
        int y2 = boxHeight * row;

        switch (status){
            case FIRST:
                canvas.drawBitmap(player1Bitmap,
                        null,  // src
                        new Rect(x1, y1, x2, y2),  // dest
                        null);
                break;
            case SECOND:
                canvas.drawBitmap(player2Bitmap,
                        null,  // src
                        new Rect(x1, y1, x2, y2),  // dest
                        null);
                break;
        }
    }

    void showAlert(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
                .setMessage(message)
                .setTitle(title)
                .setNeutralButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                resetBoard();
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    void resetBoard(){
        board = new BoardStatus[][] {
                { BoardStatus.FREE, BoardStatus.FREE, BoardStatus.FREE},
                { BoardStatus.FREE, BoardStatus.FREE, BoardStatus.FREE},
                { BoardStatus.FREE, BoardStatus.FREE, BoardStatus.FREE}
        };
        invalidate();
    }

    public Position getRandomMove() {
        Position position;
        int row, column;

        do {
            row = genericRandom.nextInt(3);
            column = genericRandom.nextInt(3);
        }
        while (board[row][column] != BoardStatus.FREE);

        position = new Position(row, column);

        return position;
    }

    public Position getBlockingMove() {
        Position position = null;

        for (int row = 0; row < 3; row++){
            for (int column = 0; column < 3; column++){
                BoardStatus positionStatus = board[row][column];
                if(positionStatus != BoardStatus.FREE){
                    continue;
                }
                board[row][column] = BoardStatus.FIRST;
                GameStatus gameStatus = validateWin();
                board[row][column] = positionStatus;

                if(gameStatus == GameStatus.WON){
                    position = new Position(row,column);
                }
            }
        }

        return position;
    }

    public Position getWinningMove() {
        Position position = null;

        for (int row = 0; row < 3; row++){
            for (int column = 0; column < 3; column++){
                BoardStatus positionStatus = board[row][column];
                if(positionStatus != BoardStatus.FREE){
                    continue;
                }
                board[row][column] = BoardStatus.SECOND;
                GameStatus gameStatus = validateWin();
                board[row][column] = positionStatus;

                if(gameStatus == GameStatus.WON){
                    position = new Position(row,column);
                }
            }
        }

        return position;
    }

    public Position getComputerMove() {
        Position position = null;

        if (currentDifficulty == DifficultyLevel.Easy)
            position = getRandomMove();
        else if (currentDifficulty == DifficultyLevel.Harder) {
            position = getWinningMove();
            if (position == null)
                position = getRandomMove();
        } else if (currentDifficulty == DifficultyLevel.Expert) {
            // Try to win, but if that's not possible,block.
            // If that's not possible, position anywhere.
            position = getWinningMove();
            if (position == null)
                position = getBlockingMove();
            if (position == null)
                position = getRandomMove();
        }

        return position;
    }

    public int[] getBoardState(){
        int status[] = new int[9];

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int positionStatus = board[row][column].ordinal();
                int linearPosition = (row * 3) + column;

                status[linearPosition] = positionStatus;
            }
        }

        return status;
    }

    public void setBoardState(int[] status){
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int linearPosition = (row * 3) + column;
                board[row][column] = BoardStatus.values()[status[linearPosition]];
            }
        }
    }

    public void setHumanTextView(TextView humanTextView){
        this.humanTextView = humanTextView;
    }
    public void setComputerTextView(TextView computerTextView){
        this.computerTextView = computerTextView;
    }
    public void setTiesTextView(TextView tiesTextView){
        this.tiesTextView = tiesTextView;
    }

    public void displayScores(){
        humanTextView.setText(Integer.toString(getHumanWin()));
        computerTextView.setText(Integer.toString(getComputerWin()));
        tiesTextView.setText(Integer.toString(getTies()));
    }

    public GameStatus validateWin(){
        /* Diagonales */
        if(board[0][0] != BoardStatus.FREE && board[1][1] != BoardStatus.FREE  && board[2][2] != BoardStatus.FREE
                && board[0][0] == board[1][1] && board[0][0] == board[2][2]){
            return GameStatus.WON;
        }
        if(board[0][2] != BoardStatus.FREE && board[1][1] != BoardStatus.FREE  && board[2][0] != BoardStatus.FREE
                && board[0][2] == board[1][1]&& board[0][2] == board[2][0]){

            return GameStatus.WON;
        }

        /* Horizontales */
        if(board[0][0] != BoardStatus.FREE && board[0][1] != BoardStatus.FREE  && board[0][2] != BoardStatus.FREE
                && board[0][0] == board[0][1] && board[0][0] == board[0][2]){

            return GameStatus.WON;
        }

        if(board[1][0] != BoardStatus.FREE && board[1][1] != BoardStatus.FREE  && board[1][2] != BoardStatus.FREE
                && board[1][0] == board[1][1] && board[1][0] == board[1][2]){

            return GameStatus.WON;
        }

        if(board[2][0] != BoardStatus.FREE && board[2][1] != BoardStatus.FREE  && board[2][2] != BoardStatus.FREE
                && board[2][0] == board[2][1] && board[2][0] == board[2][2]){

            return GameStatus.WON;
        }

        /* Verticales */

        if(board[0][0] != BoardStatus.FREE && board[1][0] != BoardStatus.FREE  && board[2][0] != BoardStatus.FREE
                && board[0][0] == board[1][0] && board[0][0] == board[2][0]){

            return GameStatus.WON;
        }

        if(board[0][1] != BoardStatus.FREE && board[1][1] != BoardStatus.FREE  && board[2][1] != BoardStatus.FREE
                && board[0][1] == board[1][1] && board[0][1] == board[2][1]){

            return GameStatus.WON;
        }

        if(board[0][2] != BoardStatus.FREE && board[1][2] != BoardStatus.FREE  && board[2][2] != BoardStatus.FREE
                && board[0][2] == board[1][2] && board[0][2] == board[2][2]){

            return GameStatus.WON;
        }

        if(board[0][0] != BoardStatus.FREE && board[0][2] != BoardStatus.FREE  && board[0][2] != BoardStatus.FREE
                && board[1][0] != BoardStatus.FREE && board[1][1] != BoardStatus.FREE  && board[1][2] != BoardStatus.FREE
                && board[2][0] != BoardStatus.FREE && board[2][1] != BoardStatus.FREE  && board[2][2] != BoardStatus.FREE ){

            return GameStatus.DRAW_GAME;
        }

        return GameStatus.UNFINISHED;
    }
}
