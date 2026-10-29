package com.campus.lostfound.util;

import com.google.gson.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class JsonUtil {
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    String dateStr = json.getAsString();
                    if (dateStr == null || dateStr.trim().isEmpty()) return null;
                    try {
                        if (dateStr.length() == 10) {
                            return new Date(DATE_ONLY_FORMAT.parse(dateStr).getTime());
                        }
                        return new Date(ISO_FORMAT.parse(dateStr).getTime());
                    } catch (ParseException e) {
                        try {
                            return Date.valueOf(dateStr);
                        } catch (Exception ex) {
                            throw new JsonParseException("Could not parse Date: " + dateStr, ex);
                        }
                    }
                }
            })
            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(DATE_ONLY_FORMAT.format(src));
                }
            })
            .registerTypeAdapter(Timestamp.class, new JsonSerializer<Timestamp>() {
                @Override
                public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(ISO_FORMAT.format(src));
                }
            })
            .create();

    public static Gson getGson() {
        return gson;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static void sendJsonResponse(HttpServletResponse resp, int statusCode, Object data) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    public static void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(String.format("{\"error\": \"%s\", \"status\": %d}", message.replace("\"", "\\\""), statusCode));
        out.flush();
    }
}
