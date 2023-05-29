package space.moonstudio.vkbook;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public List<String> splitString(String input, int maxLength) {
        List<String> resultList = new ArrayList<>();
        int length = input.length();
        for (int i = 0; i < length; i += maxLength) {
            int endIndex = Math.min(i + maxLength, length);
            String substring = input.substring(i, endIndex);
            resultList.add(substring);
        }
        return resultList;
    }
}
