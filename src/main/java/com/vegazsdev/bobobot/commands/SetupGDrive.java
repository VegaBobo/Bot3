package com.vegazsdev.bobobot.commands;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.Command;
import com.vegazsdev.bobobot.core.CustomConfigFileObj;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.GDrive;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class SetupGDrive extends Command {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(SetupGDrive.class);

    private String configFile = "gdrive.config";

    private String validateURL = "https://accounts.google.com/o/oauth2/auth?access_type=offline&" +
            "client_id=$CLIENT_ID$" +
            "&redirect_uri=http://localhost:8888/Callback&response_type=code&scope=https://www.googleapis.com/auth/drive";

    public SetupGDrive() {
        super("setupgdrive", "Tests and Setup Google Drive connection");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {

        // allow configuration only in master's private chat

        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            if (update.getMessage().getText().contains("http") && !update.getMessage().getText().contains("json")) {
                String callback = update.getMessage().getText();
                if (callback.contains(" url http")) {
                    callback = callback.split(" ")[2];
                } else {
                    callback = callback.split(" ")[1];
                }
                try {
                    boolean success = false;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(callback).openStream(), StandardCharsets.UTF_8))) {
                        for (String line; (line = reader.readLine()) != null; ) {
                            if (line.contains(prefs.getString("gdrive_recv_ver_code"))) {
                                success = true;
                            }
                        }
                    }
                    if (success) {
                        bot.sendMessage(prefs.getString("gdrive_setup_complete"), update);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else if (update.getMessage().getText().contains("mykey")) {
                String apikey = update.getMessage().getText().trim().split(" ")[2];
                if (!new FileTools().checkFileExistsCurPath(configFile)) {
                    createConfigFile(apikey, prefs);
                    bot.sendMessage(prefs.getString("gdrive_config_file_make").replace("%1", prefs.getHotkey()), update);
                } else {
                    if (new File(configFile).delete()) {
                        bot.sendMessage(prefs.getString("gdrive_old_conf"), update);
                    }
                }
            } else if (update.getMessage().getText().contains("json")) {
                try {
                    String msg = update.getMessage().getText().trim().split(" ")[2];
                    PrintWriter writer = new PrintWriter(prefs.getString("gdrive_credentials_file"));
                    writer.println(msg);
                    writer.close();
                    bot.sendMessage(prefs.getString("gdrive_credentials_created")
                            .replace("%1", prefs.getHotkey()), update);
                } catch (Exception e) {
                    bot.sendMessage(prefs.getString("something_went_wrong"), update);
                }
            } else {
                switch (update.getMessage().getText().replace(prefs.getHotkey(), "")) {
                    case "setupgdrive curltoken":
                        bot.sendMessage(prefs.getString("gdrive_localcurlinfo").replace("%1", prefs.getHotkey()), update);
                        break;
                    case "setupgdrive":
                        if (FileTools.createFolder("credentials")) {
                            bot.sendMessage(prefs.getString(prefs.getString("gdrive_credentails_folder_created")), update);
                        } else {
                            bot.sendMessage(prefs.getString("gdrive_credentails_folder_error"), update);
                        }
                        bot.sendMessage(prefs.getString("gdrive_how2setup").replace("%1", prefs.getHotkey()), update);
                        break;
                    case "setupgdrive ok":
                        try {
                            InputStream is = new FileInputStream("credentials/credentials.json");
                            String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
                            validateURL = validateURL.replace("$CLIENT_ID$", new JSONObject(jsonTxt).getJSONObject("installed").getString("client_id"));
                            bot.sendMessage(prefs.getString("gdrive_need2allow").replace("%1", validateURL), update);
                            bot.sendMessage(prefs.getString("gdrive_localcurl").replace("%1", prefs.getHotkey()), update);
                            new Thread(() -> {
                                try {
                                    GDrive.getGoogleRootFolders();
                                    bot.sendMessage(prefs.getString("gdrive_setup_complete").replace("%1", prefs.getHotkey()), update);
                                } catch (IOException e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }).start();
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                }
            }
        } else {
            bot.sendMessage(prefs.getString("only_master_can_run"), update);
        }


    }

    private void createConfigFile(String key, PrefObj prefs) {
        ArrayList<CustomConfigFileObj> configs = new ArrayList<>();
        configs.add(new CustomConfigFileObj("client-secret", key));
        Config.createCustomConfig(configs, configFile, prefs.getString("gdrive_setup_info"));
        LOGGER.info(prefs.getString("gdrive_conf_file_created"));
    }

}
