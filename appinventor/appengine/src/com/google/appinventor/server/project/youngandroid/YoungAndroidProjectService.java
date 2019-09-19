// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.FileExporterImpl;
import com.google.appinventor.server.Server;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceFolderNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidYailNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.Settings;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provides support for Young Android projects.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author markf@google.com (Mark Friedman)
 */
public final class YoungAndroidProjectService extends CommonProjectService {

  private static int currentProgress = 0;
  private static final Logger LOG = Logger.getLogger(YoungAndroidProjectService.class.getName());

  // The value of this flag can be changed in appengine-web.xml
  private static final Flag<Boolean> sendGitVersion =
    Flag.createFlag("build.send.git.version", true);

  // Project folder prefixes
  public static final String SRC_FOLDER = YoungAndroidSourceAnalyzer.SRC_FOLDER;
  protected static final String ASSETS_FOLDER = "assets";
  private static final String EXTERNAL_COMPS_FOLDER = "assets/external_comps";
  static final String PROJECT_DIRECTORY = "youngandroidproject";

  // TODO(user) Source these from a common constants library.
  private static final String FORM_PROPERTIES_EXTENSION =
      YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION;
  private static final String CODEBLOCKS_SOURCE_EXTENSION =
      YoungAndroidSourceAnalyzer.CODEBLOCKS_SOURCE_EXTENSION;
  private static final String BLOCKLY_SOURCE_EXTENSION =
      YoungAndroidSourceAnalyzer.BLOCKLY_SOURCE_EXTENSION;
  private static final String YAIL_FILE_EXTENSION =
      YoungAndroidSourceAnalyzer.YAIL_FILE_EXTENSION;

  public static final String PROJECT_PROPERTIES_FILE_NAME = PROJECT_DIRECTORY + "/" +
      "project.properties";

  private static final JSONParser JSON_PARSER = new ServerJsonParser();

  // Build folder path
  private static final String BUILD_FOLDER = "build";

  public static final String PROJECT_KEYSTORE_LOCATION = "android.keystore";

  // host[:port] to use for connecting to the build server
  private static final Flag<String> buildServerHost =
      Flag.createFlag("build.server.host", "localhost:9990");
  // host[:port] to tell build server app host url
  private static final Flag<String> appengineHost =
      Flag.createFlag("appengine.host", "");
  private static final boolean DEBUG = Flag.createFlag("appinventor.debugging", false).get();

  public YoungAndroidProjectService(StorageIo storageIo) {
    super(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, storageIo);
  }

  /**
   * Returns project settings that can be used when creating a new project.
   */
  public static String getProjectSettings(String icon, String vCode, String vName,
    String useslocation, String aName, String sizing, String showListsAsJson, String tutorialURL,
                                          String applicationPackage, String oneSignalAppId
            , String minApi, String maxApi) {
    icon = Strings.nullToEmpty(icon);
    vCode = Strings.nullToEmpty(vCode);
    vName = Strings.nullToEmpty(vName);
    useslocation = Strings.nullToEmpty(useslocation);
    sizing = Strings.nullToEmpty(sizing);
    aName = Strings.nullToEmpty(aName);
    showListsAsJson = Strings.nullToEmpty(showListsAsJson);
    tutorialURL = Strings.nullToEmpty(tutorialURL);
    oneSignalAppId = Strings.nullToEmpty(oneSignalAppId);
    applicationPackage = Strings.nullToEmpty(applicationPackage);

    minApi = Strings.nullToEmpty(minApi);
    maxApi = Strings.nullToEmpty(maxApi);

    return "{\"" + SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS + "\":{" +
        "\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON + "\":\"" + icon +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE + "\":\"" + vCode +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME + "\":\"" + vName +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION + "\":\"" + useslocation +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME + "\":\"" + aName +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING + "\":\"" + sizing +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON + "\":\"" + showListsAsJson +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL + "\":\"" + tutorialURL +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_APPLICATION_PACKAGE + "\":\"" + applicationPackage +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_ONE_SIGNAL_APP_ID + "\":\"" + oneSignalAppId +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_MinAPI + "\":\"" + minApi +
        "\",\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_MaxAPI + "\":\"" + maxApi +
        "\"}}";
  }

