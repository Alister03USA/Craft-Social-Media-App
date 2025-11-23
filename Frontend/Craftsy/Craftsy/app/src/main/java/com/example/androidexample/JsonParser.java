package com.example.androidexample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses Boards + Items from backend JSON, mapping directly
 * to your Pattern, FeedItem, and TutorialItem models.
 */
public class JsonParser {

    public static List<BoardModel> parseBoards(JSONArray array) {
        List<BoardModel> list = new ArrayList<>();
        if (array == null) return list;

        for (int i = 0; i < array.length(); i++) {
            try {
                list.add(parseBoard(array.getJSONObject(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static BoardModel parseBoard(JSONObject obj) {
        BoardModel board = new BoardModel();
        try {
            board.id = obj.getLong("id");
            board.boardName = obj.optString("boardName", "");
            board.description = obj.optString("description", "");
            board.dateCreated = obj.optString("dateCreated", "");

            board.patterns = parsePatterns(obj.optJSONArray("patterns"));
            board.projects = parseFeedItems(obj.optJSONArray("projects"));
            board.tutorials = parseTutorials(obj.optJSONArray("tutorials"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return board;
    }

    /* ------------------------------------------------------------
       PATTERNS (Matches your Pattern.java constructor EXACTLY)
       ------------------------------------------------------------ */
    private static List<Pattern> parsePatterns(JSONArray arr) throws JSONException {
        List<Pattern> list = new ArrayList<>();
        if (arr == null) return list;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.getJSONObject(i);
            list.add(new Pattern(
                    p.getLong("id"),
                    p.optString("patternName", ""),
                    p.optString("username", ""),
                    p.optString("patternType", ""),
                    (float) p.optDouble("rating", 0),
                    p.optString("patternImage", ""),
                    p.optString("patternLink", ""),
                    p.optString("difficulty", ""),
                    p.optString("description", ""),
                    p.optString("supplies", ""),
                    p.optString("date", "")
            ));
        }
        return list;
    }

    /* ------------------------------------------------------------
       FEED ITEMS (Matches FeedItem.java constructor EXACTLY)
       ------------------------------------------------------------ */
    private static List<FeedItem> parseFeedItems(JSONArray arr) throws JSONException {
        List<FeedItem> list = new ArrayList<>();
        if (arr == null) return list;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject f = arr.getJSONObject(i);
            list.add(new FeedItem(
                    f.optString("username", ""),
                    f.optString("projectName", ""),
                    f.optString("projectType", ""),
                    f.optString("supplies", ""),
                    f.optString("projectDesc", ""),
                    f.optString("visibility", ""),
                    f.optString("date", ""),
                    f.optString("imageUrl", "")
            ));
        }
        return list;
    }

    /* ------------------------------------------------------------
       TUTORIALS (Matches TutorialItem.java constructor EXACTLY)
       ------------------------------------------------------------ */
    private static List<TutorialItem> parseTutorials(JSONArray arr) throws JSONException {
        List<TutorialItem> list = new ArrayList<>();
        if (arr == null) return list;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject t = arr.getJSONObject(i);
            list.add(new TutorialItem(
                    t.getLong("id"),
                    t.optString("title", ""),
                    t.optString("description", ""),
                    t.optString("category", ""),
                    t.optString("fileURL", ""),
                    t.optString("filePath", ""),
                    t.optString("username", "")
            ));
        }
        return list;
    }
}