package converters;

public class ChessPositionConverter {

    public static int[] convertMove(String part) {

        StringBuilder result = new StringBuilder();

        int column = part.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(part.charAt(1));
        return new int[]{row, column};
    }
}
