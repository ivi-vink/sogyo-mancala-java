package mancala.domain;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


class BowlTest {

    protected void traverseAndCheckBoard(Bowl currentBowl, int position) {
        Bowl initialBowl = currentBowl;
        int traversedCount = 0;
        int currentPosition = 0;
        for (int i = 0; i < 14; i++) {
            if ((position + traversedCount) > 14) {
                // if looping around the board, position = ((start+traversed) - total)
                // in other words the amount of bowls that the absolute position is greater than the board's total bowls
                //
                // Only relevant to check construction btw, also only checking in the case where there are 14 bowls
                currentPosition = ((traversedCount + position) - 14);
            } else
                // Or just use normal position
                currentPosition = position + traversedCount;

            // check for kalaha's, and check for smallbowl otherwise
            if (currentPosition == 7 || currentPosition == 14)
                assertEquals(currentBowl.getClass(), Kalaha.class);
            else
                assertEquals(currentBowl.getClass(), SmallBowl.class);

            currentBowl = currentBowl.getNextBowl();
            assertNotNull(currentBowl);
            traversedCount++;
        }
        assertSame(initialBowl, currentBowl);
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class For_a_normal_mancala_bowl{
        SmallBowl firstSmallBowlPlayer;

        @BeforeEach
        public void makeASmallBowlInMancala() {
            firstSmallBowlPlayer = new SmallBowl();
            traverseAndCheckBoard(firstSmallBowlPlayer, 1);
        }

        @Nested
        class GIVEN_its_the_start_of_the_game {
            @Test
            public void WHEN_before_any_small_bowls_are_played_THEN_has_four_rocks() {
                Bowl current = firstSmallBowlPlayer;
                for (int i = 0; i < 14; i++) {
                    current = current.getNextBowl();
                    if (current.getClass() == SmallBowl.class)
                        assertEquals(current.getMyStones(), 4);
                }
                assertSame(current, firstSmallBowlPlayer);
            }

            @Test
            public void WHEN_chosen_by_the_player_that_has_the_turn_THEN_distribute_its_rocks_anti_clockwise() {
                int initialRocks = firstSmallBowlPlayer.getMyStones();
                firstSmallBowlPlayer.play();
                Bowl neighbour = firstSmallBowlPlayer.getNextBowl();
                for (int i = 0; i < initialRocks; i++) {
                    assertEquals(5, neighbour.getMyStones());
                    neighbour = neighbour.getNextBowl();
                }
                assertEquals(4, neighbour.getMyStones());
            }
        }

        @Nested
        class GIVEN_the_game_is_in_a_state_where {

            @Test
            public void its_not_the_players_turn_WHEN_played_by_the_player_THEN_nothing_happens() {
                firstSmallBowlPlayer.getMyOwner().switchTurn();
                int initialRocks = firstSmallBowlPlayer.getMyStones();
                firstSmallBowlPlayer.play();
                Bowl neighbour = firstSmallBowlPlayer.getNextBowl();
                for (int i = 0; i < initialRocks; i++) {
                    assertEquals(4, neighbour.getMyStones());
                    neighbour = neighbour.getNextBowl();
                }
                assertEquals(4, neighbour.getMyStones());
            }

            @Test
            public void play_can_reach_opponents_kalaha_WHEN_played_by_the_player_THEN_opponents_kalaha_is_skipped() {
                SmallBowl playWillSkipFromThisBowl = goToSkippableState();
                int opponentKalahaRocksBefore = firstSmallBowlPlayer.getNextSmallBowlTimes(11).getNextBowl().getMyStones();
                playWillSkipFromThisBowl.play();
                int opponentKalahaRocksAfter = firstSmallBowlPlayer.getNextSmallBowlTimes(11).getNextBowl().getMyStones();
                assertEquals(opponentKalahaRocksBefore, opponentKalahaRocksAfter);
            }

            @Test
            public void the_bowl_is_empty_WHEN_the_player_plays_the_empty_bowl_THEN_nothing_happens() {
                firstSmallBowlPlayer.play();
                firstSmallBowlPlayer.getMyOwner().switchTurn();
                assertTrue(firstSmallBowlPlayer.getMyOwner().hasTheTurn());
                firstSmallBowlPlayer.play();
                assertTrue(firstSmallBowlPlayer.getMyOwner().hasTheTurn());
                assertEquals(5, firstSmallBowlPlayer.getNextBowl().getMyStones());
            }

            @Test
            public void all_small_bowls_of_the_player_are_empty_WHEN_a_play_ends_THEN_tell_players_who_won() {
                Player player = firstSmallBowlPlayer.getMyOwner();
                Player opponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6).getMyOwner();
                assertFalse(player.won());
                assertFalse(opponent.won());
                goToEndOfSillyGame();
                assertTrue(player.won());
                assertFalse(opponent.won());

            }

            @Test
            public void all_small_bowls_of_the_player_are_empty_WHEN_a_play_ends_THEN_tell_players_who_wonOPPONENTVARIATION() {
                Player player = firstSmallBowlPlayer.getMyOwner();
                Player opponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6).getMyOwner();
                goToEndOfGameWhereOpponentWins();
                assertFalse(player.won());
                assertTrue(opponent.won());
            }

            @Test
            public void the_play_would_skip_past_opponent_kalaha_at_the_last_rock_and_steal_WHEN_played_THEN_should_skip_and_steal_correctly() {
                goToSkipAndStealOnLast();
                SmallBowl firstSmallBowlOpponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6);
                assertEquals(3, firstSmallBowlPlayer.getNextSmallBowlTimes(5).getNextBowl().getMyStones());
                firstSmallBowlOpponent.getNextSmallBowlTimes(3).play();
                assertEquals(19, firstSmallBowlOpponent.getNextSmallBowlTimes(5).getNextBowl().getMyStones());
            }

