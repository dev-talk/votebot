package net.dev_talk.votebot.data.settings;

/**
 * @author LGA1151 (https://github.com/LGA1151)
 */
public class Settings {
    private Discord discord;

    public Discord getDiscordSettings() {
        return discord;
    }

    public class Discord {
        private String token;
        private String activity;
        private String activityType;
        private String embedColor;

        public String getToken() {
            return token;
        }

        public String getActivity() {
            return activity;
        }

        public String getActivityType() {
            return activityType;
        }

        public String getEmbedColor() {
            return embedColor;
        }
    }
}
