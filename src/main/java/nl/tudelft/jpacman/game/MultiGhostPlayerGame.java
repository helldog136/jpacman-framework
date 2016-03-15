package nl.tudelft.jpacman.game;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.level.*;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.GhostColor;
import nl.tudelft.jpacman.sprite.PacManSprites;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by helldog136 on 25/02/16.
 */
public class MultiGhostPlayerGame extends Game {

    private static final long HUNTER_SWITCH_INTERVAL = 5*1000;
    private static final int SWITCH_PROBA_START = 100;
    public static final long PENALTY_TIME = 5000;
    /**
     * The players of this game.
     */
    private final ArrayList<HunterGhostPlayer> players;
    
    private final ArrayList<HunterGameModePlayer> potentialHunters = new ArrayList<>();

    /**
     * The level of this game.
     */
    private final Level level;
    private boolean colorsChosen = false;
    private PacManSprites sprites = new PacManSprites();

    /**
     * Create a new multi players game for the provided level and players.
     * @param _players The players.
     * @param l The level.
     */
    protected MultiGhostPlayerGame(ArrayList<HunterGhostPlayer> _players, Level l) {
        assert _players != null;
        assert _players.size() > 0;
        assert l != null;

        this.players = _players;
        potentialHunters.addAll(_players);
        potentialHunters.addAll(l.getGhosts().stream().map(g -> (HunterGameModePlayer) g).collect(Collectors.toList()));
        this.level = l;
        this.level.setCollisions(new MultiGhostPlayerCollisions());
        for (Player p : players) {
            level.registerPlayer(p);
        }
        for (NPC g : l.getGhosts()) {
            level.registerUnitOnStartSquare(g);
        }
    }

    private int currentHunterIndex = 0;
    private Timer hunterSwitchTimer = new Timer();
    private double[] hunterSwitchProbs = {SWITCH_PROBA_START,SWITCH_PROBA_START,SWITCH_PROBA_START,SWITCH_PROBA_START};

    @Override
    public void start(){
        if(!colorsChosen){

        }else{
            super.start();
        }
    }

    @Override
    public void notifyEnter(){
        colorsChosen = true;
        sprites = null;
        start();
    }

    @Override
    public void move(Player p, Direction d){
        if(!colorsChosen){
            switch (d){
                case NORTH:
                    ((GhostPlayer)p).setColor(sprites.getGhostSprite(GhostColor.RED));
                    break;
                case SOUTH:
                    ((GhostPlayer)p).setColor(sprites.getGhostSprite(GhostColor.ORANGE));
                    break;
                case EAST:
                    ((GhostPlayer)p).setColor(sprites.getGhostSprite(GhostColor.CYAN));
                    break;
                case WEST:
                    ((GhostPlayer)p).setColor(sprites.getGhostSprite(GhostColor.PINK));
                    break;
            }
        }else{
            super.move(p,d);
        }
    }
    
    @Override
    void customStart() {
        long interval = HUNTER_SWITCH_INTERVAL + (new Random().nextInt(11) * 1000);
        if(isInProgress()){
            //first choose (weighted-)randomly a player
            int bestval = Integer.MIN_VALUE;
            int index = 0;
            for (int i = 0; i < 4; i++) {
                int val = new Random().nextInt((int)hunterSwitchProbs[i]);
                if(val > bestval){
                    index = i;
                    bestval = val;
                }
            }
            //then divide by 2 the weight of chances to be a hunter
            hunterSwitchProbs[index] = hunterSwitchProbs[index]/2;
            //then check if must not reset weights
            checkHunterSwitchProbs();
            potentialHunters.get(currentHunterIndex).setHunter(false);
            currentHunterIndex = index;
            potentialHunters.get(currentHunterIndex).setHunter(true);
        }
        hunterSwitchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                customStart();
            }
        }, interval);
    }

    private void checkHunterSwitchProbs() {
        boolean must = true;
        for (int i = 0; i < 4; i++) {
            if(hunterSwitchProbs[i] > SWITCH_PROBA_START - 1){
                must = false;
            }
        }
        if(must){
            for (int i = 0; i < 4; i++) {
                hunterSwitchProbs[i] = hunterSwitchProbs[i] * 2;
            }
        }
    }

    @Override
    public List<Scorer> getScorers() {
        return Arrays.asList(potentialHunters.toArray(new Scorer[potentialHunters.size()]));
    }
    
    @Override
    public List<Player> getPlayers(){
        return Arrays.asList(players.toArray(new Player[players.size()]));
    }

    @Override
    public Level getLevel() {
        return level;
    }

}
