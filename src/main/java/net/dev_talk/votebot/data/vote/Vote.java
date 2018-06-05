package net.dev_talk.votebot.data.vote;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Vote {
    private final JsonObject jsonObject;
    private final ReentrantLock lock;
    private final long messageId;
    private final long authorId;
    private final boolean silent;
    private final Set<Long> yesVoters;
    private final Set<Long> noVoters;
    private boolean evaluated;

    public Vote(final JsonObject jsonObject, final VoteDataHandler voteHandler) {
        this.jsonObject = jsonObject;
        this.lock = voteHandler.getLock();
        messageId = jsonObject.getAsJsonPrimitive("messageId").getAsLong();
        authorId = jsonObject.getAsJsonPrimitive("authorId").getAsLong();
        evaluated = jsonObject.getAsJsonPrimitive("evaluated").getAsBoolean();
        silent = jsonObject.getAsJsonPrimitive("silent").getAsBoolean();
        yesVoters = new HashSet<>();
        noVoters = new HashSet<>();
        jsonObject.getAsJsonArray("yesVoters").forEach(id -> noVoters.add(id.getAsLong()));
        jsonObject.getAsJsonArray("noVoters").forEach(id -> yesVoters.add(id.getAsLong()));
    }

    public Vote(final long messageId, final long authorId, final boolean silent, final VoteDataHandler voteHandler) {
        jsonObject = new JsonObject();
        this.messageId = messageId;
        jsonObject.add("messageId", new JsonPrimitive(messageId));
        this.authorId = authorId;
        jsonObject.add("authorId", new JsonPrimitive(authorId));
        evaluated = false;
        jsonObject.add("evaluated", new JsonPrimitive(false));
        this.silent = silent;
        jsonObject.add("silent", new JsonPrimitive(silent));
        yesVoters = new HashSet<>();
        jsonObject.add("yesVoters", new JsonArray());
        noVoters = new HashSet<>();
        jsonObject.add("noVoters", new JsonArray());
        this.lock = voteHandler.getLock();
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(final boolean evaluated) {
        try {
            lock.lock();
            this.evaluated = evaluated;
            jsonObject.add("evaluated", new JsonPrimitive(evaluated));
        } finally {
            lock.unlock();
        }
    }

    public boolean isSilent() {
        return silent;
    }

    public Set<Long> getYesVoters() {
        return yesVoters;
    }

    public Set<Long> getNoVoters() {
        return noVoters;
    }

    public boolean voteYes(final long voterId) {
        try {
            lock.lock();
            final boolean result = yesVoters.add(voterId);
            if (result) {
                jsonObject.getAsJsonArray("yesVoters").add(new JsonPrimitive(voterId));
                if (noVoters.remove(voterId)) {
                    jsonObject.getAsJsonArray("noVoters").remove(new JsonPrimitive(voterId));
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public boolean voteNo(final long voterId) {
        try {
            lock.lock();
            final boolean result = noVoters.add(voterId);
            if (result) {
                jsonObject.getAsJsonArray("noVoters").add(new JsonPrimitive(voterId));
                if (yesVoters.remove(voterId)) {
                    jsonObject.getAsJsonArray("yesVoters").remove(new JsonPrimitive(voterId));
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}
