package com.vegazsdev.bobobot.db;

import com.vegazsdev.bobobot.Main;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.XMLs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.sql.*;
import java.util.Objects;

public class DbThings {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(DbThings.class);

    // Generic Methods

    public static void createNewDatabase(String database) {
        if (FileTools.checkIfFolderExists("databases")) {
            FileTools.createFolder("databases");
        }
        try (Connection conn = connect(database)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                LOGGER.info(Objects.requireNonNull(
                        XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "sql_driver_info"))
                        .replace("%1", meta.getDriverName()));
                LOGGER.info(Objects.requireNonNull(
                        XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "sql_db_ok"))
                        .replace("%1", database));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static Connection connect(String database) {
        String url = "jdbc:sqlite:databases/" + database;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return conn;
    }

    public static void createTable(String database, String query) {
        try (Connection conn = connect(database);
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    // specific prefs.db methods

    public static void insertIntoPrefsTable(double id) {
        String sql = "INSERT INTO chat_prefs(group_id) VALUES(?)";
        try (Connection conn = connect("prefs.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static PrefObj selectIntoPrefsTable(double id) {
        String sql = "SELECT group_id, lang, hotkey FROM chat_prefs WHERE group_id = " + id;
        PrefObj prefObj = null;
        try (Connection conn = connect("prefs.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                prefObj = new PrefObj(rs.getDouble("group_id"), rs.getString("lang"), rs.getString("hotkey"));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return prefObj;
    }

    public static void changeLanguage(double groupid, String newlang) {
        String sql = "UPDATE chat_prefs SET lang = '" + newlang + "' WHERE group_id = " + groupid;
        try (Connection conn = connect("prefs.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void changeHotkey(double groupid, String newhotkey) {
        String sql = "UPDATE chat_prefs SET hotkey = '" + newhotkey + "' WHERE group_id = " + groupid;
        try (Connection conn = connect("prefs.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
