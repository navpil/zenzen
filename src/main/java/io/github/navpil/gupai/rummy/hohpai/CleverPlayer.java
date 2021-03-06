package io.github.navpil.gupai.rummy.hohpai;

import io.github.navpil.gupai.ChineseDominoSet;
import io.github.navpil.gupai.CombinationType;
import io.github.navpil.gupai.Domino;
import io.github.navpil.gupai.XuanHePaiPu;
import io.github.navpil.gupai.jielong.player.MutableInteger;
import io.github.navpil.gupai.util.CollectionUtil;
import io.github.navpil.gupai.util.HashBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CleverPlayer extends AbstractPlayer {

    public enum DiscardType {
        /**
         * Will stubbornly keep all good dominoes and discard the worst one
         */
        STUBBORN,
        /**
         * Keeps the combination with BEST tiles, and removes the worst tile from the tiles which are not included in the combination
         */
        KEEP_STRONGEST,
        /**
         * Keeps the combination with RAREST tiles, and removes the worst tile from the tiles which are not included in the combination
         */
        KEEP_RAREST
    }

    private final DiscardType discardType;

    public CleverPlayer(String name) {
        this(name, false);
    }

    public CleverPlayer(String name, boolean stubborn) {
        this(name, stubborn ? DiscardType.STUBBORN : DiscardType.KEEP_STRONGEST);
    }
    public CleverPlayer(String name, DiscardType discardType) {
        super(name);
        this.discardType = discardType;
    }

    @Override
    public void give(Domino give) {
        dominos.add(give);
    }

    @Override
    public Collection<Domino> offer(Domino lastDiscard) {
        final Collection<Collection<Domino>> collections = CollectionUtil.allPermutations(dominos, 2);
        final XuanHePaiPu xuanHePaiPu =table.getRuleSet().getXuanHePaiPu();
        for (Collection<Domino> collection : collections) {
            final ArrayList<Domino> withDomino = new ArrayList<>(collection);
            withDomino.add(lastDiscard);
            if (xuanHePaiPu.evaluate(withDomino) != CombinationType.none) {
                for (Domino domino : withDomino) {
                    this.dominos.remove(domino);
                }
                return withDomino;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Hand getWinningHand() {
        if (dominos.isEmpty()) {
            return new Hand(Collections.emptyList(), Collections.emptyList());
        }
        return getHands()
                .stream()
                .filter(Hand::isWinningHand)
                .findAny()
                .orElse(null);
    }


    @Override
    public Domino discard() {
        final Domino worstDomino = DiscardType.STUBBORN == discardType ? findWorstDominoStubbornly() : findWorstDomino();
        dominos.remove(worstDomino);
        return worstDomino;
    }

    private Domino findWorstDomino() {
        final Collection<Hand> hands = getHands();
        final HashMap<Domino, MutableInteger> dominoStrenghts = new HashMap<>();
        for (Domino domino : this.dominos) {
            dominoStrenghts.put(domino, new MutableInteger());
        }

        hands.stream().flatMap(h -> h.getCombinations().stream()).flatMap(Collection::stream).forEach(d -> dominoStrenghts.get(d).add(10));

        if (!hands.isEmpty()) {
            HashSet<Collection<Domino>> combinations = new HashSet<>();
            if (discardType == DiscardType.KEEP_STRONGEST) {
                int strongestHand = 0;
                for (Hand hand : hands) {
                    final Collection<Domino> singleCombination = hand.getCombinations().iterator().next();
                    final int handStrength = singleCombination.stream().map(dominoStrenghts::get).mapToInt(MutableInteger::getCount).sum();
                    if (handStrength > strongestHand) {
                        strongestHand = handStrength;
                        combinations = new HashSet<>();
                        combinations.add(singleCombination);
                    } else if (handStrength == strongestHand) {
                        combinations.add(singleCombination);
                    }
                }
            } else {
                int rarestHand = Integer.MAX_VALUE;
                for (Hand hand : hands) {
                    final Collection<Domino> singleCombination = hand.getCombinations().iterator().next();
                    final int handStrength = singleCombination.stream().map(dominoStrenghts::get).mapToInt(MutableInteger::getCount).sum();
                    if (handStrength < rarestHand) {
                        rarestHand = handStrength;
                        combinations = new HashSet<>();
                        combinations.add(singleCombination);
                    } else if (handStrength == rarestHand) {
                        combinations.add(singleCombination);
                    }
                }

            }
            final ArrayList<Collection<Domino>> strongestCombinations = new ArrayList<>(combinations);
            Collections.shuffle(strongestCombinations);
            final Collection<Domino> strongestCombination = strongestCombinations.get(0);

            final HashBag<Domino> deadWood = new HashBag<>(this.dominos);
            deadWood.strictRemoveAll(strongestCombination);

            return findWorstFromRemaining(deadWood);
        }

        return findWorstFromRemaining(this.dominos);
    }

    private Domino findWorstFromRemaining(Collection<Domino> dominos) {
        final HashMap<Domino, MutableInteger> dominoStrenghts = new HashMap<>();
        for (Domino domino : dominos) {
            dominoStrenghts.put(domino, new MutableInteger());
        }

        final ArrayList<Domino> allAvailableDominoes = new ArrayList<>(ChineseDominoSet.create());
        for (Domino d : table.getAllDeadDominoes()) {
            allAvailableDominoes.remove(d);
        }
        for (Domino d : dominos) {
            allAvailableDominoes.remove(d);
        }
        final XuanHePaiPu xuanHePaiPu = table.getRuleSet().getXuanHePaiPu();
        final Collection<Collection<Domino>> allPairs = CollectionUtil.allPermutations(dominos, 2);
        for (Collection<Domino> pair : allPairs) {
            for (Domino d : allAvailableDominoes) {
                final ArrayList<Domino> combo = new ArrayList<>(pair);
                combo.add(d);
                if (xuanHePaiPu.evaluate(combo) != CombinationType.none) {
                    for (Domino domino : pair) {
                        dominoStrenghts.get(domino).add(1);
                    }
                }
            }
        }

        final HashBag<Domino> duplicateFinder = new HashBag<>(dominos);
        for (Map.Entry<Domino, MutableInteger> dominoWithStrength : dominoStrenghts.entrySet()) {
            if (duplicateFinder.count(dominoWithStrength.getKey()) > 1) {
                final MutableInteger value = dominoWithStrength.getValue();
                value.set(value.getCount() / 2);
            }
        }

        int minValue = Integer.MAX_VALUE;
        HashSet<Domino> worstDominos = new HashSet<>();
        for (Map.Entry<Domino, MutableInteger> dws : dominoStrenghts.entrySet()) {
            if (dws.getValue().getCount() < minValue) {
                minValue = dws.getValue().getCount();
                worstDominos = new HashSet<>();
                worstDominos.add(dws.getKey());
            } else if (dws.getValue().getCount() == minValue) {
                worstDominos.add(dws.getKey());
            }
        }
        final ArrayList<Domino> list = new ArrayList<>(worstDominos);
        Collections.shuffle(list);
//        if (list.size() > 1) {
//            throw new RuntimeException("YES!!");
//        }
        return list.get(0);
    }

    private Domino findWorstDominoStubbornly() {

        final Collection<Hand> hands = getHands();
        final HashMap<Domino, MutableInteger> dominoStrenghts = new HashMap<>();
        for (Domino domino : this.dominos) {
            dominoStrenghts.put(domino, new MutableInteger());
        }

        final XuanHePaiPu xuanHePaiPu = table.getRuleSet().getXuanHePaiPu();

        hands.stream().flatMap(h -> h.getCombinations().stream()).flatMap(Collection::stream).forEach(d -> dominoStrenghts.get(d).add(10));

        final ArrayList<Domino> allAvailableDominoes = new ArrayList<>(ChineseDominoSet.create());
        for (Domino d : table.getAllDeadDominoes()) {
            allAvailableDominoes.remove(d);
        }
        for (Domino d : dominos) {
            allAvailableDominoes.remove(d);
        }
        final Collection<Collection<Domino>> allPairs = CollectionUtil.allPermutations(this.dominos, 2);
        for (Collection<Domino> pair : allPairs) {
            for (Domino d : allAvailableDominoes) {
                final ArrayList<Domino> combo = new ArrayList<>(pair);
                combo.add(d);
                if (xuanHePaiPu.evaluate(combo) != CombinationType.none) {
                    for (Domino domino : pair) {
                        dominoStrenghts.get(domino).add(1);
                    }
                }
            }
        }

        final HashBag<Domino> dominos = new HashBag<>(this.dominos);
        for (Map.Entry<Domino, MutableInteger> dominoWithStrength : dominoStrenghts.entrySet()) {
            if (dominos.count(dominoWithStrength.getKey()) > 1) {
                final MutableInteger value = dominoWithStrength.getValue();
                value.set(value.getCount() / 2);
            }
        }

        int minValue = Integer.MAX_VALUE;
        HashSet<Domino> worstDominos = new HashSet<>();
        for (Map.Entry<Domino, MutableInteger> dws : dominoStrenghts.entrySet()) {
            if (dws.getValue().getCount() < minValue) {
                minValue = dws.getValue().getCount();
                worstDominos = new HashSet<>();
                worstDominos.add(dws.getKey());
            } else if (dws.getValue().getCount() == minValue) {
                worstDominos.add(dws.getKey());
            }
        }
        final ArrayList<Domino> list = new ArrayList<>(worstDominos);
        Collections.shuffle(list);
        return list.get(0);
    }
}