  /**
   * Returns the contents of the project properties file for a new Young Android
   * project.
   *
   * @param projectName the name of the project
   * @param qualifiedName the qualified name of Screen1 in the project
   * @param icon the name of the asset to use as the application icon
   * @param vcode the version code
   * @param vname the version name
   */
  public static String getProjectPropertiesFileContents(String projectName, String qualifiedName,
    String icon, String vcode, String vname, String useslocation, String aname,
    String sizing, String showListsAsJson, String tutorialURL, String applicationPackage, String oneSignalAppId
          , String minApi, String maxApi) {
    String contents = "main=" + qualifiedName + "\n" +
        "name=" + projectName + '\n' +
        "assets=../" + ASSETS_FOLDER + "\n" +
        "source=../" + SRC_FOLDER + "\n" +
        "build=../build\n";
    if (icon != null && !icon.isEmpty()) {
      contents += "icon=" + icon + "\n";
    }
    if (vcode != null && !vcode.isEmpty()) {
      contents += "versioncode=" + vcode + "\n";
    }
    if (vname != null && !vname.isEmpty()) {
      contents += "versionname=" + vname + "\n";
    }
    if (useslocation != null && !useslocation.isEmpty()) {
      contents += "useslocation=" + useslocation + "\n";
    }
    if (aname != null) {
      contents += "aname=" + aname + "\n";
    }
    if (sizing != null && !sizing.isEmpty()) {
      contents += "sizing=" + sizing + "\n";
    }
    if (showListsAsJson != null && !showListsAsJson.isEmpty()) {
      contents += "showlistsasjson=" + showListsAsJson + "\n";
    }
    if (tutorialURL != null && !tutorialURL.isEmpty()) {
      contents += "tutorialurl=" + tutorialURL + "\n";
    }
    if (applicationPackage != null && !applicationPackage.isEmpty()) {
      contents += "ApplicationPackage=" + applicationPackage + "\n";
    }

    if (oneSignalAppId != null && !oneSignalAppId.isEmpty()) {
      contents += "AppId=" + oneSignalAppId + "\n";
    }

    if (minApi != null && !minApi.isEmpty()) {
      contents += "MinAPI=" + minApi + "\n";
    }

    if (maxApi != null && !maxApi.isEmpty()) {
      contents += "MaxAPI=" + maxApi + "\n";
    }

    return contents;
  }

  /**
   * Returns the contents of a new Young Android form file.
   * @param qualifiedName the qualified name of the form.
   * @return the contents of a new Young Android form file.
   */
  @VisibleForTesting
  public static String getInitialFormPropertiesFileContents(String qualifiedName) {
    // read in reverse
    String[] tokens = qualifiedName.split("\\.");
    String formName = tokens[tokens.length - 1];
    String appName = tokens[tokens.length - 2];
    final int lastDotPos = qualifiedName.lastIndexOf('.');

    //hossein: packageName. for appinventor.ai_kkashi01.AppName.FormName, get the 2rd element
    //hossein: packageName. for com.appybuilder.kkashi01.AppName.FormName, get the 3th element
//    String appName = qualifiedName.split("\\.")[2];     // for appinventor.ai_kkashi01.SomeAppName
//    String appName = qualifiedName.split("\\.")[3];     // for com.appybuilder.kkashi01.SomeAppName
//      String formName = qualifiedName.substring(lastDotPos + 1);
    // The initial Uuid is set to zero here since (as far as we know) we can't get random numbers
    // in ode.shared.  This shouldn't actually matter since all Uuid's are random int's anyway (and
    // 0 was randomly chosen, I promise).  The TODO(user) in MockComponent.java indicates that
    // there will someday be assurance that these random Uuid's are unique.  Once that happens
    // this will be perfectly acceptable.  Until that happens, choosing 0 is just as safe as
    // allowing a random number to be chosen when the MockComponent is first created.
    return "#|\n$JSON\n" +
        "{\"authURL\":[]," +
        "\"YaVersion\":\"" + YaVersion.YOUNG_ANDROID_VERSION + "\",\"Source\":\"Form\"," +
        "\"Properties\":{\"$Name\":\"" + formName + "\",\"$Type\":\"Form\"," +
        "\"$Version\":\"" + YaVersion.FORM_COMPONENT_VERSION + "\",\"Uuid\":\"" + 0 + "\"," +
        "\"Title\":\"" + formName + "\",\"AppName\":\"" + appName +"\"}}\n|#";
  }

  /**
   * Returns the initial contents of a Young Android blockly blocks file.
   */
  private static String getInitialBlocklySourceFileContents(String qualifiedName) {
    return "";
  }

  private static String packageNameToPath(String packageName) {
    return SRC_FOLDER + '/' + packageName.replace('.', '/');
  }

  public static String getSourceDirectory(String qualifiedName) {
    return StorageUtil.dirname(packageNameToPath(qualifiedName));
  }

  // CommonProjectService implementation

