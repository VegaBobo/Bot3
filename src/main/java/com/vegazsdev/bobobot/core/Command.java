package com.vegazsdev.bobobot.core;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class Command {

    private String alias;
    private String commandInfo;

    public Command(String alias, String commandInfo) {
        this.alias = alias;
        this.commandInfo = commandInfo;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCommandInfo() {
        return commandInfo;
    }

    public void setCommandInfo(String commandInfo) {
        this.commandInfo = commandInfo;
    }

    public abstract void botReply(Update update, TelegramBot bot, PrefObj prefs);

}
