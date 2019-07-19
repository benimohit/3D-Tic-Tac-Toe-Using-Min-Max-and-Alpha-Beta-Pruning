import java.util.*;

public class aiTicTacToe extends AI {
    private int player;
    private int opponent;
    private final List<List<positionTicTacToe>> winLines;  //all winning lines
    private static final int DEPTH = 2;                    //depth of MINIMAX
    private static final int DIM = 4;                      //TicTacToe cube size
    private static final int MAX_POINT = 10000;
    private static final int MIN_POINT = -10000;
    private HashMap<String, List<Integer>> winMap = new HashMap<>();  //Key Value pair of board position and passing lines
    private positionTicTacToe defaultMove;
    private positionTicTacToe currentMove;                 //temp move
    private positionTicTacToe best;                        //best move

    public aiTicTacToe(int setPlayer) {
        this.player = setPlayer;
        this.opponent = this.player == 1 ? 2 : 1;           // defining opponent on the basis of player
        winLines = initializeWinningLines();
        defaultMove = new positionTicTacToe(0, 0, 0);
        currentMove = defaultMove;

        String key;
        List<Integer> val;

        for (int i = 0; i < winLines.size(); i++) {
            for (int j = 0; j < winLines.get(i).size(); j++) {
                positionTicTacToe item = winLines.get(i).get(j);
                key = getKey(item);
                if (winMap.containsKey(key)) {
                    val = winMap.get(key);          // if key is present append this line number
                } else {
                    val = new ArrayList<>();
                }
                val.add(i);
                winMap.put(key, val);
            }
        }
    }

    /**
     * @param pos Input position for Key
     * @return return the Key for this position
     */
    private String getKey(positionTicTacToe pos) {
        return getKey(pos.x, pos.y, pos.z);
    }

    /**
     * Input values for Key
     *
     * @param x
     * @param y
     * @param z
     * @return return the Key for these values
     */
    private String getKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    /**
     * get board index by position
     * @param pos
     * @return index on the board
     */
    private int getBoardIndex(positionTicTacToe pos) {
        return getBoardIndex(pos.x, pos.y, pos.z);
    }

    /**
     * get board index by the values
     * @param x
     * @param y
     * @param z
     * @return index on the board
     */
    private int getBoardIndex(int x, int y, int z) {
        return (x * DIM * DIM) + (y * DIM) + (z);
    }

    private int getStateOfPositionFromBoard(positionTicTacToe position, List<positionTicTacToe> board) {
        //a helper function to get state of a certain position in the Tic-Tac-Toe board by given position TicTacToe
        return board.get(getBoardIndex(position)).state;
    }

    /**
     * terminal condition where the game ends
     * @param node
     * @return winner at this terminal node
     */
    public int findWinnerOnGameEnd(Node node) {
        List<Integer> val = winMap.get(getKey(currentMove));
        for (int i = 0; i < val.size(); i++) {
            List<positionTicTacToe> win_lines = winLines.get(val.get(i));
            int[] lineCounts = findPlayerAndOpponentLineCount(node.board, win_lines);
            if (lineCounts[1] == DIM) {
                return player;
            } else if (lineCounts[0] == DIM) {
                return opponent;
            }
        }
        return 0;
    }

    /**
     * Calculating the number of moves player and opponent has marked at winning line
     * @param board
     * @param win_lines
     * @return count of lines for both players
     */
    public int[] findPlayerAndOpponentLineCount(List<positionTicTacToe> board, List<positionTicTacToe> win_lines) {
        int oppLines = 0;
        int playerLines = 0;
        for (int j = 0; j < win_lines.size(); j++) {
            int state_index = getBoardIndex(win_lines.get(j));
            int state = board.get(state_index).state;
            if (state == this.player) {
                playerLines += 1;
            } else if (state == this.opponent) {
                oppLines += 1;
            }
        }
        return new int[]{playerLines, oppLines};
    }

