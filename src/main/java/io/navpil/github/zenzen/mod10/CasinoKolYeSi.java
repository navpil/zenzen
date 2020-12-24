package io.navpil.github.zenzen.mod10;

import io.navpil.github.zenzen.ChineseDominoSet;
import io.navpil.github.zenzen.dominos.Domino;
import io.navpil.github.zenzen.dominos.DominoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CasinoKolYeSi {

    public static void main(String[] args) {

        int sim = 100;
        int minStake = 1;
        int maxStake = 10;


//        final ConsoleInput consoleInput = new ConsoleInput();
        final RealKolYeSiPlayer realKolYeSiPlayer = new RealKolYeSiPlayer("Jim", 0, 100);

        final List<Domino> dominos = ChineseDominoSet.create();
        game_loop:
        for (int i = 0; i < sim && realKolYeSiPlayer.stillHasMoney(); i++) {
            Collections.shuffle(dominos);

            List<Domino> userDominoes = new ArrayList<>();
            final Domino domino = dominos.get(0);
            userDominoes.add(domino);

            int stake = realKolYeSiPlayer.stake(domino, minStake, maxStake);
//            System.out.println("You've got: " + domino);
//
//            int maxPossibleStake = Math.min(money, maxStake);
//            int stake = consoleInput.readInt(
//                    (s) -> s >= minStake && s <= maxPossibleStake,
//                    "You have " + money + " left, what's your stake?",
//                    "Invalid stake, min: " + minStake + ", max: " + maxPossibleStake);

            final Domino d1 = dominos.get(1);
            final Domino d2 = dominos.get(2);

            System.out.println("Banker got " + d1 + ", " + d2);

            if (DominoUtil.isWenPair(d1, d2)) {
                System.out.println("Perfect pair, instant banker win");
                realKolYeSiPlayer.lose(stake);
//                money = money - stake;
                continue game_loop;
            }

            int dominoCount = realKolYeSiPlayer.dominoCount(d1, d2);
//            int dominoCount = consoleInput.readInt(
//                    (s) -> s == 1 || s == 2,
//                    "How many tiles you want to pick, 1 or 2?",
//                    "Invalid input, try again");

            userDominoes.add(dominos.get(3));
            if (dominoCount == 2) {
                userDominoes.add(dominos.get(4));
            }
            System.out.println("You have these dominoes: " + userDominoes);

            int bankersPoints = Mod10Calculation.mod10(d1, d2);
            int playerPoints = Mod10Calculation.mod10(userDominoes);

            if (bankersPoints == playerPoints) {
                System.out.println("Both of you got same " + bankersPoints + " points, it's a push");
            } else if (bankersPoints > playerPoints) {
                System.out.println("Banker won with " + bankersPoints + " points, against your " + playerPoints + " it's a loss");
                realKolYeSiPlayer.lose(stake);
            } else {
                System.out.println("You won with " + playerPoints + " points, against bankers " + bankersPoints + " it's a win");
                realKolYeSiPlayer.win(stake);
            }
        }
        if (realKolYeSiPlayer.stillHasMoney()) {
            System.out.println("Congratulations, you have won, you have " + realKolYeSiPlayer.getMoney() + " Won");
        } else {
            System.out.println("Sorry, you've lost");
        }

    }

}