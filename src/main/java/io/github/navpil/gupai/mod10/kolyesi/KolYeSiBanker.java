package io.github.navpil.gupai.mod10.kolyesi;

import io.github.navpil.gupai.Domino;

public class KolYeSiBanker implements KolYeSiPlayer {

    private int money;

    @Override
    public int stake(Domino domino, int minStake, int maxStake) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int dominoCount(Domino d1, Domino d2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lose(int stake) {
        money -= stake;
    }

    @Override
    public void win(int stake) {
        money += stake;
    }

    @Override
    public int getMoney() {
        return money;
    }

    @Override
    public boolean stillHasMoney() {
        return true;
    }

    @Override
    public boolean isBankrupt() {
        return false;
    }

    @Override
    public String getName() {
        return "Banker";
    }
}
