

# License
This program and its files, its codes, implementation and functions are under the GNU AFFERO GENERAL PUBLIC LICENSE Version 3.

# DexCraft Launcher Client
This repository contains the DexCraft Launcher main program, which I call DexCraft Launcher Client, or just Launcher for short.
This program logs the user in, check their configuration and downloads the minecraft clients and stuff.

## Project purposes
All the thing started back on 2018 when I managed my own Minecraft Servers. At first I've only had one, and it was pretty difficult to me since on that time I just didn't have any ideas about how Minecraft (mostly the servers) worked.
My greatest dilema was everytime when I needed to change something (mostly .cfg files from some mod), and the changes needed to be done on client-side too in order to make effect. Well, everytime I needed to adjust something, I've had to send the file for each player, and help them to install the file properly, in the correct folder etc.
It was an exaustive journey, with files missing, edited by some naughty players sometimes, or corrupting other files or mods by accident. So I just needed to do an alternative.
Back then, at the last months of 2018, I started to do my most important project, where I learn so much, and keep learning until now.

## Initial purpose
DexCraft Launcher (and all its subprograms, like the Client) began with some simple ideas:

**1.** Create an easy, automated, practical way to provide updates (since client-side .cfg edits to JRE updates)
**2.** Securely save players' Journeymap Mod's maps and configuration in order they can have it later (in new installations or other computers)
**3.** Easily install Minecraft clients, as simple as possible, in an interactive way of some sort.


## Versioning notes
It's important to say that the versioning was all wrong at the beginning since my inexperience in the matter. Plus, the code initially was so mixed up with my servers credentials that would be dangerous for me publish this project, and at that time, I couldn't make a private repo.
 

##  Current implementation
Nowadays, the Launcher is bigger, better and much more funcional as it was in the first intent. Now, DexCraft Launcher has much more funcionalities:

**1.** UI with progress graphs, splash screens, percentage, menus and options, wallpaper background and sound (in progress), trying to be clean and cool up to the most;
**2.** Login screen, which allows the player "create an account" for syncronizing data which isn't normally synced in Minecraft (JVM arguments, game options, graphic options, textures, render distance, FOV, keybinds etc), besides, allow the player to save their singleplayer worlds and having it everywhere;
**3.** Allows the player easily installs soundpacks, textures and fancy extras to the game, in one click;
**4.** Restore the Client if something just went wrong all of sudden.
 
## DexCraft Launcher's main applications
DexCraft Launcher is an application composed of 3 programs as it follows:
 
