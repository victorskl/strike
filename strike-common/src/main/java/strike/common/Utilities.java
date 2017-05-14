package strike.common;

import org.apache.commons.lang3.StringUtils;

public class Utilities {

    private static final int MIN_CHAR = 2;
    private static final int MAX_CHAR = 17;

    public static boolean isIdValid(String id) {
        // The identity must be
        // an alphanumeric string starting with an upper or lower case character.
        // It must be at least 3 characters and no more than 16 characters long
        int length = id.length();
        return (StringUtils.isAlphanumeric(id) && length > MIN_CHAR && length < MAX_CHAR);
    }
}
