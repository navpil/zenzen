package io.github.navpil.gupai.jielong;

import io.github.navpil.gupai.jielong.player.MutableInteger;
import io.github.navpil.gupai.jielong.player.Player;
import io.github.navpil.gupai.util.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JieLongRuleSet implements RuleSet {

    @Override
    public WinningStats resolvePoints(Stats stats, List<? extends Player> players, int whoGoesFirst) {
        final List<String> originalNameList = players.stream().map(Player::getName).collect(Collectors.toList());

        Map<String, Integer> order = new HashMap<>();
        List<String> names = new ArrayList<>(originalNameList);
        for (int i = 0, size = names.size(); i < size; i++) {
            int playerIndex = (whoGoesFirst + i) % size;
            order.put(names.get(playerIndex), i);
        }

        names.sort((n1, n2) -> {
            final Integer p1 = stats.getPointsFor(n1);
            final Integer p2 = stats.getPointsFor(n2);
            if (!p1.equals(p2)) {
                return p1.compareTo(p2);
            }
            return order.get(n1).compareTo(order.get(n2));
        });
        final String winner = names.get(0);

        final HashMap<String, MutableInteger> points = new HashMap<>();
        for (String s : originalNameList) {
            points.put(s, new MutableInteger());
        }

        for (int i = 0; i < originalNameList.size(); i++) {
            final String name1 = originalNameList.get(i);
            for (int j = i + 1; j < originalNameList.size(); j++) {
                final String name2 = originalNameList.get(j);

                final Integer player1Points = stats.getPointsFor(name1);
                final Integer player2Points = stats.getPointsFor(name2);

                final int player1WinningPoints = player2Points - player1Points;

                points.get(name1).add(player1WinningPoints);
                points.get(name2).add(-player1WinningPoints);
            }
        }

        final HashMap<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, MutableInteger> stringCounterEntry : points.entrySet()) {
            result.put(stringCounterEntry.getKey(), stringCounterEntry.getValue().getCount());
        }

        return new RuleSet.WinningStats(result, winner);
    }
}