    /**
     * acts as child nodes for the given node but can also be seen as the all possible moves at this depth
     * @param node
     * @return
     */
    private List<positionTicTacToe> determineAllPossibleMoves(Node node) {
        List<positionTicTacToe> moves = new ArrayList<>();
        for (int i = 0; i < node.board.size(); i++) {
            if (node.board.get(i).state == 0) {
                moves.add(node.board.get(i));
            }
        }
        return moves;
    }

    /**
     * alpha beta pruning with min max algorithm
     * @param node
     * @param depth
     * @param alpha
     * @param beta
     * @param max
     * @return value at this depth for a particular node
     */
    private int alphaBeta(Node node, int depth, int alpha, int beta, boolean max) {
        // terminal condition
        int winner = findWinnerOnGameEnd(node);

        if (winner != 0) {
            if (winner == player) {
                return MIN_POINT;
            }
            if (winner == opponent) {
                return MAX_POINT;
            }
        }

        // terminal node
        if (depth == 0) {
            return node.getHeu();
        }

        List<positionTicTacToe> moves = determineAllPossibleMoves(node);

        if (max) {
            int value = Integer.MIN_VALUE;
            boolean move = true;
            for (int i = 0; move && i < moves.size(); i++) {
                node.makeMove(moves.get(i), true);
                value = Integer.max(value, alphaBeta(node, depth - 1, alpha, beta, false));
                if (alpha < value) {
                    best = moves.get(i);
                }
                alpha = Integer.max(alpha, value);
                node.resetMove(moves.get(i));
                //pruning
                if (alpha > beta) {
                    move = false;
                }
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            boolean move = true;
            for (int i = 0; move && i < moves.size(); i++) {
                node.makeMove(moves.get(i), false);
                value = Integer.min(value, alphaBeta(node, depth - 1, alpha, beta, true));
                beta = Integer.min(beta, value);
                node.resetMove(moves.get(i));
                //pruning
                if (alpha > beta) {
                    move = false;
                }
            }
            return value;
        }
    }

    /**
     * Calculates the best move on this board
     * @param board
     * @return position of the move
     */
    private positionTicTacToe bestMove(List<positionTicTacToe> board) {
        Node node = new Node(board);
        alphaBeta(node, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        return best;
    }

    /**
     * TODO function
     * @param board
     * @param player
     * @return Move
     */
    public positionTicTacToe myAIAlgorithm(List<positionTicTacToe> board, int player) {
        positionTicTacToe myNextMove;
        do {
            //if (player != this.player) {
                if (player == 2) {
                Random rand = new Random();
                int x = rand.nextInt(DIM);
                int y = rand.nextInt(DIM);
                int z = rand.nextInt(DIM);
                myNextMove = new positionTicTacToe(x, y, z);
            } else {
                myNextMove = bestMove(board);
            }
        } while (getStateOfPositionFromBoard(myNextMove, board) != 0);
        return myNextMove;
    }

    private List<List<positionTicTacToe>> initializeWinningLines() {
        //create a list of winning line so that the game will "brute-force" check if a player satisfied any 	winning condition(s).
        List<List<positionTicTacToe>> winningLines = new ArrayList<List<positionTicTacToe>>();

        //48 straight winning lines
        //z axis winning lines
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
                oneWinCondtion.add(new positionTicTacToe(i, j, 0, -1));
                oneWinCondtion.add(new positionTicTacToe(i, j, 1, -1));
                oneWinCondtion.add(new positionTicTacToe(i, j, 2, -1));
                oneWinCondtion.add(new positionTicTacToe(i, j, 3, -1));
                winningLines.add(oneWinCondtion);
            }
        //y axis winning lines
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
                oneWinCondtion.add(new positionTicTacToe(i, 0, j, -1));
                oneWinCondtion.add(new positionTicTacToe(i, 1, j, -1));
                oneWinCondtion.add(new positionTicTacToe(i, 2, j, -1));
                oneWinCondtion.add(new positionTicTacToe(i, 3, j, -1));
                winningLines.add(oneWinCondtion);
            }
        //x axis winning lines
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
                oneWinCondtion.add(new positionTicTacToe(0, i, j, -1));
                oneWinCondtion.add(new positionTicTacToe(1, i, j, -1));
                oneWinCondtion.add(new positionTicTacToe(2, i, j, -1));
                oneWinCondtion.add(new positionTicTacToe(3, i, j, -1));
                winningLines.add(oneWinCondtion);
            }