**1. Initializer (Init)** -  The first application to run. This application is the one which is called after installation (before made manually using a zip file and a batch file, and now via an exe application), and also its the application which runs on the Launcher shortcuts. To the player, Init is just a splash screen, but this program has the following main tasks:
 ---------- Check if the System has the minimum requirements to run any Minecraft games from server at least at low profile configuration;
 ---------- Check if the System has internet connection to run the games and provides the option to play offline (singleplayer, without the multiplayer options);
 ---------- Check and update the Launcher version installed (or install if it isn't present);
 ---------- Run the Launcher.
 **edit: Since DexCraft Launcher Init v2.1.0-201128-555 Init checks the internet speed to assure the player will sync data properly.**
 **2. Launcher Client (DCL)** -  The second application to run, DexCraft Launcher Client, "Launcher", or just DCL. This program has the biggest window, with the major funcionalities. It's from Launcher the player logs-in, prepare their configuration to sync, do backups or restores of it, installs extras, textures and soundpacks, apply JVM presets... all of it from a simple menu on the upper bar on the window. Internally, the Launcher also does:
 ---------- Check if the System is able to syncronize data, or it will just work offline;
 **edit: Since DexCraft Launcher Init v2.1.0-201128-555 this task now belongs to the Init.**
 ---------- Check and update the Background Services to work properly;
 ---------- Update JRE version installed on System;
 ---------- Transmit the game profile data to the internal launcher in order to run Minecraft;
 ---------- Run the Background Services.
 **3. Background Services (DCBS)** -  The third and last application to run, DexCraft Background Services, or just DCBS. This humble program will be next to the windows clock, on notification bar, in a form of a fancy icon. It will sync your data periodically as you play, assuring you have your journeymap's map updated and can be used in another machine if you properly logs in.
 ---------- Verify if the player logged in DexCraft Launcher properly in order to sync;
 ---------- Verify if you are currently playing the game and syncs it at specific minutes;

## DexCraft Launcher's points of attention
There's a lot to do in this project, and here I'm gonna point up some things are needed to do:

 - Javadoc. I did some archaic documentation on previous versions (not published ones) of the code, but I need to do a concise, balanced and effective documentation.
 - UML models. I want do to an UML model of the software, and thats a "must" on my future plans.
 - Customized progress bar. I'm currenly using a good progress bar, but I want a better one, and I want to do it by myself.
 - Use a better, more secure way of login and transmitting passwords (both players' and servers'). A database, crypto, just don't know. I made an archaic, simple way, creating a password cryptographed by a conversion common table but it isn't professional enough.
 - Get rid totally of Shiginima Launcher. Yes, I have Shiginima Launcher internally on my Launcher. And if you stop to think, my Launcher ISN'T a Launcher at all; it's just a program to do some fancy things before opening the Shiginima Launcher. I want my program load Minecraft by itself, but I can't find how...

## DexCraft Launcher's main files and objects
DexCraft Launcher needs a lot of specific files to work. You can find all of them in their proper classes, but I'm tell about some of them:
 **- Core File** - "CoreFile", "corefile" or just "cf" it's a common file containing essential data to make the Launcher work. The initial idea was using a JSON, but I couldn't make a way to build a fancy, editable and human-readable JSON, so I created my own "text document script type". Example of its layout:

> {
&nbsp; &nbsp; &nbsp;Information
&nbsp; &nbsp; &nbsp;{
&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;value 1
&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;value 2
&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;value 3
&nbsp; &nbsp; &nbsp;}
&nbsp; &nbsp; &nbsp;Another Information
&nbsp; &nbsp; &nbsp;{
&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;value 1
&nbsp; &nbsp; &nbsp; }
}

See the class ScriptFileReader for details.
 **edit: Since DexCraft Launcher Init v2.1.0-201128-555 the CoreFile is a JSON file.**

**- cfurl** - "CoreFile URL" contain the URL to download the Core File. Without it, the program doesn't work. This file isn't provided on the repository because it could be harm the security of my server since the CoreFile may contain the servers passwords.
 **edit: Since DexCraft Launcher Init v2.1.0-201128-555 cfurl is a JSON file.**

**- "locks"** - In order to allow the working of a lot of funcionalities, I needed to put empty files as "locks", to check if some task is running, or to make logging possible using the same txt file.
**edit: Since DexCraft Launcher Init v2.1.0-201128-555 only the loglock remains; the other "locks" were transfered to Launcher Properties File's values (see below)**

**- Launcher Properties File** - Since DexCraft Launcher Init v2.0.0-201018-358, it was a file containing some simple attributes. Usually with the name DexCraftLauncher.properties, now it's DexCraftLauncher.json, a JSON file containing the locks (see above) for the applications and other important info.


# Logbook #
 **warning: since almost all versions werent send to a git and versioning system, they are informed here only for record.**
 
## v1.0.0-181015-128
 * Launcher first release.
 
## v2.0.0-181102-353
* Code conversion to Java 8.

## v3.0.0-190120-481
* Added another minecraft client (DexCraft Vanilla).

## v4.0.0-190304-657
* Minor bugfixes and new implementations.

## v5.0.0-190520-764
* Added patching system.

## v6.0.0-190801-898
* Changed patching system for a better one.
* Minor bugfixes.
* Entire code refactoring.

## v7.0.0-191102-1108
* Added Minecraft Pixelmon Server.

## v8.0.0-191210-1874
* DexCraft Launcher Init implementation.

## v9.0.0-200310-2105
* Performance fixes.
* Bugfixes:
	* solved bug where player's performance settings were not applied properly. 

## v10.0.0-200511-2242
* Performance fixes.
* Offline Mode implementation (now users can play even without internet).
* SFTP service replaced by FTP service since I don't have a proper, funcional reliable SFTP server.
* Code partially refactored.
* Added option to backup singleplayer maps at player's choice.
* Changed patching system for a better one.

## v10.0.1-200512-2245
* Bugfix:
	* solved bug where player sync wasn't working properly. 
	* solved bug where player data wasn't restored when asked.

## v10.0.2-200512-2247
* Bugfix: solved bug where client stop working after patch update.

## v10.1.0-200513-2248
* Bugfix: solved bug where a minecraft client update caused uninstall of any other minecraft clients.
* New shaderpacks provided for DexCraft Pixelmon minecraft client.

## v10.1.1-200515-2249
* Bugfix: solved bug where changing logged player didn't reset minecraft client settings.
* Performance fix: backup system.

## v10.1.2-200515-2251
* Bugfix: solved bug where changing logged player didn't make the data restoration.

## v10.1.3-200516-2257
* Bugfix: solved bug where player's fist sync after account creation always failed.
* Performance fix: java update and installation.

## v10.1.4-200516-2258
* Bugfix: solved bug where player's first sync after account creation never apply performance adjusts.

## v10.2.0-200516-2260
* Backup and sync system remade from scratch.

## v10.2.1-200516-2278
* Bugfix: solved bug where if the last player's sync threw an error, the client asked for a new account creation.
* UI improvements.

## v10.2.2-200517-2280
* Bugfix: solved bug where performance adjusts weren't applied correctly.

## v10.2.3-200517-2282
* Bugfix: solved bug where performance adjusts weren't applied correctly.

## v10.2.4-200518-2289
* Minor bugfixes.

## v10.2.5-200518-2291
* FTP connection bugfixes.

## v10.2.6-200527-2292
* FTP connection bugfixes.

## v10.2.7-200603-2295
* Bugfixes:
	* solved bug where new player accounts weren't created.
	* solved bug where change logged player resulted on the previous another player backup overwriting with the new player logged in. 

## v10.2.8-200608-2296
* Bugfix: solved bug where new player previous login sessions malfunctioning.

## v10.2.9-200612-2297
* Minor bugfixes.

## v11.0.0-201130-2392
First commit under code refactoring.
Created:
* Splash Screen.
* Login Screen.
* Login fields validation class.

## v11.0.0-201213-2416
A lot of progress in the client creation. Not totally done yet, but I keep working as I have time to it.
I changed the entire utility classes to another folder in package so I can make it as a library in the future. I've tried a lot but I couldn't make it right now.

Created:
* Icons for the servers (got from the old client version).
* CSS to stylize fonts and stuff.
* Main client window fxml base file.
* Thread class to run and change dynamically a background wallpaper in the client's main window.
* Thread class to play music during client execution.
* Service and threads to validate login fields and access database to create a new player account.

## v11.0.0-201225-2643
Gladly back to Github!

* DexCraft Commons Classes:
	In order to keep the getters and setters better organized, and keep a better code maintenability, I separate the Utility Classes in DAO/DTO model. I could use JSON serialization, but I didn't prefer it.
	* AdminExecution.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances.
	* OfflineMode.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances.
		* Removal JSONUtility instances and using DTO to data transfering.
	* PreventSecondInstance.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances.
		* Removal JSONUtility instances and using DTO to data transfering.
	* ProvisionedPackage.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances.
		* Removal JSONUtility instances and using DTO to data transfering.
		* Added method for checking package presence.
		* Refactored method for checking if package is outdated.
	* SystemRequirements.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances.
		* Removal JSONUtility instances and using DTO to data transfering.
	* Database.java:
		* Name changed to SqlDAO to fit more suitable to its function.
	* FTP.java:
		* Name changed to FtpDAO to fit more suitable to its function.
	* JSONUtility.java:
		* Name changed to JsonDAO to fit more suitable to its function.
	* Close.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances.
		* Removal JSONUtility instances and using DTO to data transfering.
		* Entire refactoring to better readability.
	* Crypto.java:
		* Created class to encrypt backup file passwords.
	* DexCraftFiles.java:
		* Changed DexCraft Launcher installed directory's folder structure, centralizing their folders inside C:\DexCraft\launcher.
		* Created file variables for checking program and game client installation.
	* DexUI.java:
		* Added funcionalities for Ping Monitoring Service and Background Randomizer Service, with transition animation for background changes.
	* Download.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances. 
	* ErrorAlerts.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances. 
		* Adjusted the Close class callers.
	* FileIO.java:
		* Logger constructor removal from class since Commons now have a main class to bind instances.
	* Install.java:
		* Created method to download patch file and verify installation aditional rules (like special file exclusions).
	* Validate.java:
		* Now organized as a Service.
* DexCraft Launcher Client:
	* Changed changeStatus method to suit better to new Validate functions related to interactions with other scenes.
	* Added new icons for ping monitoring.
	* CSS - images.css:
		* Added CSS for applying image styles. 
	* CSS - fxmlFont.css:
		* Added new CSS IDs.
	* FXML - AboutWindow.fxml:
		* Created Combo Box for showing packages versions.
	* FXML - LoginScreen.fxml:
		* Reduced scene size.
	* FXML - MainWindow.fxml:
		*  Reduced scene size.
	* FXML - Preloader.fxml:
		*   Reduced scene size.
* Created Services:
	* LoginServices.java:
		* Performs login.
	* MusicPlayerService.java:
		* Plays music in background during Launcher execution.
	* BgImageRandomService.java:
		* Changes background wallpaper periodically.
	* MainService.java:
		*  Main service, verifies patches, installations and validates them.
	* PingService.java:
		* Monitors latency with the servers and show to user.  
## v11.0.0-210101-2712
* DexCraft Commons:
	* JsonDAO.java:
		* Changed log format to match correctly to the logger class requirements.
	* SqlDAO.java:
		* Removed the timestamp value of the table. I got the conclusion that having the backup timestamp in the database table is a wrong idea.
	* SystemDTO.java:
		* Added the GET, SET and PARSE methods to the backup directives.
	* DexCraftFiles.java:
		* Added local session cache folder variable.
		* Added binaries folder variable which will contain 3rd party programs
		* Added the runtime folder variable which contain files needed to the internal launcher (Shiginima Launcher)
		* Changed the variable which validates DexCraft Factions Client to use the files in runtime folder as reference.
		* Changed the variable which validates DexCraft Pixelmon Client to use the files in runtime folder as reference.
		* Changed the variable which validates DexCraft Vanilla Client to use the files in runtime folder as reference.
		* Changed the variable which validates DexCraft Beta Client to use the files in runtime folder as reference.
		* Added a variable of the syncproperties.json, a special file which will contain all the game clients timestamps.
		* Added a variable to the temporary folder for ftp syncronization.
	* DexUI.java:
		* Reduced animation latency of the progress bar.
		* Added method to retrieve button.
	* FileIO.java:
		* Changed algorithm logic to determine file presence in a better precise way.
	* FtpDAO.java:
		* Name changed to FtpUtils to reflect its practical use.
	* FtpUtils.java:
		* Created.   
	* Install.java:
		* Changed patching process to fix problems on finding the file to be manipulated.
* DexCraft Launcher Client:
	* MainWindowController.java:
		* Created event to validate and syncronize account with FTP Server.
		* Created event to prepate internal launcher's assets and run the launcher.
	* MainWindow.fxml:
		* Changed font color to some labels.  
* DexCraft Launcher Client Services:
	* AccountSyncService.java:
		* Created. This Service will validate the account data according to the FTP Server's timestamp, and will be used to another client-server operations.
	* LoginService.java:
		* Refactored and changed to better suit to the program needs.     
	* MainService.java:
		* Changed waiter method.
	* MusicPlayerService.java:
		* Changed log format to match correctly to the logger class requirements.
	* PrepareLauncherService.java:
		*Created. This Service will prepare the internal launcher assets to run the launcher and will apply profile settings to the player.
	* Validate.java:
		* Code refactoring for better readability.
## v11.0.0-210105-2728
* DexCraft Commons:
	* AdminExecution.java:
		* Logger calls removed since in Init, the Admin Execution utility is called before logging initialization.
	* PreventSecondInstance.java:
		* Fixed bug where starting Client at first time marks as another instance was running already.
	* SystemRequirements.java:
		* Fixed bug where Init couldn't load System Requirements since it was needing CoreFile in the constructor.
	* SystemDTO.java:
		* Added DexCraft Background Services assets variable.
	* VersionsDTO.java:
		* Fixed bug where the Client version wasn't updating.  
	* Close.java:
		* Added exit call to closing methods.
	* DexCraftFiles.java:
		* Added lock file to stop Launcher music player.
	* FtpUtils.java:
		* Added method for preventing accidental losing of previous file in folder while uploading a new one.
* DexCraft Launcher Client Services:
	* AccountSyncService.java:
		* Added local backup validation
	* MusicPlayerService.java:
		* Fixed bug where the Music Player Service keeps running even with the Launcher finished.
	* PrepareLauncherService.java:
		* Fixed bug where the servers.dat wasn't loaded to the profile.   