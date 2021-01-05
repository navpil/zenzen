package io.navpil.github.zenzen.jielong.player;

import io.navpil.github.zenzen.jielong.player.evaluators.CombiningMoveEvaluator;
import io.navpil.github.zenzen.jielong.player.evaluators.MinMaxMoveEvaluator;
import io.navpil.github.zenzen.jielong.player.evaluators.PipCounterMoveEvaluator;
import io.navpil.github.zenzen.jielong.player.evaluators.RarenessMoveEvaluator;
import io.navpil.github.zenzen.jielong.player.evaluators.SuanZhangMoveEvaluator;

public class PlayerFactory {

    public static Player createCombiningStrategyPlayer(String name) {
        return new PriorityPlayer(name, new CombiningMoveEvaluator()
                .addEvaluator(new MinMaxMoveEvaluator())
                .addEvaluator(new RarenessMoveEvaluator())
                .addEvaluator(new PipCounterMoveEvaluator(), 20)
                .addEvaluator(new SuanZhangMoveEvaluator(), 0));
    }

    public static Player createMinMaxPlayer(String name) {
        return new PriorityPlayer(name, new CombiningMoveEvaluator()
                .addEvaluator(new MinMaxMoveEvaluator())
                .addEvaluator(new PipCounterMoveEvaluator(), 20)
                .addEvaluator(new SuanZhangMoveEvaluator(), 0));
    }

    public static Player createRarenessPlayer(String name) {
        return new PriorityPlayer(name, new CombiningMoveEvaluator()
                .addEvaluator(new RarenessMoveEvaluator())
                .addEvaluator(new PipCounterMoveEvaluator(), 20)
                .addEvaluator(new SuanZhangMoveEvaluator(), 0));
    }

    public static Player createRandomPlayerImpl(String name) {
        return new PriorityPlayer(name, new CombiningMoveEvaluator());
    }
}
