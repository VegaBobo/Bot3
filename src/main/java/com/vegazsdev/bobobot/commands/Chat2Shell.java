package com.vegazsdev.bobobot.commands;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class Chat2Shell extends Command {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(Chat2Shell.class);

    public Chat2Shell() {
        super("shell", "Run shell (bash) commands via chat");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            String msg = update.getMessage().getText().substring(7);
            try {
                StringBuilder text = new StringBuilder();
                ProcessBuilder pb;
                pb = new ProcessBuilder("/bin/bash", "-c", msg);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line).append("\n");
                }
                bot.sendMessage("`" + text.toString() + "`", update);
            } catch (Exception e) {
                bot.sendMessage(prefs.getString("something_went_wrong"), update);
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
