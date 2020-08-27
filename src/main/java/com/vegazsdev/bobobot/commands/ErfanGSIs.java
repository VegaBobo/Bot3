package com.vegazsdev.bobobot.commands;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.GDrive;
import com.vegazsdev.bobobot.utils.JSONs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ErfanGSIs extends Command {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(ErfanGSIs.class);

    private static boolean isPorting = false;
    private static ArrayList<GSICmdObj> queue = new ArrayList<>();
    private final String toolPath = "ErfanGSIs/";
    private File[] supportedGSIs9 = new File(toolPath + "roms/9").listFiles(File::isDirectory);
    private File[] supportedGSIs10 = new File(toolPath + "roms/10").listFiles(File::isDirectory);
    private String infoGSI = "";

    public ErfanGSIs() {
        super("jurl2gsi", "Can port gsi");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {

        String msg = update.getMessage().getText();
        String idAsString = update.getMessage().getFrom().getId().toString();

        if (msg.contains(" allowuser") && Objects.equals(Config.getDefConfig("bot-master"), idAsString)) {
            if (update.getMessage().getReplyToMessage() != null) {
                String userid = update.getMessage().getReplyToMessage().getFrom().getId().toString();
                if (addPortPerm(userid)) {
                    bot.sendReply(prefs.getString("egsi_allowed").replace("%1", userid), update);
                }
            } else if (msg.contains(" ")) {
                String userid = msg.split(" ")[2];
                if (userid != null && userid.trim().equals("") && addPortPerm(userid)) {
                    bot.sendReply(prefs.getString("egsi_allowed").replace("%1", userid), update);
                }
            } else {
                bot.sendReply(prefs.getString("egsi_allow_by_reply").replace("%1", prefs.getHotkey())
                        .replace("%2", this.getAlias()), update);
            }
        } else if (msg.contains(" queue")) {
            if (!queue.isEmpty()) {
                StringBuilder v = new StringBuilder();
                for (int i = 0; i < queue.size(); i++) {
                    v.append("#").append(i + 1).append(": ").append(queue.get(i).getGsi()).append("\n");
                }
                bot.sendReply(prefs.getString("egsi_current_queue")
                        .replace("%2", v.toString())
                        .replace("%1", String.valueOf(queue.size())), update);
            } else {
                bot.sendReply(prefs.getString("egsi_no_ports_queue"), update);
            }
        } else if (msg.contains(" cancel")) {

            // cancel by now, maybe work, not tested
            // will exit only on when porting is "active" (when url2gsi.sh is running)
            // after that when port already already finished (eg. uploading, zipping)
            // so this cancel needs more things to fully work

            ProcessBuilder pb;
            pb = new ProcessBuilder("/bin/bash", "-c", "kill -TERM -- -$(ps ax | grep url2GSI.sh | grep -v grep | awk '{print $1;}')");
            try {
                pb.start();
            } catch (IOException ex) {
                //Logger.getLogger(BotTelegram.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            boolean userHasPermissions = userHasPortPermissions(idAsString);

            if (userHasPermissions) {
                GSICmdObj gsiCommand = isCommandValid(update);
                if (gsiCommand != null) {
                    boolean isGSITypeValid = isGSIValid(gsiCommand.getGsi());
                    if (isGSITypeValid) {
                        if (!isPorting) {
                            isPorting = true;
                            createGSI(gsiCommand, bot);
                            while (queue.size() != 0) {
                                GSICmdObj portNow = queue.get(0);
                                queue.remove(0);
                                createGSI(portNow, bot);
                            }
                            isPorting = false;
                        } else {
                            queue.add(gsiCommand);
                            bot.sendReply(prefs.getString("egsi_added_to_queue"), update);
                        }
                    } else {
                        bot.sendReply(prefs.getString("egsi_supported_types")
                                .replace("%1",
                                        Arrays.toString(supportedGSIs9).replace(toolPath + "roms/9/", "")
                                                .replace("[", "")
                                                .replace("]", ""))
                                .replace("%2",
                                        Arrays.toString(supportedGSIs10).replace(toolPath + "roms/10/", "")
                                                .replace("[", "")
                                                .replace("]", "")), update);
                    }
                }


            } else {
                // no perm
                bot.sendReply("No Permissions", update);
            }

        }
    }


    private String try2AvoidCodeInjection(String parameters) {
        try {
            // should be regex.
            parameters = parameters.replace("&", "")
                    .replace("\\", "").replace(";", "").replace("<", "")
                    .replace(">", "").replace("|", "");
        } catch (Exception e) {
            return parameters;
        }
        return parameters;
    }

    private GSICmdObj isCommandValid(Update update) {
        GSICmdObj gsiCmdObj = new GSICmdObj();
        String msg = update.getMessage().getText().replace(Config.getDefConfig("bot-hotkey") + this.getAlias() + " ", "");
        String url;
        String gsi;
        String param;
        try {
            url = msg.split(" ")[1];
            gsiCmdObj.setUrl(url);
            gsi = msg.split(" ")[2];
            gsiCmdObj.setGsi(gsi);
            param = msg.replace(url + " ", "").replace(gsi, "").trim();
            param = try2AvoidCodeInjection(param);
            gsiCmdObj.setParam(param);
            gsiCmdObj.setUpdate(update);
            return gsiCmdObj;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private boolean isGSIValid(String gsi) {
        File[] supportedGSIs = ArrayUtils.addAll(supportedGSIs9, supportedGSIs10);
        try {
            String gsi2 = null;
            if (gsi.contains(":")) {
                gsi2 = gsi.split(":")[0];
            }
            for (File supportedGSI : supportedGSIs) {
                if (gsi2 != null) {
                    if (gsi2.equals(supportedGSI.getName())) {
                        return true;
                    }
                } else {
                    if (gsi.equals(supportedGSI.getName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        return false;
    }

    private boolean userHasPortPermissions(String idAsString) {
        if (Objects.equals(Config.getDefConfig("bot-master"), idAsString)) {
            return true;
        }
        String portConfigFile = "configs/allowed2port.json";
        return Arrays.asList(Objects.requireNonNull(JSONs.getArrayFromJSON(portConfigFile)).toArray()).contains(idAsString);
    }

    private void createGSI(GSICmdObj gsiCmdObj, TelegramBot bot) {
        Update update = gsiCmdObj.getUpdate();
        ProcessBuilder pb;
        pb = new ProcessBuilder("/bin/bash", "-c",
                "cd " + toolPath + " ; ./url2GSI.sh '" + gsiCmdObj.getUrl() + "' " + gsiCmdObj.getGsi() + " " + gsiCmdObj.getParam()
        );
        boolean success = false;
        StringBuilder fullLogs = new StringBuilder();
        fullLogs.append("Starting process!");
        int id = bot.sendReply(fullLogs.toString(), update);
        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean weDontNeedAria2Logs = true;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                line = "`" + line + "`";
                if (line.contains("Downloading firmware to:")) {
                    weDontNeedAria2Logs = false;
                    fullLogs.append("\n").append(line);
                    bot.editMessage(fullLogs.toString(), update, id);
                }
                if (line.contains("Create Temp and out dir")) {
                    weDontNeedAria2Logs = true;
                }
                if (weDontNeedAria2Logs) {
                    fullLogs.append("\n").append(line);
                    bot.editMessage(fullLogs.toString(), update, id);
                    if (line.contains("GSI done on:")) {
                        success = true;
                    }
                }
            }

            if (success) {

                // gzip files!

                fullLogs.append("\n").append("Creating gzip...");
                bot.editMessage(fullLogs.toString(), update, id);

                String[] gzipFiles = listFilesForFolder(new File("ErfanGSIs" + "/output"));
                for (String gzipFile : gzipFiles) {
                    new FileTools().gzipFile(gzipFile, gzipFile + ".gz");
                }

                // send to google drive

                ArrayList<String> arr = new ArrayList<>();

                AtomicReference<String> aonly = new AtomicReference<>("");
                AtomicReference<String> ab= new AtomicReference<>("");

                try (Stream<Path> paths = Files.walk(Paths.get("ErfanGSIs/output/"))) {
                    paths
                            .filter(Files::isRegularFile)
                            .forEach(a -> {
                                if (a.toString().endsWith(".img.gz")) {
                                    arr.add(a.toString());
                                    if(a.toString().contains("Aonly")){
                                        aonly.set(FilenameUtils.getBaseName(a.toString()) + "." + FilenameUtils.getExtension(a.toString()));
                                    }else{
                                        ab.set(FilenameUtils.getBaseName(a.toString()) + "." + FilenameUtils.getExtension(a.toString()));
                                    }
                                }
                                if (a.toString().contains(".txt")) {
                                    infoGSI = a.toString();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // arr == full path

                fullLogs.append("\n").append("Sending files to SF...");
                bot.editMessage(fullLogs.toString(), update, id);

                String re = new sfUpload().uploadGsi(arr, gsiCmdObj.getGsi());
                re=re+"/";


//                GDriveGSI links = new GSIUpload().enviarGSI(gsiCmdObj.getGsi(), arr);
//
//                if (gsiCmdObj.getGsi().contains(":")) {
//                    gsiCmdObj.setGsi(gsiCmdObj.getGsi().split(":")[1]);
//                }

                StringBuilder generateLinks = new StringBuilder();

                if (!aonly.toString().trim().equals("")) {
                    generateLinks.append("\n*Download A-Only:* [SourceForge](https://sourceforge.net/projects/").append(sfsetup.getSfConf("bot-sf-proj")).append("/files/").append(re).append(aonly.toString()).append(")");
                }
                if (!ab.toString().trim().equals("")) {
                    generateLinks.append("\n*Download AB:* [SourceForge](https://sourceforge.net/projects/").append(sfsetup.getSfConf("bot-sf-proj")).append("/files/").append(re).append(ab.toString()).append(")");
                }

                generateLinks.append("\n*Folder:* [SourceForge](https://sourceforge.net/projects/").append(sfsetup.getSfConf("bot-sf-proj")).append("/files/").append(re).append(")");

                String descGSI = "" + new FileTools().readFile(infoGSI).trim();

                bot.sendReply("Job Finished", update);

                try {
                    if (sfsetup.getSfConf("bot-send-announcement").equals("true")) {
                        try {
                            bot.sendMessage2ID("*GSI: " + gsiCmdObj.getGsi() + "*\n\n"
                                    + "*Firmware Base: *" + "[URL](" + gsiCmdObj.getUrl() + ")"
                                    + "\n\n*Information:*\n`" + descGSI
                                    + "`\n" + generateLinks.toString()
                                    + "\n\nFile not found? wait some minutes\nSlow downloads? try a mirror :)"
                                    + "\n\n*Thanks to:* [Contributors List](https://github.com/erfanoabdi/ErfanGSIs/graphs/contributors)"
                                    + "\n\n[Ported using ErfanGSIs Tool](https://github.com/erfanoabdi/ErfanGSIs)", Long.parseLong(sfsetup.getSfConf("bot-announcement-id")));
                        }catch (Exception e){
                            LOGGER.error("bot-announcement-id looks wrong or not set");
                        }
                    }
                }catch (Exception e){
                    LOGGER.warn("bot-send-announcement is not set");
                }

                fullLogs.append("\n").append("Finished!");
                bot.editMessage(fullLogs.toString(), update, id);
                FileUtils.deleteDirectory(new File(toolPath + "output"));
            } else {
                throw new Exception("Task finished without generating a valid GSI");
            }
        } catch (Exception ex) {
            LOGGER.error(fullLogs);
        }
    }

    private static String[] listFilesForFolder(final File folder) {
        StringBuilder paths = new StringBuilder();
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".img")) {
                    paths.append(fileEntry.getAbsolutePath()).append("\n");
                }
            }
        }
        return paths.toString().split("\n");
    }

    private boolean addPortPerm(String id) {
        try {
            if (new FileTools().checkFileExistsCurPath("configs/allowed2port.json")) {
                ArrayList z = JSONs.getArrayFromJSON("configs/allowed2port.json");
                if (z != null) {
                    z.add(id);
                }
                JSONs.writeArrayToJSON(z, "configs/allowed2port.json");
            } else {
                ArrayList<String> z = new ArrayList<>();
                z.add(id);
                JSONs.writeArrayToJSON(z, "configs/allowed2port.json");
            }
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

}

class GSIUpload {

    GDriveGSI enviarGSI(String gsi, ArrayList<String> var) {
        String rand = RandomStringUtils.randomAlphabetic(8);
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm z").format(Calendar.getInstance().getTime());
        try {
            String uid = gsi + " GSI " + date + " " + rand;
            GDrive.createGoogleFolder(null, uid);
            List<com.google.api.services.drive.model.File> googleRootFolders = GDrive.getGoogleRootFolders();
            String folderId = "";
            for (com.google.api.services.drive.model.File folder : googleRootFolders) {
                if (folder.getName().equals(uid)) {
                    folderId = folder.getId();
                    //System.out.println("Folder ID: " + folder.getId() + " --- Name: " + folder.getName());
                }
            }
            for (String sendFile : var) {
                String fileTrim = sendFile.split("output/")[1];
                File uploadFile = new File(sendFile);
                GDrive.createGoogleFile(folderId, "application/gzip", fileTrim, uploadFile);
            }
            String aonly = "";
            String ab = "";
            List<com.google.api.services.drive.model.File> arquivosNaPasta = GDrive.showFiles(folderId);
            for (com.google.api.services.drive.model.File f : arquivosNaPasta) {
                if (!f.getName().contains(".txt")) {
                    if (f.getName().contains("Aonly")) {
                        aonly = f.getId();
                    } else if (f.getName().contains("AB")) {
                        ab = f.getId();
                    }
                }
            }
            GDriveGSI links = new GDriveGSI();
            if (ab != null && !ab.trim().equals("")) {
                links.setAb(ab);
            }
            if (aonly != null && !aonly.trim().equals("")) {
                links.setA(aonly);
            }
            links.setFolder(folderId);
            GDrive.createPublicPermission(folderId);
            return links;
        } catch (Exception e) {
            return null;
        }
    }

}

class GSICmdObj {

    private String url;
    private String gsi;
    private String param;
    private Update update;

    GSICmdObj() {
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    String getGsi() {
        return gsi;
    }

    void setGsi(String gsi) {
        this.gsi = gsi;
    }

    String getParam() {
        return param;
    }

    void setParam(String param) {
        this.param = param;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }
}

class GDriveGSI {

    private String ab;
    private String a;
    private String folder;

    GDriveGSI() {
    }

    String getAb() {
        return ab;
    }

    void setAb(String ab) {
        this.ab = ab;
    }

    String getA() {
        return a;
    }

    void setA(String a) {
        this.a = a;
    }

    String getFolder() {
        return folder;
    }

    void setFolder(String folder) {
        this.folder = folder;
    }
}

class sfUpload {

    String user;
    String host;
    String pass;
    String proj;

    sfUpload(){
        this.user = sfsetup.getSfConf("bot-sf-user");
        this.host =  sfsetup.getSfConf("bot-sf-host");
        this.pass =  sfsetup.getSfConf("bot-sf-pass");
        this.proj =  sfsetup.getSfConf("bot-sf-proj");
    }

    public String uploadGsi(ArrayList<String> aar, String name){

        if(name.contains(":")){
            // should be better to regex any special char
            name=name.replace(":", " - ");
        }

        name=name + " - " + RandomStringUtils.randomAlphanumeric(10).toUpperCase();
        String path = "/home/frs/project/" + proj + "/"+name;

        try{
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pass);
            session.connect();
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.mkdir(path);
            for (int i = 0; i < aar.size(); i++) {
                if(!aar.get(i).endsWith(".img")){
                    sftpChannel.put(aar.get(i), path);
                }// dont send .img files, they should be compressed//
            }
            return name;
        } catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

}