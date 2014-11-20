package com.artemis.managers;

import com.artemis.World;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TeamManagerTest {

    World world;
    TeamManager teamManager;

    @Before
    public void setup() {
        world = new World();
        teamManager = new TeamManager();
        world.setManager(teamManager);
        world.initialize();
    }

    @After
    public void tearDown() {
        world.deleteManager(teamManager);
        teamManager = null;
        world = null;
    }

    @Test
    public void testGetTeam() throws Exception {
        assertEquals(null, teamManager.getTeam(null));
        assertEquals(null, teamManager.getTeam(""));
        assertEquals(null, teamManager.getTeam("a"));
    }

    @Test
    public void testSetTeam() throws Exception {
        assertEquals(null, teamManager.getTeam("playerA"));
        teamManager.setTeam("playerA", "teamA");
        assertEquals("teamA", teamManager.getTeam("playerA"));
    }

    @Test
    public void testGetPlayers() throws Exception {
        assertTrue(teamManager.getPlayers("teamA").isEmpty());
        teamManager.setTeam("playerA", "teamA");
        assertTrue(teamManager.getPlayers("teamA").contains("playerA"));
        teamManager.setTeam("playerB", "teamA");
        assertTrue(teamManager.getPlayers("teamA").contains("playerA"));
        assertTrue(teamManager.getPlayers("teamA").contains("playerB"));
    }

    @Test
    public void testRemoveFromTeam() throws Exception {
        teamManager.setTeam("playerA", "teamA");
        assertTrue(teamManager.getPlayers("teamA").contains("playerA"));
        teamManager.removeFromTeam("playerA");
        assertFalse(teamManager.getPlayers("teamA").contains("playerA"));
        teamManager.setTeam("playerA", "teamA");
        teamManager.setTeam("playerB", "teamA");
        assertTrue(teamManager.getPlayers("teamA").contains("playerA"));
        assertTrue(teamManager.getPlayers("teamA").contains("playerB"));
        teamManager.removeFromTeam("playerA");
        teamManager.removeFromTeam("playerB");
        assertTrue(teamManager.getPlayers("teamA").isEmpty());
    }

    @Test
    public void changeTeamTest() throws Exception {
        teamManager.setTeam("playerA", "teamA");
        assertTrue(teamManager.getPlayers("teamA").contains("playerA"));
        teamManager.setTeam("playerB", "teamB");
        assertTrue(teamManager.getPlayers("teamB").contains("playerB"));
        teamManager.setTeam("playerA", "teamB");
        assertTrue(teamManager.getPlayers("teamA").isEmpty());
        assertTrue(teamManager.getPlayers("teamB").contains("playerA"));
        assertTrue(teamManager.getPlayers("teamB").contains("playerB"));
    }
}