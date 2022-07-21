package com.root.headfirstjava;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
/* TODO
    Протестировать гит
 */

class WrongInputEx extends Exception {
    int cause;
    WrongInputEx(int cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return (cause == 0) ? "буква":"число";
    }
}

class Warship {
    private final int[][] location;
    private final int size;
    private int[][] hits;
    private int hitsCount;

    Warship(int[][] location) {
        this.location = location;
        size = location.length;
        hits = new int[size][2];
        Arrays.fill(hits, new int[]{-1, -1});
        hitsCount = 0;
    }

    int[][] getLocation() {
        return location;
    }

    boolean isAlive() {
        return location[0][0] != -1 && hitsCount < size;
    }

    int hitCheck(int[] coords) {
        for(int[] l: location)
            if (Arrays.equals(l, coords)) {
                if (size > 0 && !isAlive()) {
                    System.out.println("Попал в подбитое судно");
                    return 3;
                }
                for(int[] h: hits)
                    if (Arrays.equals(h, coords)) {
                        System.out.println("Попал повторно");
                        return 3;
                    }
                hits[hitsCount] = coords;
                if (++hitsCount >= size) {
                    System.out.println("Убил!");
                    return 2;
                } else {
                    System.out.println("Ранил");
                    return 1;
                }
            }
        return 0;
    }
}

class Game {
    private final int W;
    private final int H;
    private char[][] map;
    private char[][] sea;
    private Warship[] warships;
    private Random rnd = new Random(System.currentTimeMillis());
    // Что это здесь у нас?
    private final char[] C2I = {'А','Б','В','Г','Д','Е','Ж','З','И','К','Л','М','Н','О',
                                'П','Р','С','Т','У','Ф','Х','Ц','Ч','Ш','Ы','Э','Ю','Я'};
    private int moveCount = 0;

    Game(int width, int height, int[] warshipSizes) {
        if (width > C2I.length) {
            System.out.println("Поле слишком длинное");
            width = C2I.length;
        }
        W = width;
        H = height;
        map = new char[H][W];
        sea = new char[H][W];
        for (int i = 0; i < H; i++) {
            Arrays.fill(map[i], '_');
            Arrays.fill(sea[i], '_');
        }
        warships = new Warship[warshipSizes.length];
        for (int i = 0; i < warships.length; i++) {
            // TODO: переделать на список с проверкой на валидное размещение
            warships[i] = new Warship(putWarship(warshipSizes[i]));
        }
    }

    static Game startClassic() {
        int[] warshipSizes = {
                5,
                4, 4,
                3, 3, 3,
                2, 2, 2, 2,
                1, 1, 1, 1, 1
        };
        Game game = new Game(10, 10, warshipSizes);
        //game.printSea();
        game.start();
        return game;
    }

    private int[][] putWarship(int size) {
        if (size <= 0 || size > H && size > W) {
            System.out.println("Корабль такого размера на поле не поместится");
            return new int[][]{{-1},{-1}};
        }
        boolean vertical;
        if (size > W)
            vertical = true;
        else
            vertical = rnd.nextBoolean();

        int[][] location = new int[size][2];
        for (int[] l: location)
            Arrays.fill(l, -1);

        if (vertical) {
            int[] anchor = putVertical(size);
            if (anchor[0] != -1) {
                int x = anchor[0];
                int y = anchor[1];
                for (int i = 0; i < size; i++) {
                    sea[x + i][y] = 'X';
                    location[i][0] = x + i;
                    location[i][1] = y;
                }
                makeIndent(sea, location);
            }
        } else {
            int[] anchor = putHorizontal(size);
            if (anchor[0] != -1) {
                int x = anchor[0];
                int y = anchor[1];
                for (int i = 0; i < size; i++) {
                    sea[x][y + i] = 'X';
                    location[i][0] = x;
                    location[i][1] = y + i;
                }
                makeIndent(sea, location);
            }
        }

        return location;
    }

    private int[] putVertical(int size) {
        int[] anchor = new int[]{-1,-1};

        int y = rnd.nextInt(W);
        // Столбцы вправо
        int x = -1;
        for (int i = y; i < W; i++) {
            x = getRandomColPosition(i, size);
            if (x != -1) {
                y = i;
                break;
            }
        }
        // Столбцы влево
        if (x == -1 && y > 0)
            for (int i = y - 1; i >= 0; i--) {
                x = getRandomColPosition(i, size);
                if (x != -1) {
                    y = i;
                    break;
                }
            }

        if (x != -1) {
            anchor[0] = x;
            anchor[1] = y;
        }
        return anchor;
    }

    private int[] putHorizontal(int size) {
        int[] anchor = new int[]{-1,-1};
        int x = rnd.nextInt(H);
        // Строки вниз
        int y = -1;
        for (int i = x; i < H; i++) {
            y = getRandomRowPosition(i, size);
            if (y != -1) {
                x = i;
                break;
            }
        }

        // Строки вверх
        if (y == -1 && x > 0)
            for (int i = x - 1; i >= 0; i--) {
                y = getRandomRowPosition(i, size);
                if (y != -1) {
                    x = i;
                    break;
                }
            }

        if (y != -1) {
            anchor[0] = x;
            anchor[1] = y;
        }
        return anchor;
    }