  @Override
  public void storeProjectSettings(String userId, long projectId, String projectSettings) {
    super.storeProjectSettings(userId, projectId, projectSettings);

    // If the icon has been changed, update the project properties file.
    // Extract the new icon from the projectSettings parameter.
    Settings settings = new Settings(JSON_PARSER, projectSettings);
    String newIcon = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON));
    String newVCode = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE));
    String newVName = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME));
    String newUsesLocation = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION));
    String newSizing = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING));
    String newShowListsAsJson = Strings.nullToEmpty(settings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON));
    String newTutorialURL = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL));
    String newApplicationPackage = Strings.nullToEmpty(settings.getSetting(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_APPLICATION_PACKAGE));
    String newOneSignalAppId = Strings.nullToEmpty(settings.getSetting(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ONE_SIGNAL_APP_ID));
    String newAName = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME));

   String newMinApi = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_MinAPI));

   String newMaxApi = Strings.nullToEmpty(settings.getSetting(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_MaxAPI));

    // Extract the old icon from the project.properties file from storageIo.
    String projectProperties = storageIo.downloadFile(userId, projectId,
        PROJECT_PROPERTIES_FILE_NAME, StorageUtil.DEFAULT_CHARSET);
    Properties properties = new Properties();
    try {
      properties.load(new StringReader(projectProperties));
    } catch (IOException e) {
      // Since we are reading from a String, I don't think this exception can actually happen.
      e.printStackTrace();
      return;
    }
    String oldIcon = Strings.nullToEmpty(properties.getProperty("icon"));
    String oldVCode = Strings.nullToEmpty(properties.getProperty("versioncode"));
    String oldVName = Strings.nullToEmpty(properties.getProperty("versionname"));
    String oldUsesLocation = Strings.nullToEmpty(properties.getProperty("useslocation"));
    String oldSizing = Strings.nullToEmpty(properties.getProperty("sizing"));
    String oldAName = Strings.nullToEmpty(properties.getProperty("aname"));
    String oldShowListsAsJson = Strings.nullToEmpty(properties.getProperty("showlistsasjson"));
    String oldTutorialURL = Strings.nullToEmpty(properties.getProperty("tutorialurl"));
    String oldApplicationPackage = Strings.nullToEmpty(properties.getProperty("ApplicationPackage"));
    String oldOneSignalAppId = Strings.nullToEmpty(properties.getProperty("AppId"));
    String oldMinApi = Strings.nullToEmpty(properties.getProperty("MinAPI"));
    String oldMaxApi = Strings.nullToEmpty(properties.getProperty("MaxAPI"));

    if (!newIcon.equals(oldIcon) || !newVCode.equals(oldVCode) || !newVName.equals(oldVName)
      || !newUsesLocation.equals(oldUsesLocation) ||
            !newAName.equals(oldAName) || !newSizing.equals(oldSizing) ||
      !newShowListsAsJson.equals(oldShowListsAsJson) ||
      !newTutorialURL.equals(oldTutorialURL)
            || !newApplicationPackage.equals(oldApplicationPackage)
            || !newOneSignalAppId.equals(oldOneSignalAppId)
            || !newMinApi.equals(oldMinApi)
            || !newMaxApi.equals(oldMaxApi)
            ) {
      // Recreate the project.properties and upload it to storageIo.
      String projectName = properties.getProperty("name");
      String qualifiedName = properties.getProperty("main");
      String newContent = getProjectPropertiesFileContents(projectName, qualifiedName, newIcon,
        newVCode, newVName, newUsesLocation, newAName, newSizing, newShowListsAsJson, newTutorialURL, newApplicationPackage, newOneSignalAppId, newMinApi, newMaxApi);
      storageIo.uploadFileForce(projectId, PROJECT_PROPERTIES_FILE_NAME, userId,
          newContent, StorageUtil.DEFAULT_CHARSET);
    }
  }

  /**
   * {@inheritDoc}
   *
   * {@code params} needs to be an instance of
   * {@link NewYoungAndroidProjectParameters}.
   */
  @Override
  public long newProject(String userId, String projectName, NewProjectParameters params) {
    NewYoungAndroidProjectParameters youngAndroidParams = (NewYoungAndroidProjectParameters) params;
    String qualifiedFormName = youngAndroidParams.getQualifiedFormName();

    String propertiesFileName = PROJECT_PROPERTIES_FILE_NAME;
    String propertiesFileContents = getProjectPropertiesFileContents(projectName,
      qualifiedFormName, null, null, null, null, null, null, null, null, null, null, null, null);

    String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
    String formFileContents = getInitialFormPropertiesFileContents(qualifiedFormName);

    String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
    String blocklyFileContents = getInitialBlocklySourceFileContents(qualifiedFormName);

    String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);
    String yailFileContents = "";

    Project project = new Project(projectName);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    // Project history not supported in legacy ode new project wizard
    project.addTextFile(new TextFile(propertiesFileName, propertiesFileContents));
    project.addTextFile(new TextFile(formFileName, formFileContents));
    project.addTextFile(new TextFile(blocklyFileName, blocklyFileContents));
    project.addTextFile(new TextFile(yailFileName, yailFileContents));

    // Create new project
    return storageIo.createProject(userId, project, getProjectSettings("", "1", "1.0", "false", projectName, "Fixed", "false", "", "", "", "14", "26"));
  }

  @Override
  public long copyProject(String userId, long oldProjectId, String newName) {
    String oldName = storageIo.getProjectName(userId, oldProjectId);
    String oldProjectSettings = storageIo.loadProjectSettings(userId, oldProjectId);
    String oldProjectHistory = storageIo.getProjectHistory(userId, oldProjectId);
    Settings oldSettings = new Settings(JSON_PARSER, oldProjectSettings);
    String icon = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON);
    String vcode = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE);
    String vname = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME);
    String useslocation = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION);
    String aname = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME);
    String sizing = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING);
    String showListsAsJson = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON);
    String tutorialURL = oldSettings.getSetting(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL);
    String applicationPackage = oldSettings.getSetting(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_APPLICATION_PACKAGE);
    String oneSignalAppId = oldSettings.getSetting(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ONE_SIGNAL_APP_ID);

    String minApi = oldSettings.getSetting(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_MinAPI);

    String maxApi = oldSettings.getSetting(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_MaxAPI);


    Project newProject = new Project(newName);
    newProject.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    newProject.setProjectHistory(oldProjectHistory);

    // Get the old project's source files and add them to new project, modifying where necessary.
    for (String oldSourceFileName : storageIo.getProjectSourceFiles(userId, oldProjectId)) {
      String newSourceFileName;

      String newContents = null;
      if (oldSourceFileName.equals(PROJECT_PROPERTIES_FILE_NAME)) {
        // This is the project properties file. The name of the file doesn't contain the old
        // project name.
        newSourceFileName = oldSourceFileName;
        // For the contents of the project properties file, generate the file with the new project
        // name and qualified name.
        String qualifiedFormName = StringUtils.getQualifiedFormName(
            storageIo.getUser(userId).getUserEmail(), newName);
        newContents = getProjectPropertiesFileContents(newName, qualifiedFormName, icon, vcode,
          vname, useslocation, aname, sizing, showListsAsJson, tutorialURL, applicationPackage, oneSignalAppId, minApi, maxApi);
      } else {
        // This is some file other than the project properties file.
        // oldSourceFileName may contain the old project name as a path segment, surrounded by /.
        // Replace the old name with the new name.
        newSourceFileName = StringUtils.replaceLastOccurrence(oldSourceFileName,
            "/" + oldName + "/", "/" + newName + "/");
      }

      if (newContents != null) {
        // We've determined (above) that the contents of the file must change for the new project.
        // Use newContents when adding the file to the new project.
        newProject.addTextFile(new TextFile(newSourceFileName, newContents));
      } else {
        // If we get here, we know that the contents of the file can just be copied from the old
        // project. Since it might be a binary file, we copy it as a raw file (that works for both
        // text and binary files).
        byte[] contents = storageIo.downloadRawFile(userId, oldProjectId, oldSourceFileName);
        newProject.addRawFile(new RawFile(newSourceFileName, contents));
      }
    }

    // Create the new project and return the new project's id.
    return storageIo.createProject(userId, newProject, getProjectSettings(icon, vcode, vname,
        useslocation, aname, sizing, showListsAsJson, tutorialURL, applicationPackage, oneSignalAppId, minApi, maxApi));
  }

  @Override
  public ProjectRootNode getRootNode(String userId, long projectId) {
    // Create root, assets, and source nodes (they are mocked nodes as they don't really
    // have to exist like this on the file system)
    ProjectRootNode rootNode =
        new YoungAndroidProjectNode(storageIo.getProjectName(userId, projectId),
                                    projectId);
    ProjectNode assetsNode = new YoungAndroidAssetsFolder(ASSETS_FOLDER);
    ProjectNode sourcesNode = new YoungAndroidSourceFolderNode(SRC_FOLDER);
    ProjectNode compsNode = new YoungAndroidComponentsFolder(EXTERNAL_COMPS_FOLDER);

    rootNode.addChild(assetsNode);
    rootNode.addChild(sourcesNode);
    rootNode.addChild(compsNode);

    // Sources contains nested folders that are interpreted as packages
    Map<String, ProjectNode> packagesMap = Maps.newHashMap();

    // Retrieve project information
    List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
    for (String fileId : sourceFiles) {
      if (fileId.startsWith(ASSETS_FOLDER + '/')) {
        if (fileId.startsWith(EXTERNAL_COMPS_FOLDER + '/')) {
          compsNode.addChild(new YoungAndroidComponentNode(StorageUtil.basename(fileId), fileId));
        }
        else {
          assetsNode.addChild(new YoungAndroidAssetNode(StorageUtil.basename(fileId), fileId));
        }
      } else if (fileId.startsWith(SRC_FOLDER + '/')) {
        // We send form (.scm), blocks (.blk), and yail (.yail) nodes to the ODE client.
        YoungAndroidSourceNode sourceNode = null;
        if (fileId.endsWith(FORM_PROPERTIES_EXTENSION)) {
          sourceNode = new YoungAndroidFormNode(fileId);
        } else if (fileId.endsWith(BLOCKLY_SOURCE_EXTENSION)) {
          sourceNode = new YoungAndroidBlocksNode(fileId);
        } else if (fileId.endsWith(CODEBLOCKS_SOURCE_EXTENSION)) {
          String blocklyFileName =
              fileId.substring(0, fileId.lastIndexOf(CODEBLOCKS_SOURCE_EXTENSION))
              + BLOCKLY_SOURCE_EXTENSION;
          if (!sourceFiles.contains(blocklyFileName)) {
            // This is an old project that hasn't been converted yet. Convert
            // the blocks file to Blockly format and name. Leave the old
            // codeblocks file around for now (for debugging) but don't send it to the client.
            String blocklyFileContents = convertCodeblocksToBlockly(userId, projectId, fileId);
            storageIo.addSourceFilesToProject(userId, projectId, false, blocklyFileName);
            storageIo.uploadFileForce(projectId, blocklyFileName, userId, blocklyFileContents,
                StorageUtil.DEFAULT_CHARSET);
            sourceNode = new YoungAndroidBlocksNode(blocklyFileName);
          }
        } else if (fileId.endsWith(YAIL_FILE_EXTENSION)) {
          sourceNode = new YoungAndroidYailNode(fileId);
        }
        if (sourceNode != null) {
          String packageName = StorageUtil.getPackageName(sourceNode.getQualifiedName());
          ProjectNode packageNode = packagesMap.get(packageName);
          if (packageNode == null) {
            packageNode = new YoungAndroidPackageNode(packageName, packageNameToPath(packageName));
            packagesMap.put(packageName, packageNode);
            sourcesNode.addChild(packageNode);
          }
          packageNode.addChild(sourceNode);
        }
      }
    }

    return rootNode;
  }

  /*
   * Convert the contents of the codeblocks file named codeblocksFileId
   * to blockly format and return the blockly contents.
   */
  private String convertCodeblocksToBlockly(String userId, long projectId,
      String codeblocksFileId) {
    // TODO(sharon): implement this!
    return "";
  }

  @Override
  public long addFile(String userId, long projectId, String fileId) {
    if (fileId.endsWith(FORM_PROPERTIES_EXTENSION) ||
        fileId.endsWith(BLOCKLY_SOURCE_EXTENSION)) {
      // If the file to be added is a form file or a blocks file, add a new form file, a new
      // blocks file, and a new yail file (as a placeholder for later code generation)
      String qualifiedFormName = YoungAndroidSourceNode.getQualifiedName(fileId);
      String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
      String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);

      List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
      if (!sourceFiles.contains(formFileName) &&
          !sourceFiles.contains(blocklyFileName) &&
          !sourceFiles.contains(yailFileName)) {

        String formFileContents = getInitialFormPropertiesFileContents(qualifiedFormName);
        storageIo.addSourceFilesToProject(userId, projectId, false, formFileName);
        storageIo.uploadFileForce(projectId, formFileName, userId, formFileContents,
            StorageUtil.DEFAULT_CHARSET);

        String blocklyFileContents = getInitialBlocklySourceFileContents(qualifiedFormName);
        storageIo.addSourceFilesToProject(userId, projectId, false, blocklyFileName);
        storageIo.uploadFileForce(projectId, blocklyFileName, userId, blocklyFileContents,
            StorageUtil.DEFAULT_CHARSET);

        String yailFileContents = "";  // start empty
        storageIo.addSourceFilesToProject(userId, projectId, false, yailFileName);
        return storageIo.uploadFileForce(projectId, yailFileName, userId, yailFileContents,
            StorageUtil.DEFAULT_CHARSET);
      } else {
        throw new IllegalStateException("One or more files to be added already exists.");
      }

    } else {
      return super.addFile(userId, projectId, fileId);
    }
  }

  @Override
  public long copyScreen(String userId, long projectId, String targetFileId, String fileId) {
    if (fileId.endsWith(FORM_PROPERTIES_EXTENSION) ||
            fileId.endsWith(BLOCKLY_SOURCE_EXTENSION)) {
      // If the file to be added is a form file or a blocks file, add a new form file, a new
      // blocks file, and a new yail file (as a placeholder for later code generation)
      String qualifiedFormName = YoungAndroidSourceNode.getQualifiedName(fileId);
      String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
      String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);
      String targetQualifiedFormName = YoungAndroidSourceNode.getQualifiedName(targetFileId);
      String targetFormFileName = YoungAndroidFormNode.getFormFileId(targetQualifiedFormName);
      String targetBlocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(targetQualifiedFormName);
      String targetYailFileName = YoungAndroidYailNode.getYailFileId(targetQualifiedFormName);
      List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
      //LOG.info("source files are:" +sourceFiles);

      boolean hasYail = true;
      if (!sourceFiles.contains(formFileName) &&
              !sourceFiles.contains(blocklyFileName)
              && !sourceFiles.contains(yailFileName)
              ) {
        hasYail = sourceFiles.contains(targetYailFileName);
        if (sourceFiles.contains(targetFormFileName) &&
                sourceFiles.contains(targetBlocklyFileName)
//                &&sourceFiles.contains(targetYailFileName)
                ) {
          //Screen1, or Screen2
          int lastDotPos = qualifiedFormName.lastIndexOf('.');
          String simpleFormName = qualifiedFormName.substring(lastDotPos + 1);
          lastDotPos = targetQualifiedFormName.lastIndexOf('.');
          String simpleTargetFormName = targetQualifiedFormName.substring(lastDotPos + 1);
          String formFileContents = load(userId, projectId, targetFormFileName).replace(simpleTargetFormName, simpleFormName);
          storageIo.addSourceFilesToProject(userId, projectId, false, formFileName);
          storageIo.uploadFileForce(projectId, formFileName, userId, formFileContents, StorageUtil.DEFAULT_CHARSET);
          if (hasYail) {
              String yailFileContents = load(userId, projectId, targetYailFileName).replace(simpleTargetFormName, simpleFormName);
              storageIo.addSourceFilesToProject(userId, projectId, false, yailFileName);
              storageIo.uploadFileForce(projectId, yailFileName, userId, yailFileContents, StorageUtil.DEFAULT_CHARSET);
          }
        String blocklyFileContents = load(userId, projectId, targetBlocklyFileName).replace(simpleTargetFormName, simpleFormName);
        storageIo.addSourceFilesToProject(userId, projectId, false, blocklyFileName);
        return storageIo.uploadFileForce(projectId, blocklyFileName, userId, blocklyFileContents, StorageUtil.DEFAULT_CHARSET);
        } else {
          throw new IllegalStateException("One or more files to be copied don't exist. Error:" +sourceFiles + ", target:" +  targetYailFileName);
        }
      } else {
        throw new IllegalStateException("One or more files to be added already exists.");
      }
    } else {
      return super.addFile(userId, projectId, fileId);
    }
  }
  @Override
  public long deleteFile(String userId, long projectId, String fileId) {
    if (fileId.endsWith(FORM_PROPERTIES_EXTENSION) ||
        fileId.endsWith(BLOCKLY_SOURCE_EXTENSION)) {
      // If the file to be deleted is a form file or a blocks file, delete both the form file
      // and the blocks file. Also, if there was a codeblocks file laying around
      // for that same form, delete it too (if it doesn't exist the delete
      // for it will be a no-op).
      String qualifiedFormName = YoungAndroidSourceNode.getQualifiedName(fileId);
      String formFileName = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      String blocklyFileName = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
      String codeblocksFileName = YoungAndroidBlocksNode.getCodeblocksFileId(qualifiedFormName);
      String yailFileName = YoungAndroidYailNode.getYailFileId(qualifiedFormName);
      storageIo.deleteFile(userId, projectId, formFileName);
      storageIo.deleteFile(userId, projectId, blocklyFileName);
      storageIo.deleteFile(userId, projectId, codeblocksFileName);
      storageIo.deleteFile(userId, projectId, yailFileName);
      storageIo.removeSourceFilesFromProject(userId, projectId, true,
          formFileName, blocklyFileName, codeblocksFileName, yailFileName);
      return storageIo.getProjectDateModified(userId, projectId);

    } else {
      return super.deleteFile(userId, projectId, fileId);
    }
  }

  /**
   * Make a request to the Build Server to build a project.  The Build Server will asynchronously
   * post the results of the build via the {@link com.google.appinventor.server.ReceiveBuildServlet}
   * A later call will need to be made by the client in order to get those results.
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param nonce random string used to find resulting APK from unauth context
   * @param target  build target (optional, implementation dependent)
   *
   * @return an RpcResult reflecting the call to the Build Server
   */
  @Override
  public RpcResult build(User user, long projectId, String nonce, String target) {
    String userId = user.getUserId();
    String projectName = storageIo.getProjectName(userId, projectId);
    String outputFileDir = BUILD_FOLDER + '/' + target;

    // Store the userId and projectId based on the nonce

    storageIo.storeNonce(nonce, userId, projectId);

    // Delete the existing build output files, if any, so that future attempts to get it won't get
    // old versions.
    List<String> buildOutputFiles = storageIo.getProjectOutputFiles(userId, projectId);
    for (String buildOutputFile : buildOutputFiles) {
      storageIo.deleteFile(userId, projectId, buildOutputFile);
    }
    URL buildServerUrl = null;
    ProjectSourceZip zipFile = null;
    int responseCode = 0;

    // Keeping max at 20MB, otherwise, we may have to change the appengine size
    int maxAiaSize = 20;  // in mb
    String errMsg="";

    int zipFileLength = -1;
    try {
        buildServerUrl = new URL(getBuildServerUrlStr(
            user.getUserEmail(),
            userId,
            projectId,
            outputFileDir));

//      try (BufferedInputStream in = new BufferedInputStream(new URL(buildServerUrl).openStream());
//           FileOutputStream fileOutputStream new FileOutputStream(FILE_NAME)) {
//        byte dataBuffer[] = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
//          fileOutputStream.write(dataBuffer, 0, bytesRead);
//        }
//      } catch (IOException e) {
//        // handle exception
//      }

      HttpURLConnection connection = (HttpURLConnection) buildServerUrl.openConnection();
//      connection.setRequestMethod("PUT");
//      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//      writer.write(URLEncoder.encode(jsonObj.toString(), "UTF-8"));
//      writer.close();

//      urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

//      connection.setConnectTimeout(2000);
      connection.setUseCaches(false);
      connection.setChunkedStreamingMode(1024);
      connection.setRequestProperty("Connection", "Keep-Alive");
//      connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

        connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
      FileExporter fileExporter = new FileExporterImpl();
        zipFile = fileExporter.exportProjectSourceZip(userId, projectId, false,
            /* includeAndroidKeystore */ true,
          projectName + ".aia", true, false, true, false);
      bufferedOutputStream.write(zipFile.getContent());
        bufferedOutputStream.flush();
        bufferedOutputStream.close();


      zipFileLength = zipFile == null ? -1 : zipFile.getContent().length;
      String lengthMbs = format((zipFileLength * 1.0)/(1024*1024));

      // Pre-check to see if we pass our max aia size.
      errMsg = "Sorry, can't package projects larger than " + maxAiaSize + "MB. Yours is " + lengthMbs + "MB. Please consider shrinking assets and/or reducing number of screens";
      if (zipFileLength >= (maxAiaSize * 1024 * 1024) ) {
        return new RpcResult(false, "", errMsg);
      }

      // our project is below the limit
      responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {


        // Put the HTTP response code into the RpcResult so the client code in BuildCommand.java
        // can provide an appropriate error message to the user.
        // NOTE(lizlooney) - There is some weird bug/problem with HttpURLConnection. When the
        // responseCode is 503, connection.getResponseMessage() returns "OK", but it should return
        // "Service Unavailable". If I make the request with curl and look at the headers, they
        // have the expected error message.
        // For now, the moral of the story is: don't use connection.getResponseMessage().
        String error = "Build server responded with response code " + responseCode + ".";
        try {
          String content = readContent(connection.getInputStream());
          if (content != null && !content.isEmpty()) {
            error += "\n" + content;
          }
        } catch (IOException e) {
          // No content. That's ok.
        }
        try {
          String errorContent = readContent(connection.getErrorStream());
          if (errorContent != null && !errorContent.isEmpty()) {
            error += "\n" + errorContent;
          }
        } catch (IOException e) {
          // No error content. That's ok.
        }
        if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
          // The build server is not compatible with this App Inventor instance. Log this as severe
          // so the owner of the app engine instance will know about it.
          LOG.severe(error);
        }

        return new RpcResult(responseCode, "", StringUtils.escape(error));
      }

    } catch (MalformedURLException e) {
      CrashReport.createAndLogError(LOG, null,
          buildErrorMsg("Response code: " + responseCode +  " MalformedURLException", buildServerUrl, userId, projectId), e);
      return new RpcResult(false, "", e.getMessage());
    } catch (IOException e) {
      // As of App Engine 1.9.0 we get these when UrlFetch is asked to send too much data
      Throwable wrappedException = e;
      // Was this due to reaching max aia size?
      if (zipFileLength >= (maxAiaSize * 1024 * 1024) ) {
        wrappedException = new IllegalArgumentException(errMsg, e);
      }
      CrashReport.createAndLogError(LOG, null,
          buildErrorMsg("IOException", buildServerUrl, userId, projectId), wrappedException);
      return new RpcResult(false, "", wrappedException.getMessage());
    }
    catch (EncryptionException e) {
      CrashReport.createAndLogError(LOG, null,
          buildErrorMsg("EncryptionException", buildServerUrl, userId, projectId), e);
      return new RpcResult(false, "", e.getMessage());
    } catch (RuntimeException e) {
      // In particular, we often see RequestTooLargeException (if the zip is too
      // big) and ApiProxyException. There may be others.
      Throwable wrappedException = e;
      if (e instanceof ApiProxy.RequestTooLargeException && zipFile != null) {
        if (zipFileLength >= (maxAiaSize * 1024 * 1024) ) {
          wrappedException = new IllegalArgumentException(errMsg, e);
        } else {
          wrappedException = new IllegalArgumentException(
              "Sorry, project was too large to package (" + zipFileLength + " bytes)");
        }
      }
      CrashReport.createAndLogError(LOG, null,
          buildErrorMsg("RuntimeException", buildServerUrl, userId, projectId), wrappedException);
      return new RpcResult(false, "", wrappedException.getMessage());
    }

    return new RpcResult(true, "Building " + projectName, "");
  }

  // This was an attemp to chunk and overcome the 10mb. However, now checking beforehand to ensure that
  // aia is not over 20mb. Also, we are now using java8 in appengine web and native that allows for building larger files
  private String sendFileToServer(String filename, String targetUrl) {
    String response = "error";
    LOG.info("Image filename" + filename);
    LOG.info("url:" + targetUrl);
    HttpURLConnection connection = null;
    DataOutputStream outputStream = null;
    // DataInputStream inputStream = null;

    String pathToOurFile = filename;
    String urlServer = targetUrl;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024;
    try {
      FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));

      URL url = new URL(urlServer);
      connection = (HttpURLConnection) url.openConnection();

      // Allow Inputs & Outputs
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setUseCaches(false);
      connection.setChunkedStreamingMode(1024);
      // Enable POST method
      connection.setRequestMethod("POST");

      connection.setRequestProperty("Connection", "Keep-Alive");
      connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

      outputStream = new DataOutputStream(connection.getOutputStream());
      outputStream.writeBytes(twoHyphens + boundary + lineEnd);

      String connstr = null;
      connstr = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile + "\"" + lineEnd;
      LOG.info("Connstr: " +  connstr);

      outputStream.writeBytes(connstr);
      outputStream.writeBytes(lineEnd);

      bytesAvailable = fileInputStream.available();
      bufferSize = Math.min(bytesAvailable, maxBufferSize);
      buffer = new byte[bufferSize];

      // Read file
      bytesRead = fileInputStream.read(buffer, 0, bufferSize);
      LOG.info("Image length:" + bytesAvailable + "");
      try {
        while (bytesRead > 0) {
          try {
            outputStream.write(buffer, 0, bufferSize);
          } catch (OutOfMemoryError e) {
            e.printStackTrace();
            response = "outofmemoryerror";
            return response;
          }
          bytesAvailable = fileInputStream.available();
          bufferSize = Math.min(bytesAvailable, maxBufferSize);
          bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }
      } catch (Exception e) {
        e.printStackTrace();
        response = "error";
        return response;
      }
      outputStream.writeBytes(lineEnd);
      outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

      // Responses from the server (code and message)
      int serverResponseCode = connection.getResponseCode();
      String serverResponseMessage = connection.getResponseMessage();
      LOG.info("Server Response Code " + serverResponseCode);
      LOG.info("Server Response Message: " + serverResponseMessage);

      if (serverResponseCode == 200) {
        response = "true";
      }

      String CDate = null;
      Date serverTime = new Date(connection.getDate());
      try {
        CDate = df.format(serverTime);
      } catch (Exception e) {
        e.printStackTrace();
        LOG.info("Date Exception" +  e.getMessage() + " Parse Exception");
      }
      LOG.info("Server Response Time:" + CDate + "");

      filename = CDate + filename.substring(filename.lastIndexOf("."), filename.length());
      LOG.info("File Name in Server : " +  filename);

      fileInputStream.close();
      outputStream.flush();
      outputStream.close();
      outputStream = null;
    } catch (Exception ex) {
      // Exception handling
      response = "error";
      LOG.info("Send file Exception: " + ex.getMessage() + "");
      ex.printStackTrace();
    }
    return response;
  }

  private String buildErrorMsg(String exceptionName, URL buildURL, String userId, long projectId) {
    return "Request to build failed with " + exceptionName + ", user=" + userId
        + ", project=" + projectId + ", build URL is " + buildURL; // + " [" + buildURL.toString().length() + "]";
  }

  // Note that this is a function rather than just a constant because we assume it will get
  // a little more complicated when we want to get the URL from an App Engine config file or
  // command line argument.
  private String getBuildServerUrlStr(String userName, String userId,
                                      long projectId, String fileName)
      throws UnsupportedEncodingException, EncryptionException {
    return "http://" + buildServerHost.get() + "/buildserver/build-all-from-zip-async"
           + "?uname=" + URLEncoder.encode(userName, "UTF-8")
           + (sendGitVersion.get()
               ? "&gitBuildVersion="
                 + URLEncoder.encode(GitBuildId.getVersion(), "UTF-8")
               : "")
           + "&callback="
           + URLEncoder.encode("http://" + getCurrentHost() + ServerLayout.ODE_BASEURL_NOAUTH
                               + ServerLayout.RECEIVE_BUILD_SERVLET + "/"
                               + Security.encryptUserAndProjectId(userId, projectId)
                               + "/" + fileName,
                               "UTF-8");
  }

  private String getCurrentHost() {
    if (Server.isProductionServer()) {
      if (appengineHost.get()=="") {
        String applicationVersionId = SystemProperty.applicationVersion.get();
        String applicationId = SystemProperty.applicationId.get();
        return applicationVersionId + "." + applicationId + ".appspot.com";
      } else {
        return appengineHost.get();
      }
    } else {
      // TODO(user): Figure out how to make this more generic
      return "localhost:8888";
    }
  }

  /*
   * Reads the UTF-8 content from the given input stream.
   */
  private static String readContent(InputStream stream) throws IOException {
    if (stream != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      try {
        return CharStreams.toString(reader);
      } finally {
        reader.close();
      }
    }
    return null;
  }

  /**
   * Check if there are any build results available for the given user's project
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param target  build target (optional, implementation dependent)
   * @return an RpcResult reflecting the call to the Build Server. The following values may be in
   *         RpcResult.result:
   *            0:  Build is done and was successful
   *            1:  Build is done and was unsuccessful
   *            2:  Yail generation failed
   *           -1:  Build is not yet done.
   */
  @Override
  public RpcResult getBuildResult(User user, long projectId, String target) {
    String userId = user.getUserId();
    String buildOutputFileName = BUILD_FOLDER + '/' + target + '/' + "build.out";
    List<String> outputFiles = storageIo.getProjectOutputFiles(userId, projectId);
    updateCurrentProgress(user, projectId, target);
    RpcResult buildResult = new RpcResult(-1, ""+currentProgress, ""); // Build not finished
    for (String outputFile : outputFiles) {
      if (buildOutputFileName.equals(outputFile)) {
        String outputStr = storageIo.downloadFile(userId, projectId, outputFile, "UTF-8");
        try {
          JSONObject buildResultJsonObj = new JSONObject(outputStr);
          buildResult = new RpcResult(buildResultJsonObj.getInt("result"),
                                      buildResultJsonObj.getString("output"),
                                      buildResultJsonObj.getString("error"),
                                      outputStr);
        } catch (JSONException e) {
          buildResult = new RpcResult(1, "", "");
        }
        break;
      }
    }
    return buildResult;
  }

  /**
   * Check if there are any build progress available for the given user's project
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param target  build target (optional, implementation dependent)
   */
  public void updateCurrentProgress(User user, long projectId, String target) {
    try {
      String userId = user.getUserId();
      String projectName = storageIo.getProjectName(userId, projectId);
      String outputFileDir = BUILD_FOLDER + '/' + target;
      URL buildServerUrl = null;
      ProjectSourceZip zipFile = null;

      buildServerUrl = new URL(getBuildServerUrlStr(user.getUserEmail(),
        userId, projectId, outputFileDir));
      HttpURLConnection connection = (HttpURLConnection) buildServerUrl.openConnection();

      connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
          try {
            String content = readContent(connection.getInputStream());
            if (content != null && !content.isEmpty()) {
              if (DEBUG) {
                LOG.info("The current progress is " + content + "%.");
              }
              currentProgress = Integer.parseInt(content);
            }
          } catch (IOException e) {
            // No content. That's ok.
          }
         }
      } catch (MalformedURLException e) {
        // that's ok, nothing to do
      } catch (IOException e) {
        // that's ok, nothing to do
      } catch (EncryptionException e) {
        // that's ok, nothing to do
      } catch (RuntimeException e) {
        // that's ok, nothing to do
      }
  }

  // Nicely format floating number using only two decimal places
  private String format(double input) {
    DecimalFormat formatter = new DecimalFormat("###.##");
    return formatter.format(input);
  }
}