            private void goToSkipAndStealOnLast() {
                SmallBowl firstSmallBowlOpponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6);
                firstSmallBowlPlayer.getNextSmallBowlTimes(1).play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(2).play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(5).play();
                firstSmallBowlPlayer.getNextSmallBowlTimes(1).play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(1).play();
                firstSmallBowlPlayer.getNextSmallBowlTimes(2).play();
                firstSmallBowlOpponent.play();
                firstSmallBowlPlayer.getNextSmallBowlTimes(3).play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(1).play();
                firstSmallBowlPlayer.getNextSmallBowlTimes(4).play();
                firstSmallBowlOpponent.play();
                // Cheating here, let player go again >:), i'm too dumb too make a loop/skip and steal play happen in fair game
                firstSmallBowlOpponent.getMyOwner().switchTurn();
                // Should skip and steal
                // this bowls rocks
                assertEquals(10, firstSmallBowlOpponent.getNextSmallBowlTimes(3).getMyStones());
                // End up here by looping around the board, thus skipping
                assertEquals(0, firstSmallBowlOpponent.getMyStones());
                // Thus steal from last bowl on players side
                assertEquals(8, firstSmallBowlPlayer.getNextSmallBowlTimes(5).getMyStones());
                // Result is big kalaha booty
                assertEquals(8, firstSmallBowlOpponent.getNextSmallBowlTimes(5).getNextBowl().getMyStones());
            }

            private void goToEndOfGameWhereOpponentWins() {
                goToSkipAndStealOnLast();
                SmallBowl firstSmallBowlOpponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6);
                firstSmallBowlOpponent.getNextSmallBowlTimes(3).play();
                firstSmallBowlPlayer.getNextSmallBowlTimes(1).play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(1).play();
                firstSmallBowlPlayer.play();
                firstSmallBowlPlayer.getMyOwner().switchTurn();
                firstSmallBowlPlayer.getNextSmallBowlTimes(3).play();
                firstSmallBowlPlayer.getMyOwner().switchTurn();
                firstSmallBowlPlayer.getNextSmallBowlTimes(4).play();
                firstSmallBowlPlayer.getNextSmallBowlTimes(5).play();
            }

            private void goToEndOfSillyGame() {
                SmallBowl firstSmallBowlOpponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6);

                // player
                // Best opening
                firstSmallBowlPlayer.getNextSmallBowlTimes(2).play();
                // Set up for steal move
                firstSmallBowlPlayer.getNextSmallBowlTimes(4).play();
                assertEquals(2, firstSmallBowlPlayer.getKalaha().getMyStones());

                // opponent
                // ... worst opening?
                firstSmallBowlOpponent.play();

                // player
                assertSame(firstSmallBowlPlayer.getNextSmallBowlTimes(4).getOpposite(), firstSmallBowlPlayer.getKalaha().getNextBowl().getNextBowl());
                firstSmallBowlPlayer.play();
                // Check if i did it properly on paper
                assertEquals(9, firstSmallBowlPlayer.getKalaha().getMyStones());
                assertEquals(0, firstSmallBowlPlayer.getNextSmallBowlTimes(4).getMyStones());
                // assertEquals(0, firstSmallBowlPlayer.getNextSmallBowlTimes(4).getOpposite().getMyRocks());

                // opponent
                firstSmallBowlOpponent.getNextSmallBowlTimes(3).play();

                //Player
                firstSmallBowlPlayer.getNextSmallBowlTimes(3).play();
                assertEquals(10, firstSmallBowlPlayer.getNextSmallBowlTimes(5).getNextBowl().getMyStones());

                // opponent makes stupid move again
                firstSmallBowlOpponent.getNextSmallBowlTimes(1).play();

                // player makes big steal
                //assertEquals(0, firstSmallBowlPlayer.getNextSmallBowlTimes(5).getNextBowl().getMyRocks());
                assertEquals(10, firstSmallBowlPlayer.getNextSmallBowlTimes(5).getNextBowl().getMyStones());
                firstSmallBowlPlayer.getNextSmallBowlTimes(2).play();
                assertEquals(19, firstSmallBowlPlayer.getNextSmallBowlTimes(5).getNextBowl().getMyStones());

                // opponent steals tiny booty
                firstSmallBowlOpponent.play();
                assertEquals(3, firstSmallBowlOpponent.getNextSmallBowlTimes(5).getNextBowl().getMyStones());

                // player is stalling until the end
                firstSmallBowlPlayer.play();

                // opponent is heading for disaster
                firstSmallBowlOpponent.getNextSmallBowlTimes(5).play();
                firstSmallBowlPlayer.play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(4).play();
                firstSmallBowlPlayer.play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(5).play();
                // everything empty!
                for (int i = 0; i < 6; i++) {
                    assertEquals(0, firstSmallBowlOpponent.getNextSmallBowlTimes(i).getMyStones());
                }

            }

            private SmallBowl goToSkippableState() {
                SmallBowl firstSmallBowlOpponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6);

                firstSmallBowlPlayer.getNextSmallBowlTimes(2).play();
                firstSmallBowlPlayer.getNextSmallBowlTimes(3).play();

                firstSmallBowlOpponent.getNextSmallBowlTimes(2).play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(3).play();

                firstSmallBowlPlayer.play();
                firstSmallBowlOpponent.play();

                firstSmallBowlPlayer.getNextSmallBowlTimes(4).play();
                firstSmallBowlOpponent.getNextSmallBowlTimes(4).play();

                // Playing this bowl should give a skip!
                assertTrue(firstSmallBowlPlayer.getNextSmallBowlTimes(5).getMyStones() >= 8);
                return firstSmallBowlPlayer.getNextSmallBowlTimes(5);
            }
        }

        @Nested
        class GIVEN_the_play_ends{

            @Test
            public void in_own_kalaha_WHEN_play_ends_THEN_turn_is_not_switched() {
                firstSmallBowlPlayer.getNextSmallBowlTimes(2).play();
                assertTrue(firstSmallBowlPlayer.getMyOwner().hasTheTurn());
            }

            @Test
            public void in_own_small_bowl_WHEN_play_ends_THEN_turn_is_switched() {
                firstSmallBowlPlayer.play();
                assertFalse(firstSmallBowlPlayer.getMyOwner().hasTheTurn());
            }

            @Test
            public void in_opponents_small_bowl_WHEN_player_plays_this_bowl_THEN_turn_is_switched() {
                firstSmallBowlPlayer.getNextSmallBowlTimes(5).play();
                assertFalse(firstSmallBowlPlayer.getMyOwner().hasTheTurn());
            }

            @Test
            public void in_own_empty_small_bowl_and_opposite_has_rocks_WHEN_play_ends_THEN_rocks_of_opposite_plus_last_rock_of_play_are_added_to_kalaha() {
                firstSmallBowlPlayer.getNextSmallBowlTimes(5).play();
                SmallBowl firstSmallBowlOpponent = firstSmallBowlPlayer.getNextSmallBowlTimes(6);
                firstSmallBowlOpponent.getNextSmallBowlTimes(5).play();
                assertSame(firstSmallBowlPlayer.getNextSmallBowlTimes(1).getOpposite(), firstSmallBowlPlayer.getKalaha().getSmallBowl().getNextSmallBowlTimes(4));
                // assertSame(firstSmallBowlPlayer.getOpposite(), firstSmallBowlPlayer.getKalaha().getNextSmallBowlTimes(5));
                firstSmallBowlPlayer.play();
                assertEquals(7, firstSmallBowlPlayer.getNextSmallBowlTimes(5).getNextBowl().getMyStones());
            }

        }


    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class a_kalaha {

        SmallBowl smallBowl;
        Kalaha kalaha;

        @BeforeEach
        public void makeKalahaInBoard() {
            smallBowl = new SmallBowl();
            kalaha = smallBowl.getNextSmallBowlTimes(6).getKalaha();
        }

        @Test
        public void exists_in_a_mancala_board() {
            traverseAndCheckBoard(kalaha, 14);
        }

        @Test
        public void has_zero_rocks_when_created() {
            Bowl current = kalaha;
            for (int i = 0; i < 14; i++) {
                current = current.getNextBowl();
                if (current.getClass() == Kalaha.class)
                    assertEquals(current.getMyStones(), 0);
            }
        }
    }
}