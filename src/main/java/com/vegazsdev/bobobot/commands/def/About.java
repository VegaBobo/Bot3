package com.vegazsdev.bobobot.commands.def;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

public class About extends Command {

    public About() {
        super("about", "About infomation");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        bot.sendMessage(
                "*bo³+t Bot* for Telegram by @VegaZS\n" +
                        "Written in Java using TelegramBots and many other libraries :)\n\n" +
                        "[TelegramBots (by rubenlagus)](https://github.com/rubenlagus/TelegramBots)\n" +
                        "[Apache Commons](https://commons.apache.org/)\n" +
                        "[Apache Log4j 2 (API/Core)](https://logging.apache.org/log4j/)\n" +
                        "[Google API Client](https://developers.google.com/api-client-library)\n" +
                        "[Google OAuth Client](https://developers.google.com/api-client-library/java/google-oauth-java-client)\n" +
                        "[Google Drive API](https://developers.google.com/drive)\n" +
                        "[Google Guava](https://github.com/google/guava)\n" +
                        "[SQLite JDBC Driver (by xerial)](https://github.com/xerial/sqlite-jdbc)" +
                        "", update);
    }
}