    private int getRandomRowPosition(int rowNum, int size) {
        int[] positionList = new int[W];
        Arrays.fill(positionList, -1);

        int c = 0;
        for (int i = 0; i <= W - size; i++) {
            boolean noplace = false;
            for (int j = 0; j < size; j++) {
                if (i+j < W) {
                    if (sea[rowNum][i+j] != '_') {
                        noplace = true;
                        break;
                    }
                } else {
                    noplace = true;
                    break;
                }
            }
            if (!noplace)
                positionList[c++] = i;
        }
        if (c == 0)
            return -1;

        return positionList[rnd.nextInt(c)];
    }

    private int getRandomColPosition(int colNum, int size) {
        int[] positionList = new int[H];
        Arrays.fill(positionList, -1);

        int c = 0;
        for (int i = 0; i <= H - size; i++) {
            boolean noplace = false;
            for (int j = 0; j < size; j++) {
                if (i+j < H) {
                    if (sea[i+j][colNum] != '_') {
                        noplace = true;
                        break;
                    }
                } else {
                    noplace = true;
                    break;
                }
            }
            if (!noplace)
                positionList[c++] = i;
        }
        if (c == 0)
            return -1;

        return positionList[rnd.nextInt(c)];
    }

    void start() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Всего вражеских судов: "+ checkAliveShipsCount());
        do {
            printMap();
            System.out.print("Координаты: ");
            int[] nextMove;
            try {
                nextMove = getCoords(sc.next());
            } catch (WrongInputEx e) {
                System.out.println("Неверные координаты: "+ e);
                continue;
            } catch (NumberFormatException e) {
                System.out.println("В координатах не число");
                continue;
            }
            hitCheckShips(nextMove);
            moveCount++;
        } while(checkAliveShipsCount() > 0);
        System.out.println("Победа!\nХодов сделано: "+ moveCount);
        printMap();
    }

    private int[] getCoords(String input) throws WrongInputEx, NumberFormatException {
        int[] coordinates = new int[2];

        char a = input.charAt(0);
        if(Character.isLetter(a)) {
            if (a >= 'а')
                a -= 32;
            boolean found = false;
            int i;
            for (i = 0; i < C2I.length && i < W; i++)
                if(C2I[i] == a) {
                    found = true;
                    break;
                }
            if(found)
                coordinates[1] = i;
            else
                throw new WrongInputEx(0);
        }

        int b = Integer.parseInt(input.substring(1)) - 1;
        if(b < 0 || b >= H)
            throw new WrongInputEx(1);
        else
            coordinates[0] = b;

        return coordinates;
    }

    private void hitCheckShips(int[] coords) {
        int hit = 0;
        for (Warship ship : warships) {
            hit = ship.hitCheck(coords);
            if (hit != 0) {
                map[coords[0]][coords[1]] = 'X';
                if (hit == 2) {
                    makeIndent(map, ship.getLocation());
                    int aliveCount = checkAliveShipsCount();
                    if (aliveCount > 0)
                        System.out.println("Осталось вражеских судов: "+ aliveCount);
                }
                break;
            }
        }
        if (hit == 0) {
            System.out.println("Мимо");
            map[coords[0]][coords[1]] = '◦';
        }
    }

    void printMap() {
        printDeck(map);
    }

    void printSea() {
        printDeck(sea);
    }

    private void printDeck(char[][] deck) {
        StringBuilder out = new StringBuilder("  " + " _".repeat(W) +"\n");
        for (int i = 0; i < H; i++) {
            for (int j = -1; j <= W; j++) {
                if (j == -1)
                    out.append(String.format("%2d", i+1));
                else {
                    out.append("|");
                    if (j != W)
                        out.append(deck[i][j]);
                }
            }
            out.append("\n");
        }
        out.append("  ");
        for (int i = 0; i < W; i++)
            out.append(String.format("%2c", C2I[i]));
        System.out.println(out);
    }

    private void makeIndent(char[][] deck, int[][] location) {
        int x = location[0][0];
        int y = location[0][1];
        int size = location.length;

        if(location.length > 1 && location[0][0] != location[1][0]) { // вертикальный
            if (x - 1 >= 0)
                deck[x - 1][y] = '◦';
            if (x + size < H)
                deck[x + size][y] = '◦';
            for (int i = x - 1; i <= x + size; i++) {
                if (i >= 0 && i < H) {
                    if (y + 1 < W)
                        deck[i][y + 1] = '◦';
                    if (y - 1 < W && y - 1 >= 0)
                        deck[i][y - 1] = '◦';
                }
            }
        } else {
            if (y - 1 >= 0)
                deck[x][y - 1] = '◦';
            if (y + size < W)
                deck[x][y + size] = '◦';
            for (int i = y - 1; i <= y + size; i++) {
                if (i >= 0 && i < W) {
                    if (x + 1 < H)
                        deck[x + 1][i] = '◦';
                    if (x - 1 < H && x - 1 >= 0)
                        deck[x - 1][i] = '◦';
                }
            }
        }
    }

    private int checkAliveShipsCount() {
        int c = 0;
        for (Warship ship : warships)
            if (ship.isAlive())
                c++;
        return c;
    }

}

public class Battleship {
    public static void main(String[] args) {
        Game.startClassic();
        /*Game g = new Game(4, 4, new int[]{5,3,3,3,3,5,3});
        g.printSea();
        g.start();*/
    }
}
