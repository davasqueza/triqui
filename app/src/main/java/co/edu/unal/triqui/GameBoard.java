package co.edu.unal.triqui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

public class GameBoard extends View {
    Paint paint = new Paint();
    private Random genericRandom;
    Context activity;

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

    void init() {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);

        this.setBackgroundColor(Color.WHITE);
    }

    public DifficultyLevel getDifficultyLevel() {
        return currentDifficulty;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        currentDifficulty = difficultyLevel;
    }

    public GameBoard(Context context) {
        super(context);
        activity = context;
        genericRandom = new Random();

        init();
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
                    playTurn(row, column);
                    invalidate();

                    GameStatus gameStatus = validateWin();

                    if(gameStatus == GameStatus.UNFINISHED){
                        playComputerTurn();
                        invalidate();
                    }
                    return true;
                }
            }
        }

        return true;
    }

    void playTurn(int row, int column){
        if(board[row-1][column-1] != BoardStatus.FREE){return;}
        board[row-1][column-1] = currentPlayer;

        GameStatus status = validateWin();

        switch (status){
            case WON:
                String winner = (currentPlayer == BoardStatus.FIRST) ? "equis" : "circulo";
                showAlert("Victoria", "Felicitaciones, gana "+winner);
                break;
            case DRAW_GAME:
                showAlert("Empate", "¡Mejor suerte para la próxima!");
                break;
            case UNFINISHED:
                currentPlayer = (currentPlayer == BoardStatus.FIRST) ?
                        BoardStatus.SECOND : BoardStatus.FIRST;
                break;
        }
    }

    void playComputerTurn(){
        Position computerMove = getComputerMove();
        playTurn(computerMove.getRow(), computerMove.getColumn());
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
                canvas.drawLine(x1, y1, x2, y2, paint);
                canvas.drawLine(x1, y2, x2, y1, paint);
                break;
            case SECOND:
                canvas.drawCircle((x1 + x2 )/2, (y1 + y2)/2, boxWidth/2 ,paint);
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
            if (position.getColumn() == -1)
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

    GameStatus validateWin(){
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
