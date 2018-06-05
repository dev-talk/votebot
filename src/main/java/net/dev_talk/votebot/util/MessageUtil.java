package net.dev_talk.votebot.util;

import net.dv8tion.jda.core.EmbedBuilder;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

public class MessageUtil {
    private final Properties properties;
    private final EmbedBuilder embedBuilder;

    /**
     * Constructs a new MessageUtil.
     * It is used to get messages from the provided file with an identifier-key.
     *
     * @param messageFile         File where the messages are located
     * @param defaultResourcePath Relative path where the default resource is located
     * @param embedBuilder        EmbedBuilder to save
     *
     * @throws IOException Exception thrown while interacting with the file
     */
    public MessageUtil(final File messageFile, final String defaultResourcePath, final EmbedBuilder embedBuilder)
            throws IOException {
        ResourceUtil.saveDefaultResource(getClass().getClassLoader(), defaultResourcePath, messageFile);

        try (final Reader reader = new BufferedReader(new FileReader(messageFile))) {
            properties = new Properties();
            properties.load(reader);
        }

        this.embedBuilder = embedBuilder;
    }

    public EmbedBuilder getEmbedBuilder() {
        return new EmbedBuilder(embedBuilder);
    }

    public Optional<String> getMessage(final String key, final Object... arguments) {
        final String message = properties.getProperty(key);
        return message == null ? Optional.empty() : Optional.of(String.format(message, arguments));
    }

    public String getMessageOrKey(final String key, final Object... arguments) {
        return getMessage(key, arguments).orElse(key);
    }
}
