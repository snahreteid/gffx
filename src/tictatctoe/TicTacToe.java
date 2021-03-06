package tictactoe;

import gffx.game.Game;
import gffx.game.Game2D;
import gffx.game.entity.Player;
import gffx.game.entity.PlayerAI;
import gffx.game.rule.AIOperation;
import gffx.game.world.Field2D;

public class TicTacToe extends Game2D {
    private int next = 0, winPoints;
    private boolean gravity;

    public TicTacToe(Player[] players, Field2D field) throws Exception {
        this(players, field, field.height());
    }

    public TicTacToe(Player[] players, Field2D field, int winPoints) throws Exception {
        this(players, field, winPoints, false);
    }

    public TicTacToe(Player[] players, Field2D field, int winPoints, boolean gravity) throws Exception {
        super(players, field);
        this.winPoints = winPoints;
        this.gravity = gravity;
        init();
    }

    public TicTacToe(TicTacToe other) {
        super(other.getPlayers().toArray(new Player[0]), new Field2D(other.getField()));
        winPoints = other.winPoints;
        gravity = other.gravity;

        try {
            init();
            next = other.next;
        } catch(Exception e) { // this shouldn't happen
            e.printStackTrace();
        }
    }

    public void setWinPoints(int points) {
        winPoints = points;
    }

    public int getWinPoints() {
        return winPoints;
    }

    private void init() throws Exception {
        setTurnCondition(() -> {
            if(gravity) {
                setCursor(getCursorX(), 0);
                while(getCursorY() < getField().height()-1 && getField().get(getCursorX(), getCursorY()+1) == null)
                    setCursor(getCursorX(), getCursorY()+1);
            }

            if(getField().get(getCursorX(), getCursorY()) == null) {
                int last = next;
                getField().set(getCursorX(), getCursorY(), getPlayers().get(next));
                next = (next+1)%getPlayers().size();
                return getPlayers().get(last);
            }

            return null;
        });

        setWinCondition((p) -> {
            if(p != null) {
                int wdt = getField().width(), hgt = getField().height(), dgt = wdt+hgt-1;
                int[] hpoints = new int[hgt];
                int[] vpoints = new int[wdt];
                int[] d1points = new int[dgt];
                int[] d2points = new int[dgt];
                
                for(int d1, d2, y = 0; y < hgt; ++y) {
                    for(int x = 0; x < wdt; ++x) {
                        d1 = (hgt-1) + (x-y);
                        d2 = x+y;

                        if(getField().get(x, y) == p) {
                            hpoints[y]++;
                            vpoints[x]++;
                            d1points[d1]++;
                            d2points[d2]++;

                            if(hpoints[y] >= winPoints || vpoints[x] >= winPoints
                            || d1points[d1] >= winPoints || d2points[d2] >= winPoints)
                                return true;
                        } else {
                            hpoints[y] = 0;
                            vpoints[x] = 0;
                            d1points[d1] = 0;
                            d2points[d2] = 0;
                        }
                    }
                }
            }

            return false;
        });

        for(int j, i = 0; i < getPlayers().size()-1; ++i) {
            if(getPlayers().get(i) == null)
                throw new Exception("player symbol must not be null");

            for(j = i+1; j < getPlayers().size(); ++j)
                if(getPlayers().get(i).getSprite() == getPlayers().get(j).getSprite())
                    throw new Exception("only one player per symbol allowed");
        }
    }

    public Player getNextPlayer() {
        return getPlayers().get(next);
    }
    
    @Override
    public void reset() {
        next = 0;
        getField().clear();
    }

    @Override
    public void aiMove(AIOperation op) {
        try {
            ((PlayerAI)getPlayers().get(next)).move(this, op);
        } catch(ClassCastException e) { /* not a PlayerAI */ }
    }

    @Override
    public boolean isRunning() {
        for(Player p : getPlayers())
            if(winCondition().check(p))
                return false;

        for(int y = 0; y < getField().height(); ++y)
            for(int x = 0; x < getField().width(); ++x)
                if(getField().get(x, y) == null)
                    return true;

        return false;
    }
    
    @Override
    public Game copy() {
        return new TicTacToe(this);
    }

    @Override
    public String getTitle() {
        return "TicTacToe";
    }
}