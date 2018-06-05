package net.dev_talk.votebot.handler;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dev_talk.votebot.core.VoteBot;
import net.dev_talk.votebot.data.vote.Vote;
import net.dev_talk.votebot.data.vote.VoteDataHandler;
import net.dev_talk.votebot.util.MessageUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.Checks;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class VoteHandler extends ListenerAdapter {
    private final MessageUtil messageUtil;
    private final VoteDataHandler dataHandler;
    private final ExecutorService executorService;
    private final String voteChannelName;
    private final String silentIdentifier;

    //emojis as unicode
    private final String voteYesEmoji;
    private final String voteNoEmoji;
    private final String deleteVoteEmoji;
    private final String evaluateVoteEmoji;

    public VoteHandler(final VoteBot bot, final MessageUtil messageUtil, final VoteDataHandler dataHandler,
                       final String voteChannelName, final String silentIdentifier, final String voteYesEmoji,
                       final String voteNoEmoji, final String evaluateVoteEmoji, final String deleteVoteEmoji) {
        this.voteChannelName = voteChannelName;
        this.silentIdentifier = silentIdentifier;
        this.messageUtil = messageUtil;
        this.dataHandler = dataHandler;
        executorService = bot.getExecutorService();

        final Emoji parsedVoteYesEmoji = EmojiManager.getForAlias(voteYesEmoji);
        this.voteYesEmoji = parsedVoteYesEmoji == null ? null : parsedVoteYesEmoji.getUnicode();

        final Emoji parsedVoteNoEmoji = EmojiManager.getForAlias(voteNoEmoji);
        this.voteNoEmoji = parsedVoteNoEmoji == null ? null : parsedVoteNoEmoji.getUnicode();

        final Emoji parsedEvaluateVoteEmoji = EmojiManager.getForAlias(evaluateVoteEmoji);
        this.evaluateVoteEmoji = parsedEvaluateVoteEmoji == null ? null : parsedEvaluateVoteEmoji.getUnicode();

        final Emoji parsedDeleteVoteEmoji = EmojiManager.getForAlias(deleteVoteEmoji);
        this.deleteVoteEmoji = parsedDeleteVoteEmoji == null ? null : parsedDeleteVoteEmoji.getUnicode();

        Checks.notNull(this.voteYesEmoji, "Vote-Yes-Emoji Unicode");
        Checks.notNull(this.voteNoEmoji, "Vote-No-Emoji Unicode");
        Checks.notNull(this.deleteVoteEmoji, "Delete-Vote-Emoji Unicode");
        Checks.notNull(this.evaluateVoteEmoji, "Evaluate-Vote-Emoji Unicode");
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getChannel().getName().equals(voteChannelName)) {
            executorService.execute(() -> handleVoteCreate(event.getMessage()));
        }
    }

    @Override
    public void onGuildMessageReactionAdd(final GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        final long messageId = event.getMessageIdLong();
        executorService.execute(() -> dataHandler.getVote(messageId).ifPresent(vote -> {
            event.getReaction().removeReaction(event.getUser()).queue(null, ignored -> {});

            final String name = event.getReaction().getReactionEmote().getName();
            if (name.equals(voteYesEmoji) || name.equals(voteNoEmoji)) {
                handleVoteAdd(event, vote);
            } else if (name.equals(evaluateVoteEmoji)) {
                if (vote.getAuthorId() != event.getUser().getIdLong()) {
                    return; //only the author is permitted to evaluate the vote
                }
                handleVoteEvaluate(event, vote);
            } else if (name.equals(deleteVoteEmoji)) {
                if (vote.getAuthorId() != event.getUser().getIdLong()) {
                    return; //only the author is permitted to delete the vote
                }
                handleVoteDelete(event, vote);
            }
        }));
    }

    @Override
    public void onGuildMessageDelete(final GuildMessageDeleteEvent event) {
        final long messageId = event.getMessageIdLong();
        executorService.execute(() -> dataHandler.getVote(messageId).ifPresent(vote -> handleVoteDelete(event, vote)));
    }

    protected void handleVoteCreate(final Message message) {
        final String content = message.getContentRaw();
        final String[] split = content.split(" ");
        message.delete().queue();
        final MessageChannel channel = message.getChannel();
        final EmbedBuilder embedBuilder = messageUtil.getEmbedBuilder();
        if (split.length < 2) {
            embedBuilder.appendDescription(messageUtil.getMessageOrKey("vote-invalid-usage"));
            channel.sendMessage(embedBuilder.build()).complete();
            return;
        }

        final User author = message.getAuthor();
        embedBuilder.setAuthor(author.getName(), null, author.getEffectiveAvatarUrl());
        final boolean silent = split.length > 2 && split[0].equals(silentIdentifier);
        final String voteMessage = String.join(" ", Arrays.copyOfRange(split, silent ? 2 : 1, split.length));
        embedBuilder.addField(messageUtil.getMessageOrKey("vote-field-title-name", silent ? split[1] : split[0]),
                messageUtil.getMessageOrKey("vote-field-title-value", voteMessage), false);
        embedBuilder.setTimestamp(Instant.now());
        if (!silent) {
            embedBuilder.addField(messageUtil.getMessageOrKey("vote-field-pro-name"),
                    messageUtil.getMessageOrKey("vote-field-pro-value", 0), true);
            embedBuilder.addField(messageUtil.getMessageOrKey("vote-field-con-name"),
                    messageUtil.getMessageOrKey("vote-field-con-value", 0), true);
        }
        final Message sentMessage = channel.sendMessage(embedBuilder.build()).complete();
        sentMessage.addReaction(voteYesEmoji).queue();
        sentMessage.addReaction(voteNoEmoji).queue();
        sentMessage.addReaction(evaluateVoteEmoji).queue();
        sentMessage.addReaction(deleteVoteEmoji).queue();
        dataHandler.createVote(sentMessage.getIdLong(), author.getIdLong(), silent);
    }

    protected void handleVoteAdd(final GuildMessageReactionAddEvent event, final Vote vote) {
        if (vote.isEvaluated()) {
            return;
        }
        final Message message = event.getChannel().getMessageById(event.getMessageIdLong()).complete();
        final long id = event.getUser().getIdLong();
        final boolean result;
        if (event.getReaction().getReactionEmote().getName().equals(voteYesEmoji)) {
            result = vote.voteYes(id);
        } else {
            result = vote.voteNo(id);
        }

        if (result && !vote.isSilent()) {
            final EmbedBuilder embedBuilder = new EmbedBuilder(message.getEmbeds().get(0));
            embedBuilder.getFields().remove(1);
            embedBuilder.getFields().remove(1); //remove the two fields describing the result
            embedBuilder.addField(messageUtil.getMessageOrKey("vote-field-pro-name"),
                    messageUtil.getMessageOrKey("vote-field-pro-value", vote.getYesVoters().size()), true);
            embedBuilder.addField(messageUtil.getMessageOrKey("vote-field-con-name"),
                    messageUtil.getMessageOrKey("vote-field-con-value", vote.getNoVoters().size()), true);
            message.editMessage(embedBuilder.build()).complete();
        }
    }

    protected void handleVoteEvaluate(final GuildMessageReactionAddEvent event, final Vote vote) {
        if (vote.isEvaluated()) {
            return;
        }
        vote.setEvaluated(true);
        final Message message = event.getChannel().getMessageById(event.getMessageIdLong()).complete();
        for (final MessageReaction reaction : message.getReactions()) {
            final String name = reaction.getReactionEmote().getName();
            if (!name.equals(deleteVoteEmoji)) {
                reaction.getUsers().forEach(user -> reaction.removeReaction(user).queue(null, ignored -> {}));
            }
        }

        if (!vote.isSilent()) { //the embed fields are already showing the results
            return;
        }

        final EmbedBuilder embedBuilder = new EmbedBuilder(message.getEmbeds().get(0));
        embedBuilder.addField(messageUtil.getMessageOrKey("vote-field-pro-name"),
                messageUtil.getMessageOrKey("vote-field-pro-value", vote.getYesVoters().size()), true);
        embedBuilder.addField(messageUtil.getMessageOrKey("vote-field-con-name"),
                messageUtil.getMessageOrKey("vote-field-con-value", vote.getNoVoters().size()), true);
        message.editMessage(embedBuilder.build()).complete();
    }

    protected void handleVoteDelete(final GenericGuildMessageEvent event, final Vote vote) {
        final Message message = event.getChannel().getMessageById(event.getMessageIdLong()).complete();
        message.delete().complete();
        dataHandler.deleteVote(vote);
    }
}
