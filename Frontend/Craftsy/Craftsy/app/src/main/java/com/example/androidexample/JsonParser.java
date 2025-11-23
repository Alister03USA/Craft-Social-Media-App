package com.example.androidexample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
            board.id = obj.optLong("id", -1);
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

    private static List<Pattern> parsePatterns(JSONArray arr) {
        List<Pattern> list = new ArrayList<>();
        if (arr == null) return list;

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject p = arr.getJSONObject(i);

                long id = p.optLong("id", -1);   // avoid crash if missing

                list.add(new Pattern(
                        id,
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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static List<FeedItem> parseFeedItems(JSONArray arr) {
        List<FeedItem> list = new ArrayList<>();
        if (arr == null) return list;

        for (int i = 0; i < arr.length(); i++) {
            try {
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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    private static List<TutorialItem> parseTutorials(JSONArray arr) {
        List<TutorialItem> list = new ArrayList<>();
        if (arr == null) return list;

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject t = arr.getJSONObject(i);

                list.add(new TutorialItem(
                        t.optLong("id", -1),
                        t.optString("title", ""),
                        t.optString("description", ""),
                        t.optString("category", ""),
                        t.optString("fileURL", ""),
                        t.optString("filePath", ""),
                        t.optString("username", "")
                ));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
}