        //12 main diagonal winning lines
        //xz plane-4
        for (int i = 0; i < 4; i++) {
            List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
            oneWinCondtion.add(new positionTicTacToe(0, i, 0, -1));
            oneWinCondtion.add(new positionTicTacToe(1, i, 1, -1));
            oneWinCondtion.add(new positionTicTacToe(2, i, 2, -1));
            oneWinCondtion.add(new positionTicTacToe(3, i, 3, -1));
            winningLines.add(oneWinCondtion);
        }
        //yz plane-4
        for (int i = 0; i < 4; i++) {
            List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
            oneWinCondtion.add(new positionTicTacToe(i, 0, 0, -1));
            oneWinCondtion.add(new positionTicTacToe(i, 1, 1, -1));
            oneWinCondtion.add(new positionTicTacToe(i, 2, 2, -1));
            oneWinCondtion.add(new positionTicTacToe(i, 3, 3, -1));
            winningLines.add(oneWinCondtion);
        }
        //xy plane-4
        for (int i = 0; i < 4; i++) {
            List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
            oneWinCondtion.add(new positionTicTacToe(0, 0, i, -1));
            oneWinCondtion.add(new positionTicTacToe(1, 1, i, -1));
            oneWinCondtion.add(new positionTicTacToe(2, 2, i, -1));
            oneWinCondtion.add(new positionTicTacToe(3, 3, i, -1));
            winningLines.add(oneWinCondtion);
        }

        //12 anti diagonal winning lines
        //xz plane-4
        for (int i = 0; i < 4; i++) {
            List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
            oneWinCondtion.add(new positionTicTacToe(0, i, 3, -1));
            oneWinCondtion.add(new positionTicTacToe(1, i, 2, -1));
            oneWinCondtion.add(new positionTicTacToe(2, i, 1, -1));
            oneWinCondtion.add(new positionTicTacToe(3, i, 0, -1));
            winningLines.add(oneWinCondtion);
        }
        //yz plane-4
        for (int i = 0; i < 4; i++) {
            List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
            oneWinCondtion.add(new positionTicTacToe(i, 0, 3, -1));
            oneWinCondtion.add(new positionTicTacToe(i, 1, 2, -1));
            oneWinCondtion.add(new positionTicTacToe(i, 2, 1, -1));
            oneWinCondtion.add(new positionTicTacToe(i, 3, 0, -1));
            winningLines.add(oneWinCondtion);
        }
        //xy plane-4
        for (int i = 0; i < 4; i++) {
            List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
            oneWinCondtion.add(new positionTicTacToe(0, 3, i, -1));
            oneWinCondtion.add(new positionTicTacToe(1, 2, i, -1));
            oneWinCondtion.add(new positionTicTacToe(2, 1, i, -1));
            oneWinCondtion.add(new positionTicTacToe(3, 0, i, -1));
            winningLines.add(oneWinCondtion);
        }

        //4 additional diagonal winning lines
        List<positionTicTacToe> oneWinCondtion = new ArrayList<positionTicTacToe>();
        oneWinCondtion.add(new positionTicTacToe(0, 0, 0, -1));
        oneWinCondtion.add(new positionTicTacToe(1, 1, 1, -1));
        oneWinCondtion.add(new positionTicTacToe(2, 2, 2, -1));
        oneWinCondtion.add(new positionTicTacToe(3, 3, 3, -1));
        winningLines.add(oneWinCondtion);

