package net.dev_talk.votebot.data.vote;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dev_talk.votebot.data.json.JsonDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class VoteDataHandler extends JsonDataHandler {
    private static final Logger logger = LoggerFactory.getLogger(VoteDataHandler.class);

    private static final String VOTE_ARRAY_NAME = "votes";
    private static final int WRITE_CHANGES_PERIOD_MINUTES = 10;

    private final Set<Vote> voteCache;
    private final JsonArray votesJsonArray;

    /**
     * Constructs a new VoteDataHandler.
     * The changes made are automatically saved all 10 minutes.
     *
     * @param gson            Gson instance to use for interacting with the json object.
     * @param jsonFile        Json-File to read from and write to
     * @param executorService ExecutorService to schedule the write actions
     *
     * @throws IOException Exception while creating the file
     */
    public VoteDataHandler(final Gson gson, final File jsonFile, final ExecutorService executorService) throws IOException {
        super(gson, jsonFile);
        voteCache = new HashSet<>();

        final JsonObject jsonObject = getJsonObject();
        if (jsonObject.getAsJsonArray(VOTE_ARRAY_NAME) == null) {
            jsonObject.add(VOTE_ARRAY_NAME, new JsonArray());
        }
        votesJsonArray = getJsonObject().getAsJsonArray(VOTE_ARRAY_NAME);
        for (final JsonElement jsonElement : votesJsonArray) {
            voteCache.add(new Vote(jsonElement.getAsJsonObject(), this));
        }

        executorService.execute(() -> {
            while (!Thread.interrupted()) {
                try {
                    writeChanges();
                } catch (final IOException exc) {
                    logger.error(String.format("Encountered exception while writing changes to data file %s!", file), exc);
                }
                LockSupport.parkNanos(TimeUnit.MINUTES.toNanos(WRITE_CHANGES_PERIOD_MINUTES)); //block this thread
            }
        });
    }

    /**
     * Gets the lock for interacting with the vote data.
     * Make sure to unlock the lock in a finally block!
     *
     * @return {@link ReentrantLock} to lock on
     */
    public final ReentrantLock getLock() {
        return lock;
    }

    public Optional<Vote> getVote(final long messageId) {
        return voteCache.stream().filter(vote -> vote.getMessageId() == messageId).findFirst();
    }

    public Vote createVote(final long messageId, final long authorId, final boolean silent) {
        try {
            lock.lock();
            final Vote vote = new Vote(messageId, authorId, silent, this);
            votesJsonArray.add(vote.getJsonObject());
            voteCache.add(vote);
            return vote;
        } finally {
            lock.unlock();
        }
    }

    public void deleteVote(final Vote vote) {
        try {
            lock.lock();
            votesJsonArray.remove(vote.getJsonObject());
            voteCache.remove(vote);
        } finally {
            lock.unlock();
        }
    }
}
