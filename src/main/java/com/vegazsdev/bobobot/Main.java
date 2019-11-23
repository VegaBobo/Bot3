package com.vegazsdev.bobobot;

import com.google.common.reflect.ClassPath;
import com.vegazsdev.bobobot.core.Bot;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.utils.Config;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.XMLs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

public class Main {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(Main.class);

    public static String DEF_CORE_STRINGS_XML = "core-strings.xml";

    public static void main(String[] args) {

        LOGGER.info(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "bot_init"));

        // detect config file

        if (!new FileTools().checkFileExistsCurPath("configs/" + XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file"))) {
            LOGGER.info(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file_not_found"));
            new Config().createDefConfig();
            LOGGER.warn(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file_info"));
            System.exit(0);
        }

        // initialize all commands inside commands package

        ArrayList<Class> commandClasses = new ArrayList<>();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith("com.vegazsdev.bobobot.commands")) {
                    final Class<?> clazz = info.load();
                    try {
                        Object instance = ((Class<?>) clazz).getDeclaredConstructor().newInstance();
                        Method method = ((Class<?>) clazz).getSuperclass().getDeclaredMethod("getAlias");
                        method.invoke(instance);
                        commandClasses.add(clazz);
                        LOGGER.info(Objects.requireNonNull(
                                XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "cc_init_cmd"))
                                .replace("%1", clazz.getSimpleName()));
                    } catch (Exception e) {
                        // by now, ignoring exceptions here
                        // LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // create a bot object

        if ((Config.getDefConfig("bot-token") != null && Objects.requireNonNull(Config.getDefConfig("bot-token")).contains(" "))
                || (Config.getDefConfig("bot-username") != null && Objects.requireNonNull(Config.getDefConfig("bot-username")).contains(" "))) {
            LOGGER.warn(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file_info"));
            System.exit(0);
        }

        Bot bot =
                new Bot(
                        Config.getDefConfig("bot-token"),
                        Config.getDefConfig("bot-username"));

        // database
        // create a new database if current one doesn't exists

        if (!new FileTools().checkFileExistsCurPath("databases/prefs.db")) {
            DbThings.createNewDatabase("prefs.db");
            DbThings.createTable("prefs.db",
                    "CREATE TABLE IF NOT EXISTS chat_prefs ("
                            + "group_id real UNIQUE PRIMARY KEY,"
                            + "hotkey text DEFAULT '!',"
                            + "lang text DEFAULT 'strings-en.xml'"
                            + ");");
        }

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new TelegramBot(bot, commandClasses));
            LOGGER.info(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "bot_started"));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }

    }

}