        oneWinCondtion = new ArrayList<positionTicTacToe>();
        oneWinCondtion.add(new positionTicTacToe(0, 0, 3, -1));
        oneWinCondtion.add(new positionTicTacToe(1, 1, 2, -1));
        oneWinCondtion.add(new positionTicTacToe(2, 2, 1, -1));
        oneWinCondtion.add(new positionTicTacToe(3, 3, 0, -1));
        winningLines.add(oneWinCondtion);

        oneWinCondtion = new ArrayList<positionTicTacToe>();
        oneWinCondtion.add(new positionTicTacToe(3, 0, 0, -1));
        oneWinCondtion.add(new positionTicTacToe(2, 1, 1, -1));
        oneWinCondtion.add(new positionTicTacToe(1, 2, 2, -1));
        oneWinCondtion.add(new positionTicTacToe(0, 3, 3, -1));
        winningLines.add(oneWinCondtion);

        oneWinCondtion = new ArrayList<positionTicTacToe>();
        oneWinCondtion.add(new positionTicTacToe(0, 3, 0, -1));
        oneWinCondtion.add(new positionTicTacToe(1, 2, 1, -1));
        oneWinCondtion.add(new positionTicTacToe(2, 1, 2, -1));
        oneWinCondtion.add(new positionTicTacToe(3, 0, 3, -1));
        winningLines.add(oneWinCondtion);

        return winningLines;
    }

    /**
     * Class for a particular board with required states and helper functions
     */
    class Node {
        List<positionTicTacToe> board;

        /**
         * Constructor for the class
         * @param board
         */
        Node(List<positionTicTacToe> board) {
            this.board = deepCopyATicTacToeBoard(board);
        }

        private List<positionTicTacToe> deepCopyATicTacToeBoard(List<positionTicTacToe> board) {
            //deep copy of game boards
            List<positionTicTacToe> copiedBoard = new ArrayList<>();
            for (int i = 0; i < board.size(); i++) {
                copiedBoard.add(new positionTicTacToe(board.get(i).x, board.get(i).y, board.get(i).z, board.get(i).state));
            }
            return copiedBoard;
        }

        /**
         * Make temporary move
         * @param move
         * @param maximizingPlayer
         */
        public void makeMove(positionTicTacToe move, boolean maximizingPlayer) {
            currentMove = move;
            int index = getBoardIndex(move);
            // Marking with player marker
            this.board.get(index).state = maximizingPlayer ? player : opponent;
        }

        /**
         *  Undo the temp move
         * @param move
         */
        public void resetMove(positionTicTacToe move) {
            currentMove = defaultMove;
            int index = getBoardIndex(move);
            this.board.get(index).state = 0;
        }

        /**
         * The most important function for predicting the next best move
         * @return
         */
        public int getHeu() {
            List<Integer> val = winMap.get(getKey(currentMove));
            int score = 0;
            // Iterating for all possible winning lines
            for (int j = 0; j < val.size(); j++) {
                List<positionTicTacToe> win_lines = winLines.get(val.get(j));
                // calculate the players marked counts
                int[] lineCounts = findPlayerAndOpponentLineCount(board, win_lines);
                if (lineCounts[0] > 0 && lineCounts[1] > 0) {
                    // leave this line if both players have marked in this winning line
                } else {
                    // if opponent has not marked in this line
                    if (lineCounts[1] == 0) {
                        // make a priority if the player has three marked positions
                        if (lineCounts[0] == 3) {
                            score -= 10;
                        }
                        // calculate the score by number of position already marked
                        score -= (lineCounts[0]) * 10;
                    } else {
                        // if player has not marked in this line
                        if (lineCounts[0] == 0) {
                            // make a priority if the opponent has three marked positions
                            if (lineCounts[1] == 3) {
                                score -= 10;
                            }
                            // calculate the score by number of position already marked
                            score -= (lineCounts[1]) * 10;
                        }
                    }
                }
            }
            return score;
        }
    }
}