package ui.responseobjects;

import model.GameData;

import java.util.Arrays;
import java.util.Objects;

public record ListGamesResponse(GameData[] games) {

    @Override
    public GameData[] games() {
        return games;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListGamesResponse that = (ListGamesResponse) o;
        return Objects.deepEquals(games, that.games);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(games);
    }
}