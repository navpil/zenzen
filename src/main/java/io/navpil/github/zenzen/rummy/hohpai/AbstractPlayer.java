package io.navpil.github.zenzen.rummy.hohpai;

import io.navpil.github.zenzen.dominos.Domino;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractPlayer implements Player {

    private final String name;
    protected Table table;
    protected ArrayList<Domino> dominos;
    private HandCalculator handCalculator;

    public AbstractPlayer(String name) {
        this.name = name;
    }


    protected Collection<Hand> getHands() {
        return handCalculator
                .handPermutations(dominos);
    }

    @Override
    public void deal(Collection<Domino> deal) {
        dominos = new ArrayList<>(deal);
    }

    @Override
    public void showTable(Table table) {
        this.table = table;
        handCalculator = new HandCalculator(table.getRuleSet());
    }

    @Override
    public String getName() {
        return name;
    }
}
