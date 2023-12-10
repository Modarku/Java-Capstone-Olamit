package src.data;

import src.data.solver.AdvancedAlgo;
import src.data.solver.advanced.AnalyzeResult;
import src.data.solver.advanced.detail.DetailedResults;
import src.data.solver.advanced.detail.ProbabilityKnowledge;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/*   mnsw.pro
 *   The idea behind the minesweeper solver is to duplicate the game internally
 *   and then calculate the probability through a simple algorithm for those
 *   unopened cells. However only those near to the opened cells are calculated.
 *
 *   In order to duplicate the game internally we have to do the following:
 *   1. Take a screenshot of the minesweeper board.
 *   2. Iterate the board from upper-left corner to the lower-right corner.
 *   3. For each iteration, check the state of the cell whether is it opened or not.
 *     If it is opened check for its content whether it is an empty, a bomb, a one and so on.
 *   4. After determining the state of the cell, assign a constant integer value to represent
 *     its state.
 *   5. Append each cell to the 2d array until we are finished duplicating all cells
 *     from the minesweeper game board itself.
 */

public class Debugging {
    public static void main(String[] args) throws AWTException {
        Rectangle screenRegion = new Rectangle(129, 274, 511, 511);
        MinesweeperSolver minesweeperSolver = AdvancedAlgo.getInstance();
        MinesweeperBot minesweeperBot = new MinesweeperBot(screenRegion, minesweeperSolver, 16, 16, 40);

        while(true){
            try {
                // Pause for 3 seconds (3000 milliseconds)
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //minesweeperBot.run();
            System.out.println("Running...");
        }


    }

    // AUXILIARY FUNCTIONS

    // THIS IS HOW TO DISPLAY THE LAYOUT OF THE BOARD
    public static void displayBoard(Tile[][] board) {
        displayBoard(board, false);
    }

    public static void displayBoard(Tile[][] board, boolean locateMine) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                Tile cur = board[row][col];
                if (locateMine) {
                    double prob = cur.getProbability();
                    if (prob > 0.5) {
                        System.out.print("!  ");
                        continue;
                    }
                    if (prob > 0.3) {
                        System.out.print("?  ");
                        continue;
                    }
                }
                System.out.print(cur.getState().getValue() + "  ");
            }
            System.out.println();
        }
    }

    // THIS IS HOW TO DUPLICATE AN EXTERNAL MINESWEEPER GAME
    public static Tile[][] scanBoardImage(BufferedImage image, int rows, int cols) {
        Tile[][] board = new Tile[rows][cols];
        int width = image.getWidth();
        int height = image.getHeight();
        int side = width / rows;
        int offset = 5;
        int i = 1;
        for (int row = 0; row <= height - side; row += side) {
            for (int col = 0; col <= width - side; col += side) {
                BufferedImage crop = cropImage(image, col, row, side - offset, side - offset);
                int x = row / side;
                int y = col / side;
                try {
                    ImageIO.write(crop, "png", new File("src\\data\\temp\\tile" + i + ".png"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                i++;
                board[x][y] = new Tile(x, y, checkState(crop));
            }
        }
        return board;
    }

    // THIS IS HOW TO DISPLAY EACH TILES PROBABILITY
    public static void displayProbability(Tile[][] board) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                System.out.printf("%.2f ", board[row][col].getProbability());
            }
            System.out.println();
        }
    }

    // THIS IS HOW TO CHECK EACH TILES PROBABILITY
    public static Tile[][] solveBoard(Tile[][] board, int mines) {
        Tile[][] solveBoard = board.clone();
        AdvancedAlgo analyze = AdvancedAlgo.getInstance();
        AnalyzeResult<Tile> results = analyze.solve();
        DetailedResults<Tile> detail = results.analyzeDetailed(analyze);

        //System.out.println(detail.getProxies());

        for (ProbabilityKnowledge<Tile> ee : detail.getProxies()) {
            Tile cur = ee.getField();
            int x = cur.getX();
            int y = cur.getY();
            double prob = ee.getMineProbability();
            board[x][y].setProbability(prob);
        }
        return solveBoard;
    }

    // THIS IS HOW TO CHECK FOR A TILE STATE/CONTENT
    public static Block checkState(BufferedImage image) {
        int tolerance = 15;
        Point foundWhite = searchPixel(image, Pixels.WHITE.getValue(), tolerance);
        Point foundBlack = searchPixel(image, Pixels.BLACK.getValue(), tolerance);
        Point foundRed = searchPixel(image, Pixels.RED.getValue(), tolerance);

        if (foundRed != null && foundBlack != null && foundWhite != null) {
            return Block.FLAG;
        }
        if (foundBlack != null) {
            return Block.MINE;
        }
        if (foundWhite != null) {
            return Block.CLOSED;
        }
        if (foundRed != null) {
            return Block.THREE;
        }
        if (searchPixel(image, Pixels.BLUE.getValue(), tolerance) != null) {
            return Block.ONE;
        }
        if (searchPixel(image, Pixels.GREEN.getValue(), tolerance) != null) {
            return Block.TWO;
        }
        if (searchPixel(image, Pixels.DARK_BLUE.getValue(), tolerance) != null) {
            return Block.FOUR;
        }
        return Block.EMPTY;
    }

    // THIS IS HOW TO COMPARE TWO IMAGES BASED ON THEIR PIXELS
    public static boolean compareImage(BufferedImage image, BufferedImage target) {
        byte[] imagePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        byte[] targetPixels = ((DataBufferByte) target.getRaster().getDataBuffer()).getData();
        return Arrays.equals(imagePixels, targetPixels);
    }

    // THIS IS HOW TO CROP AN IMAGE FROM AN EXISTING IMAGE
    public static BufferedImage cropImage(BufferedImage image, int x, int y, int w, int h) {
        if (x + w > image.getWidth()) {
            x = image.getWidth() - w;
        }
        if (y + h > image.getHeight()) {
            y = image.getHeight() - h;
        }
        return image.getSubimage(x, y, w, h);
    }

    // THIS IS HOW TO TAKE A SCREENSHOT
    public static void screenshot(Rectangle rect, String pathname) {
        screenshot(rect, pathname, "png");
    }

    public static void screenshot(Rectangle rect, String pathname, String extension) {
        try {
            Robot robot = new Robot();
            BufferedImage img = robot.createScreenCapture(rect);
            ImageIO.write(img, extension, new File(pathname + "." + extension));
        } catch (AWTException | IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // THIS IS HOW TO DISPLAY AN RGB INT IN A STANDARD RGB FORM
    public static Color convertRGBInt(int RGBInt) {
        Color color = new Color(RGBInt, true);
//        System.out.println("Red: " + color.getRed() + ", Green: " + color.getGreen() + ", Blue: " + color.getBlue() + ", Opacity: " + color.getAlpha());
        return color;
    }

    // THIS IS HOW TO DISPLAY THE MOUSE POINTER COORDINATES RELATIVE TO THE SCREEN COORDINATES
    public static Point displayMouseCoordinates() {
        Point coordinates = MouseInfo.getPointerInfo().getLocation();
        System.out.println("Mouse pointer coordinates: (" + coordinates.getX() + ", " + coordinates.getY() + ")");
        return coordinates;
    }

    // THIS IS HOW TO ENUMERATE ALL PIXEL FROM AN IMAGE IN THE FORM OF RGB INT
    public static void enumeratePixels(BufferedImage image) {
        enumeratePixels(image, 0, 0);
    }

    public static void enumeratePixels(BufferedImage image, int x, int y) {
        enumeratePixels(image, x, y, image.getWidth() - x, image.getHeight() - y);
    }

    public static void enumeratePixels(BufferedImage image, int x, int y, int width, int height) {
        if (x + width > image.getWidth()) {
            x = image.getWidth() - width;
        }
        if (y + height > image.getHeight()) {
            y = image.getHeight() - height;
        }
        for (int pointY = y; pointY < y + height; pointY++) {
            for (int pointX = x; pointX < x + width; pointX++) {
                System.out.print(image.getRGB(pointX, pointY) + ", ");
            }
            System.out.println();
        }
    }

    // THIS IS HOW TO SEARCH FOR A SPECIFIC PIXEL FROM AN IMAGE
    public static Point searchPixel(BufferedImage image, int targetColor) {
        return searchPixel(image, targetColor, 0, 0, 0);
    }

    public static Point searchPixel(BufferedImage image, int targetColor, int tolerance) {
        return searchPixel(image, targetColor, 0, 0, tolerance);
    }

    public static Point searchPixel(BufferedImage image, int targetColor, int x, int y, int tolerance) {
        return searchPixel(image, targetColor, x, y, image.getWidth() - x, image.getHeight() - y, tolerance);
    }

    public static Point searchPixel(BufferedImage image, int targetColor, int x, int y, int width, int height, int tolerance) {
        if (x + width > image.getWidth()) {
            x = image.getWidth() - width;
        }

        if (y + height > image.getHeight()) {
            y = image.getHeight() - height;
        }

        try {
            ImageIO.write(image, "png", new File("test.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Color t = new Color(targetColor);
        int pixelLen = image.getAlphaRaster() != null ? 4 : 3;
        int min = (x * y) * pixelLen;
        int max = (x * y + width * height) * pixelLen;

        while (min < max) {
            if (pixelLen == 4) {
                int alpha = pixels[min++] & 0xFF;
                if (Math.abs(alpha - t.getAlpha()) > tolerance) {
                    continue;
                }
            }

            int blue = pixels[min++] & 0xFF;
            int green = pixels[min++] & 0xFF;
            int red = pixels[min++] & 0xFF;

            if (Math.abs(red - t.getRed()) <= tolerance &&
                    Math.abs(green - t.getGreen()) <= tolerance &&
                      Math.abs(blue - t.getBlue()) <= tolerance) {
                return new Point(0, 0);
            }
        }
        return null;
    }
}
