package com.vegazsdev.bobobot;

import com.vegazsdev.bobobot.core.Bot;
import com.vegazsdev.bobobot.core.CommandWithClass;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.XMLs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(TelegramBot.class);

    private Bot bot;
    private ArrayList<Class> commandClasses;

    TelegramBot(Bot bot, ArrayList<Class> commandClasses) {
        this.bot = bot;
        this.commandClasses = commandClasses;
    }

    public TelegramBot(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onUpdateReceived(Update update) {
        new Thread(new Runnable() {
            private TelegramBot tBot;

            Runnable init(TelegramBot tBot) {
                this.tBot = tBot;
                return this;
            }

            @Override
            public void run() {
                if (update.hasMessage() && update.getMessage().getText() != null
                        && !update.getMessage().getText().equals("")
                        && Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "possible_hotkeys"))
                        .indexOf(update.getMessage().getText().charAt(0)) >= 0) {

                    String msg = update.getMessage().getText();
                    int usrId = update.getMessage().getFrom().getId();
                    PrefObj chatPrefs = getPrefs(update);

                    if (chatPrefs == null) {
                        chatPrefs = new PrefObj(0, "strings-en.xml", "!");
                    }

                    if (msg.startsWith(Objects.requireNonNull(chatPrefs.getHotkey()))) {

                        for (CommandWithClass cmds : getActiveCommandsAsCmdObject()) {

                            String adjustCommand = msg.replace(Objects.requireNonNull(chatPrefs.getHotkey()), "");

                            if (adjustCommand.contains(" ")) {
                                adjustCommand = adjustCommand.split(" ")[0];
                            }

                            if (cmds.getAlias().equals(adjustCommand)) {
                                try {
                                    runMethod(cmds.getClazz(), update, tBot, chatPrefs);
                                    LOGGER.info(Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "command_ok"))
                                            .replace("%1", String.valueOf(usrId))
                                            .replace("%2", adjustCommand));
                                } catch (Exception e) {
                                    LOGGER.error(Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "command_failure"))
                                            .replace("%1", cmds.getAlias())
                                            .replace("%2", e.getMessage()), e);
                                }
                            }
                        }
                    }
                }
            }
        }.init(this)).start();
    }

    public int sendMessage(String msg, Update update) {
        SendMessage sndmsg = new SendMessage().setText(msg).setChatId(update.getMessage().getChatId())
                .enableMarkdown(true)
                .disableWebPagePreview();
        try {
            return execute(sndmsg).getMessageId();
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }


    public int sendReply(String msg, Update update) {
        SendMessage sndmsg = new SendMessage().setText(msg).setChatId(update.getMessage().getChatId())
                .enableMarkdown(true)
                .setReplyToMessageId(update.getMessage().getMessageId())
                .disableWebPagePreview();
        try {
            return execute(sndmsg).getMessageId();
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    public int sendMessage2ID(String msg, long id) {
        SendMessage sndmsg = new SendMessage().setText(msg).setChatId(id)
                .enableMarkdown(true)
                .disableWebPagePreview();
        try {
            return execute(sndmsg).getMessageId();
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    public void editMessage(String msg, Update update, int id) {
        EditMessageText editMessageText = new EditMessageText().setText(msg)
                .setChatId(update.getMessage().getChatId())
                .setMessageId(id)
                .enableMarkdown(true);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            // ignoring errors on edit, keep caution
            //LOGGER.error(e.getMessage(), e);
        }
    }

    public boolean isUserAdminOrPV(Update update) {
        String id1 = update.getMessage().getFrom().getId().toString();
        String id2 = update.getMessage().getChat().getId().toString();
        if (id1.equals(id2)) {
            // private chat
            return true;
        } else {
            try {
                GetChatMember z = new GetChatMember();
                z.setChatId(update.getMessage().getChatId());
                z.setUserId(update.getMessage().getFrom().getId());
                ChatMember cx = execute(z);
                switch (cx.getStatus()) {
                    case "administrator":
                    case "creator":
                        return true;
                    default:
                        return false;
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }
    }

    private void runMethod(Class aClass, Update update, TelegramBot tBot, PrefObj prefs) {
        try {
            Object instance = ((Class<?>) aClass).getDeclaredConstructor().newInstance();
            Method method = ((Class<?>) aClass).getDeclaredMethod("botReply", Update.class, TelegramBot.class, PrefObj.class);
            method.invoke(instance, update, tBot, prefs);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public ArrayList<CommandWithClass> getActiveCommandsAsCmdObject() {
        ArrayList<CommandWithClass> allCommandsArObj = new ArrayList<>();
        for (Class clazz : commandClasses) {
            try {
                Object instance = ((Class<?>) clazz).getDeclaredConstructor().newInstance();
                Method methodAli = ((Class<?>) clazz).getSuperclass().getDeclaredMethod("getAlias");
                Method methodInf = ((Class<?>) clazz).getSuperclass().getDeclaredMethod("getCommandInfo");
                String alias = (String) methodAli.invoke(instance);
                String desc = (String) methodInf.invoke(instance);
                CommandWithClass c = new CommandWithClass(clazz, alias, desc);
                allCommandsArObj.add(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return allCommandsArObj;
    }

    private PrefObj getPrefs(Update update) {
        long chatId = update.getMessage().getChatId();
        PrefObj prefObj = DbThings.selectIntoPrefsTable(chatId);
        if (prefObj == null) {
            DbThings.insertIntoPrefsTable(chatId);
        }
        return prefObj;
    }

    @Override
    public String getBotUsername() {
        return bot.getUsername();
    }

    @Override
    public String getBotToken() {
        return bot.getToken();
    }

}
