package com.vegazsdev.bobobot.commands;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Hello extends Command {

    public Hello() {
        super("hello", "Says hello!");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        bot.sendMessage(prefs.getString("hello").replace("%1", update.getMessage().getText()), update);
    }

}
