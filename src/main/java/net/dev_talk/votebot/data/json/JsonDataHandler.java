package net.dev_talk.votebot.data.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class JsonDataHandler {
    protected final Gson gson;
    protected final File file;
    protected final JsonObject jsonObject;
    protected final ReentrantLock lock;

    /**
     * Constructs a new JsonDataHandler.
     *
     * @param gson     Gson instance to use for interacting with the json object.
     * @param jsonFile Json-File to read from and write to
     *
     * @throws IOException Exception while creating the file
     */
    public JsonDataHandler(final Gson gson, final File jsonFile)
            throws IOException {
        this.gson = gson;
        file = jsonFile;
        lock = new ReentrantLock();
        if (!jsonFile.exists()) {
            jsonFile.getParentFile().mkdirs();
            jsonObject = new JsonObject();
            writeChanges(); //save
        } else {
            try (final Reader reader = new BufferedReader(new FileReader(jsonFile))) {
                jsonObject = gson.fromJson(reader, JsonObject.class);
            }
        }
    }

    public final JsonObject getJsonObject() {
        return jsonObject;
    }

    public void writeChanges() throws IOException {
        final JsonObject cloned; //clone the object here to not block any other actions while writing to the file
        try {
            lock.lock();
            cloned = jsonObject.deepCopy();
        } finally {
            lock.unlock();
        }

        try (final Writer writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(cloned, writer);
        }
    }
}
