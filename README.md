# Bo³+t Bot

Just a other Telegram Bot made using [TelegramBots](https://github.com/rubenlagus/TelegramBots) Java Library

### What are the Current Supported Commands?

Default Commands:

 - ChangeHotkey: Changes hotkey on current chat (!chkey)
 - ChangeLang: Change language on current chat (!chlang)
 - ListAllCommands: Show all commands avaiable (!comm)
 - About: Shows a About info (!about)
 
Other Commands:

 - Hello: Says a Hello! (!hello)
 - Chat2Shell: Run shell commands on host computer via chat (!shell)
 - SetupGDrive: Setup your Google Drive Account to send files (!setupgdrive)
 - Download2GDrive: Download any directlink and send to your Google Drive (!d2gdrive)
 - ErfanGSIs: Can port GSIs using [ErfanGSIs Tool](https://github.com/erfanoabdi/ErfanGSIs) (!jurl2gsi)
 
*More commands will be added in future, any suggestion or contributions are welcome :)*

### How to setup this bot?

Ill always offer a jar file of this bot on GitHub Releases, so, if you want to use this, without any changes, just using your bot "credentials" and other things, yeap, you can!

1. Get this bot (You can download the on Releases tab or Build it by yourself)

2. Run jar file by
```
java -jar Bobobot.jar
```

_First time you will get a error, because your bot token and bot username are not set_

3. Open config.prop (inside configs folder), and fit with your information, like that:

```
#BoboBot config file
#Wed Oct 30 15:17:09 BRT 2019
bot-token=Put your Telegram Bot Token here
bot-username=Put your Telegram Bot Username here
bot-master=You are the master of this bot, put your Telegram ID here
```

4. Run again and voila!

### Some good information

**! is ALWAYS the DEFAULT HOTKEY** (You can change this hotkey using !chkey)

**EN is ALWAYS the DEFAULT LANGUAGE** (You can change this language using !chlang)

To use anything that send files to Google Drive (like !jurl2gsi or !d2gdrive), YOU NEED TO SETUP YOUR ACCOUNT USING !setupgdrive

Strings by now are not "fully translatable", ill try to do something good in future

### How to build?

I Wrote this bot using IntelliJ IDEA, so, just git clone that repo and open this project on IDEA, and well, edit haha, good luck :)

Also, the META-INF folder is included on root of this project (may be useful while you creating artefact)

### How to create a new command?

This bot tries to be most modular as possible, so, we use Reflection on Main class to "track" for all classes inside Command package, so, open Hello.java, see how it works and make a other class (inside Commands package), extends Command and start your new command :)

### About

This is just mine "personal" bot for telegram haha

### Credits and Libraries

[TelegramBots (by rubenlagus)](https://github.com/rubenlagus/TelegramBots)

[Apache Commons](https://commons.apache.org/)

[Apache Log4j 2 (API/Core)](https://logging.apache.org/log4j/)

[Google API Client](https://developers.google.com/api-client-library)

[Google OAuth Client](https://developers.google.com/api-client-library/java/google-oauth-java-client)

[Google Drive API](https://developers.google.com/drive)

[Google Guava](https://github.com/google/guava)

[SQLite JDBC Driver (by xerial)](https://github.com/xerial/sqlite-jdbc)

### Also:

[Java Quickstart](https://developers.google.com/drive/api/v3/quickstart/java)

[Manipulating files.. on Google Drive using Java by o7planning](https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java)
