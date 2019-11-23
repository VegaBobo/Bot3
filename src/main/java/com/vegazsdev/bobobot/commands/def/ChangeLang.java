package com.vegazsdev.bobobot.commands.def;

import com.vegazsdev.bobobot.Main;
import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.Command;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.XMLs;
import org.telegram.telegrambots.meta.api.objects.Update;


public class ChangeLang extends Command {

    public ChangeLang() {
        super("chlang", "Change current language on this chat");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {

        if (bot.isUserAdminOrPV(update)) {
            if (update.getMessage().getText().equals(prefs.getHotkey() + "chlang".trim())) {
                bot.sendMessage("Available languages:\n" + XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "disp_lang")
                        + "\nTo change language, use: !chlang langcode\neg: !chlang br", update);
            } else {
                String msg = update.getMessage().getText().split(" ")[1].trim();
                if (msg.contains(" ")) {
                    msg = msg.replace(" ", "");
                }
                if (msg.length() < 3) {
                    String hello = XMLs.getFromStringsXML("strings-" + msg + ".xml", "hello");
                    if (hello == null) {
                        bot.sendMessage("Language not avaiable, type !chlang to see avaiable languages", update);
                    } else {
                        DbThings.changeLanguage(prefs.getId(), "strings-" + msg + ".xml");
                        prefs = DbThings.selectIntoPrefsTable(prefs.getId());
                        bot.sendMessage(prefs.getString("lang_updated"), update);
                    }
                } else {
                    bot.sendMessage("Language not avaiable, type !chlang to see avaiable languages", update);
                }
            }
        } else {
            bot.sendMessage("Only admins can run this command", update);
        }

    }
}
