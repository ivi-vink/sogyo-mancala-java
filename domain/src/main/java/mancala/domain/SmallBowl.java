package mancala.domain;

public class SmallBowl implements Bowl {
    private int myRocks;
    private final Player myOwner;
    private final Bowl nextBowl;

    public SmallBowl() {
        this.myRocks = 4;

        this.myOwner = new Player();
        int boardSize = 14;

        this.nextBowl = new SmallBowl(boardSize, --boardSize, this, this.getMyOwner());
    }

    SmallBowl(int boardSize, int remainingBowls, Bowl startBowl, Player playerOwningThisSide) {
        this.myRocks = 4;
        this.myOwner = playerOwningThisSide;

        boolean startingFromKalaha = startBowl.getClass() == Kalaha.class;
        int startingFromKalahaAdjustment = 0;
        if (startingFromKalaha)
            startingFromKalahaAdjustment = -1;

        // One more connection to make?
        if (remainingBowls == 1) {
            this.nextBowl = startBowl;
        } // Where are the Kalahas?
        else if (remainingBowls == boardSize / 2 + 2 + startingFromKalahaAdjustment || (remainingBowls == 2 && !startingFromKalaha)) {
            this.nextBowl = new Kalaha(boardSize, --remainingBowls, startBowl, playerOwningThisSide);
        } else {
            this.nextBowl = new SmallBowl(boardSize, --remainingBowls, startBowl, playerOwningThisSide);
        }
    }

    @Override
    public int getMyRocks() {
        return myRocks;
    }

    @Override
    public Bowl getNextBowl() {
        return nextBowl;
    }

    @Override
    public Player getMyOwner() {
        return myOwner;
    }

    public SmallBowl getNextSmallBowlTimes(int remainingTimes) {
        if (remainingTimes == 0)
            return this;
        else {
            return getNextSmallBowl().getNextSmallBowlTimes(--remainingTimes);
        }
    }

    public void play() {
        if ((!myOwner.hasTheTurn())) return;
        if (myRocks == 0) return;

        Bowl lastToReceiveRock;
        int myRocksBefore = myRocks;
        // Which distribute method do we need?
        if (getNextBowl().getClass() == Kalaha.class) {
            lastToReceiveRock = getNextKalaha().distribute(myRocks);
        } else {
            lastToReceiveRock = getNextSmallBowl().distribute(myRocks);
        }
        myRocks = (myRocks - myRocksBefore);

        // Did play end in smallbowl of my player? steal, otherwise do nothing
        if (lastToReceiveRock.getClass() == SmallBowl.class && lastToReceiveRock.getMyOwner().equals(getMyOwner())) {
            stealTheBooty((SmallBowl) lastToReceiveRock);
        }

        // Did play end in Kalaha? do nothing, otherwise switch turn
        if (!(lastToReceiveRock.getClass() == Kalaha.class)) {
            getMyOwner().switchTurn();
        }

        // Should a player win or is it a draw? tell player he won/ drew, otherwise do nothing
        endTheGame();
    }

    Bowl distribute(int remainingRocks) {
        this.myRocks++;
        // last?
        if (remainingRocks == 1)
            return this;
        else {
            if (getNextBowl().getClass() == SmallBowl.class) {
                return getNextSmallBowl().distribute(--remainingRocks);
            } else {
                return getNextKalaha().distribute(--remainingRocks);
            }
        }
    }

    private void stealTheBooty(SmallBowl thievingBowl) {
        SmallBowl victim = thievingBowl.getOpposite();
        int booty;
        if (thievingBowl.getMyRocks() == 1 && victim.getMyRocks() != 0) {
            booty = victim.getMyRocks();
            booty++;
            thievingBowl.myRocks = 0;
            victim.myRocks = 0;
            getNextKalaha().claimStolenBooty(booty);
        }
    }

    private void endTheGame() {
        SmallBowl firstBowlPlayer = (SmallBowl) getOpposite().getNextKalaha().getNextBowl();
        int playerRocks = firstBowlPlayer.countRocksInSmallBowlsUntilOpponentBowls();

        if (playerRocks == 0) return;

        SmallBowl firstBowlOpponent = (SmallBowl) getNextKalaha().getNextBowl();
        int opponentRocks = firstBowlOpponent.countRocksInSmallBowlsUntilOpponentBowls();

        int playerKalaha = getNextKalaha().getMyRocks();
        int opponentKalaha = getOpposite().getNextKalaha().getMyRocks();

        if ((playerRocks + playerKalaha) == (opponentRocks + opponentKalaha)) {
            getMyOwner().gotADraw();
        } else if ((playerRocks + playerKalaha) > (opponentRocks + opponentKalaha)) {
            getMyOwner().isTheWinner();
        } else {
            getMyOwner().getOpponent().isTheWinner();
        }
    }

    private SmallBowl getNextSmallBowl() {
        if (getNextBowl().getClass() == Kalaha.class) {
            return (SmallBowl) getNextBowl().getNextBowl();
        } else {
            return (SmallBowl) getNextBowl();
        }
    }

    private Kalaha getNextKalaha() {
        if (!(getNextBowl().getClass() == Kalaha.class)) {
            return getNextSmallBowl().getNextKalaha();
        } else {
            return (Kalaha) getNextBowl();
        }
    }

    private SmallBowl getOpposite() {
        SmallBowl opponentFirst = (SmallBowl) getNextKalaha().getNextBowl();
        return opponentFirst.getNextSmallBowlTimes(countStepsToKalaha());
    }

    private int countStepsToKalaha() {
        if (getNextBowl().getClass() == Kalaha.class) {
            return 0;
        } else {
            return 1 + getNextSmallBowl().countStepsToKalaha();
        }
    }

    private int countRocksInSmallBowlsUntilOpponentBowls() {
        if (!(getNextSmallBowl().getMyOwner().equals(getMyOwner()))) {
            return this.myRocks;
        } else {
            return this.myRocks + getNextSmallBowl().countRocksInSmallBowlsUntilOpponentBowls();
        }
    }
}