// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The top panel, which contains the main menu, various links plus ads.
 *
 */
public class TopPanel extends Composite {
  // Strings for links and dropdown menus:
  private final DropDownButton accountButton;
  public DropDownButton languageDropDown;

  private final String WIDGET_NAME_MESSAGES = "Messages";
  private final String WIDGET_NAME_PRIVATE_USER_PROFILE = "Profile";
  private final TextButton gallery;
  private final TextButton moderation;
  private final String WIDGET_NAME_SIGN_OUT = "Signout";
  private final String WIDGET_NAME_USER = "User";
  private static final String WIDGET_NAME_LANGUAGE = "Language";

  private static final String SIGNOUT_URL = "/ode/_logout";
  private static final String LOGO_IMAGE_URL = "/images/logo.png";
  private static final String LANGUAGES_IMAGE_URL = "/images/languages.svg";
  private static final String FACEBOOK_IMAGE_URL = "/images/facebook.png";
  private static final String DONATE_IMAGE_URL = "/images/donate.png";
  private static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  private static final String WINDOW_OPEN_LOCATION = "_ai2";

  private final VerticalPanel rightPanel;  // remember this so we can add MOTD later if needed

  final Ode ode = Ode.getInstance();

  /**
   * Initializes and assembles all UI elements shown in the top panel.
   */
  public TopPanel() {
    /*
     * The layout of the top panel is as follows:
     *
     *  +-- topPanel ------------------------------------+
     *  |+-- logo --++-----tools-----++--links/account--+|
     *  ||          ||               ||                 ||
     *  |+----------++---------------++-----------------+|
     *  +------------------------------------------------+
     */
    HorizontalPanel topPanel = new HorizontalPanel();
    topPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    // Create the Tools
    TopToolbar tools = new TopToolbar();
    ode.setTopToolbar(tools);

    // Create the Links
    HorizontalPanel links = new HorizontalPanel();
    links.setStyleName("ode-TopPanelLinks");
    links.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    if (Ode.getInstance().isReadOnly()) {
      Label readOnly = new Label(MESSAGES.readOnlyMode());
      readOnly.setStyleName("ode-TopPanelWarningLabel");
      links.add(readOnly);
    }

    // Facebook link xxxxxxxxxx
/*    TextButton donateLink = new TextButton("Donate");
    donateLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        Window.open("https://goo.gl/HcrNVa", "donate", "menubar=yes,location=yes,resizable=no,scrollbars=no,status=no");
      }
    });
    donateLink.setStyleName("ode-TopPanelButton");
    links.add(donateLink);*/

    // ======= My Projects Link image and link
    Image projectsLogo = new Image("/images/projects.png");
    projectsLogo.setTitle("My Projects");
    projectsLogo.setSize("23px", "23px");
    projectsLogo.setStyleName("ode-Language");
    projectsLogo.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ode.switchToProjectsView();
      }
    });
    links.add(projectsLogo);
    TextButton myProjects = new TextButton(MESSAGES.myProjectsTabName());
    myProjects.setStyleName("ode-TopPanelButton");

    myProjects.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ode.switchToProjectsView();
      }
    });

    myProjects.setStyleName("ode-TopPanelButton");
    links.add(myProjects);
    // =======================

    Config config = ode.getSystemConfig();
    final String guideUrl = config.getGuideUrl();

    // Community link
    Image guideLogo = new Image("/images/guide.png");
    guideLogo.setTitle("Help Docs");
    guideLogo.setSize("23px", "23px");
    guideLogo.setStyleName("ode-Language");
    guideLogo.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
//        Window.open("http://AppyBuilder.com", "_ai2", "scrollbars=1");
        Window.open(guideUrl, "help", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
      }
    });
    links.add(guideLogo);

    // Community link
    Image communityLogo = new Image("/images/community.png");
    communityLogo.setTitle("Community");
    communityLogo.setSize("23px", "23px");
    communityLogo.setStyleName("ode-Language");
    final String communityUrl = "http://Community.AppyBuilder.com";
    communityLogo.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
//        Window.open("http://AppyBuilder.com", "_ai2", "scrollbars=1");
        Window.open(communityUrl, "community", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
      }
    });

//    TextButton communityLink = new TextButton("Community");
//    communityLink.addClickHandler(new ClickHandler() {
//      @Override
//      public void onClick(ClickEvent clickEvent) {
////        Window.open("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=BD8V7EWSFR6CA", "donate", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
//        Window.open("http://Community.AppyBuilder.com", "comminity", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
//      }
//    });
//    communityLink.setStyleName("ode-TopPanelButton");
    links.add(communityLogo);


    // Code on gallerydev branch
    // Gallery Link
    gallery = new TextButton(MESSAGES.tabNameGallery());
    gallery.setStyleName("ode-TopPanelButton");
    gallery.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ode.switchToGalleryView();
      }
    });
    links.add(gallery);

//    Config config = ode.getSystemConfig();
//    String guideUrl = config.getGuideUrl();
//    if (!Strings.isNullOrEmpty(guideUrl)) {
//      TextButton guideLink = new TextButton(MESSAGES.guideTabName());
//      guideLink.addClickHandler(new WindowOpenClickHandler(guideUrl));
//      guideLink.setStyleName("ode-TopPanelButton");
//      links.add(guideLink);
//    }

    // Feedback Link
    /*String feedbackUrl = config.getFeedbackUrl();
    if (!Strings.isNullOrEmpty(feedbackUrl)) {
      TextButton feedbackLink = new TextButton(MESSAGES.feedbackTabName());
      feedbackLink.addClickHandler(
        new WindowOpenClickHandler(feedbackUrl));
      feedbackLink.setStyleName("ode-TopPanelButton");
      links.add(feedbackLink);
    }*/

    // Feedback Link
    /*TextButton feedbackLink = new TextButton(MESSAGES.feedbackLink());
    feedbackLink.setStyleName("ode-TopPanelButton");

    feedbackLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Window.open("http://AppyBuilder.com/#contact", "_blank", null);
      }
    });
    feedbackLink.setStyleName("ode-TopPanelButton");
    links.add(feedbackLink);*/

  /*
  // Code on master branch
    // Gallery Link
    if (Ode.getInstance().getUser().getIsAdmin()) {
      TextButton gallery = new TextButton(MESSAGES.galleryTabName());
      gallery.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          Window.open("http://gallery.appinventor.mit.edu", "_blank", "scrollbars=1");
        }
      });

      gallery.setStyleName("ode-TopPanelButton");
      links.add(gallery);
    }
    */

    moderation = new TextButton(MESSAGES.tabNameModeration());
    moderation.setStyleName("ode-TopPanelButton");
    moderation.addClickHandler(new ClickHandler() {
    @Override
      public void onClick(ClickEvent clickEvent) {
        ode.switchToModerationPageView();
      }
    });
    moderation.setVisible(false);
    links.add(moderation);

    // Create the Account Information
    rightPanel = new VerticalPanel();
    rightPanel.setHeight("100%");
    rightPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

    HorizontalPanel account = new HorizontalPanel();
    account.setStyleName("ode-TopPanelAccount");

    // Account Drop Down Button
    List<DropDownItem> userItems = Lists.newArrayList();

    // Sign Out
    userItems.add(new DropDownItem(WIDGET_NAME_SIGN_OUT, MESSAGES.signOutLink(), new SignOutAction()));

    accountButton = new DropDownButton(WIDGET_NAME_USER, " " , userItems, true);
    accountButton.setItemEnabled(WIDGET_NAME_MESSAGES, false);
    accountButton.setStyleName("ode-TopPanelButton");

    // Language
    List<DropDownItem> languageItems = Lists.newArrayList();
    String[] localeNames = LocaleInfo.getAvailableLocaleNames();
    String nativeName;
    for (String localeName : localeNames) {
      if (!localeName.equals("default")) {
        SelectLanguage lang = new SelectLanguage();
        lang.setLocale(localeName);
        nativeName = getDisplayName(localeName, "both");

        languageItems.add(new DropDownItem(WIDGET_NAME_LANGUAGE, nativeName, lang));
      }
    }
    String currentLang = LocaleInfo.getCurrentLocale().getLocaleName();
    String nativeDisplayName = getDisplayName(currentLang, "language");

//    TextButton flag = new TextButton(getDisplayName(currentLang, true));
//    flag.setStyleName("ode-TopPanelButton");
//    links.add(flag);

//    if (nativeDisplayName.toLowerCase().equals("english")) {
//      nativeDisplayName = "EN English";
//    }
//    OdeLog.log("nativeDisplayName is:" + nativeDisplayName);

    Image flagLogo = new Image( getDisplayName(currentLang, "flag"));
    flagLogo.setSize("21px", "14px");
    // don't set any class because we don't want it to be highlighted when hovered
    // flagLogo.setStyleName("ode-Logo");

    HorizontalPanel hrFlagAndLocal = new HorizontalPanel();
    hrFlagAndLocal.setStyleName("ode-Language");  //sets left spacing to keep space from other items
    hrFlagAndLocal.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

    languageDropDown = new DropDownButton(WIDGET_NAME_LANGUAGE, nativeDisplayName, languageItems, true);
    languageDropDown.setStyleName("ode-TopPanelButton");

//    addFlag(account, "US");

    account.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    account.add(links);


    hrFlagAndLocal.add(flagLogo);
    hrFlagAndLocal.add(languageDropDown);

    account.add(hrFlagAndLocal);
    account.add(accountButton);

    rightPanel.add(account);

    // Add the Logo, Tools, Links to the TopPanel
    addLogo(topPanel);
    topPanel.add(tools);
    topPanel.add(rightPanel);
    topPanel.setCellVerticalAlignment(rightPanel, HorizontalPanel.ALIGN_MIDDLE);
    rightPanel.setCellHorizontalAlignment(account, HorizontalPanel.ALIGN_RIGHT);
    topPanel.setCellHorizontalAlignment(rightPanel, HorizontalPanel.ALIGN_RIGHT);

    initWidget(topPanel);

    setStyleName("ode-TopPanel");
    setWidth("100%");
  }

  // todo: later change type to enum
  private String getDisplayName(String localeName, String type){
    String nativeName=LocaleInfo.getLocaleNativeDisplayName(localeName);
    String flag="defaultFlag.png"; //some default flag; in case we add new language and haven't captured the flag yet
    if (localeName.equals("zh_CN")) {
      nativeName = MESSAGES.SwitchToSimplifiedChinese();
      flag = "zh";
    } else if (localeName.equals("zh_TW")) {
      nativeName = MESSAGES.SwitchToTraditionalChinese();
      flag = "zh";
    } else if (localeName.equals("es_ES")) {
      nativeName = MESSAGES.SwitchToSpanish();
      flag = "es";
    } else if (localeName.equals("fr_FR")) {
      nativeName = MESSAGES.SwitchToFrench();
      flag = "fr";
    } else if (localeName.equals("it_IT")) {
      nativeName = MESSAGES.SwitchToItalian();
      flag = "it";
    } else if (localeName.equals("ru")) {
      nativeName = MESSAGES.SwitchToRussian();
      flag = "ru";
    } else if (localeName.equals("ko_KR")) {
      nativeName = MESSAGES.SwitchToKorean();
      flag = "ko";
    } else if (localeName.equals("sv")) {
      nativeName = MESSAGES.SwitchToSwedish();
      flag = "sv";
    } else if (localeName.equals("pt_BR")) {
      nativeName = MESSAGES.switchToPortugueseBR();
      flag = "br";
    } else if (localeName.equals("pt")) {
      nativeName = MESSAGES.switchToPortuguese();
      flag = "pt";
    } else if (localeName.equals("nl")) {
      nativeName = MESSAGES.switchToDutch();
      flag = "nl";
    } else if (localeName.equals("en")) {
      nativeName = MESSAGES.SwitchToEnglish();
      flag = "en";
    }
    flag = flag + ".png";
    // https://stackoverflow.com/questions/14276715/gwt-drop-down-menu-with-images
    final String image = "<img src=\"/images/" + flag + "\" height=\"14px\" width=\"22px\"/>";
    /*SafeHtml addActivityImagePath = new SafeHtml() {
      @Override
      public String asString() {
        return image;
      }
    };*/

    if (type.equals("both")) return image + " " + nativeName;
    else if (type.equals("language")) return nativeName;
    else if (type.equals("flag")) return "/images/" + flag; // widget image will be used. This is why we only pass pure image link
    else return nativeName;
  }

  public void updateAccountMessageButton(){
    // Since we want to insert "Messages" before "Sign Out", we need to clear first.
    accountButton.clearAllItems();

    // Gallery Items
    // (1)Private User Profile
    accountButton.addItem(new DropDownItem(WIDGET_NAME_PRIVATE_USER_PROFILE, MESSAGES.privateProfileLink(), new PrivateProfileAction()));
    // (2)Sign Out
    accountButton.addItem(new DropDownItem(WIDGET_NAME_SIGN_OUT, MESSAGES.signOutLink(), new SignOutAction()));
  }

/*  private Image addFlag(HorizontalPanel panel, String localeName) {
    String image = getDisplayName(localeName, false);
    Image logo = new Image( "/imgage/fr.png");
    logo.setSize("21px", "14px");
    logo.setStyleName("ode-Logo");
    return logo;
  }*/

  private void addLogo(HorizontalPanel panel) {
    // Logo should be a link to App Inv homepage. Currently, after the user
    // has logged in, the top level *is* ODE; so for now don't make it a link.
    // Add timestamp to logo url to get around browsers that agressively cache
    // the image! This same trick is used in StorageUtil.getFilePath().
    Image logo = new Image(LOGO_IMAGE_URL + "?t=" + System.currentTimeMillis());
    logo.setSize("191px", "40px");
    logo.setStyleName("ode-Logo");
    String logoUrl = ode.getSystemConfig().getLogoUrl();
    if (!Strings.isNullOrEmpty(logoUrl)) {
      logo.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
//        Window.open("http://AppyBuilder.com", "_ai2", "scrollbars=1");
          Window.open("http://AppyBuilder.com", "appybuilder", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
        }
      });
    }
    panel.add(logo);
    panel.setCellWidth(logo, "50px");
/*
    Label title = new Label("AppyBuilder");
    Label version = new Label("Beta");
    title.setStyleName("ode-LogoText");
    version.setStyleName("ode-LogoVersion");
    VerticalPanel titleContainer = new VerticalPanel();
    titleContainer.add(title);
    titleContainer.add(version);
    titleContainer.setCellHorizontalAlignment(version, HorizontalPanel.ALIGN_RIGHT);
    panel.add(titleContainer);
    panel.setCellWidth(titleContainer, "180px");
*/
    panel.setCellWidth(logo, "228px");
    panel.setCellHorizontalAlignment(logo, HorizontalPanel.ALIGN_LEFT);
    panel.setCellVerticalAlignment(logo, HorizontalPanel.ALIGN_MIDDLE);
    panel.setCellWidth(logo, "228px");
  }

  private void addFBButton(HorizontalPanel panel) {
    Image image = new Image(FACEBOOK_IMAGE_URL + "?t=" + System.currentTimeMillis());
    image.setSize("27px", "40px");
    image.setStyleName("ode-Logo");
    image.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        Window.open("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=BD8V7EWSFR6CA", "fb", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes");
      }
    });
    panel.add(image);
    panel.setCellWidth(image, "27px");
    panel.setCellHorizontalAlignment(image, HorizontalPanel.ALIGN_LEFT);
    panel.setCellVerticalAlignment(image, HorizontalPanel.ALIGN_MIDDLE);
  }

  private void addDonateButton2(HorizontalPanel panel) {
    Image image = new Image(DONATE_IMAGE_URL + "?t=" + System.currentTimeMillis());

    image.setSize("27px", "27px");
    image.setStyleName("ode-Logo");
    image.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        Window.open("https://goo.gl/eKK5et", "donate", "menubar=yes,location=yes,resizable=no,scrollbars=no,status=no");
      }
    });
    panel.add(image);
    panel.setCellWidth(image, "27px");
    panel.setCellHorizontalAlignment(image, HorizontalPanel.ALIGN_LEFT);
    panel.setCellVerticalAlignment(image, HorizontalPanel.ALIGN_MIDDLE);
    panel.setCellWidth(image, "27px");
  }


  private void addMotd(VerticalPanel panel) {
    MotdBox motdBox = MotdBox.getMotdBox();
    panel.add(motdBox);
    panel.setCellHorizontalAlignment(motdBox, HorizontalPanel.ALIGN_RIGHT);
    panel.setCellVerticalAlignment(motdBox, HorizontalPanel.ALIGN_BOTTOM);
  }

  /**
   * Updates the UI to show the user's email address.
   *
   * @param email the email address
   */
  public void showUserEmail(String email) {
    accountButton.setCaption(email);
  }

  /**
   * Updates the UI to show the moderation's link.
   */
  public void showModerationLink(boolean b) {
    moderation.setVisible(b);
  }

  /**
   * Updates the UI to show the moderation's link.
   */
  public void showGalleryLink(boolean b) {
    gallery.setVisible(b);
  }

  /**
   * Adds the MOTD box to the right panel. This should only be called once.
   */
  public void showMotd() {
    addMotd(rightPanel);
  }

  private static class WindowOpenClickHandler implements ClickHandler {
    private final String url;

    WindowOpenClickHandler(String url) {
      this.url = url;
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
      Window.open(url, WINDOW_OPEN_LOCATION, WINDOW_OPEN_FEATURES);
    }
  }

  private static class SignOutAction implements Command {
    @Override
    public void execute() {
      // Maybe take a screenshot
      Ode.getInstance().screenShotMaybe(new Runnable() {
          @Override
          public void run() {
            Window.Location.replace(SIGNOUT_URL);
          }
        }, true);               // Wait for i/o
    }
  }

  private class SelectLanguage implements Command {

    private String localeName;

    @Override
    public void execute() {
      final String queryParam = LocaleInfo.getLocaleQueryParam();
      Command savecmd = new SaveAction();
      savecmd.execute();
      if (queryParam != null) {
        UrlBuilder builder = Window.Location.createUrlBuilder().setParameter(
            queryParam, localeName);
        Window.Location.replace(builder.buildString());
      } else {
        // If we are using only cookies, just reload
        Window.Location.reload();
      }
    }

    public void setLocale(String nativeName) {
      localeName = nativeName;
    }

  }

  private class SaveAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(null);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, projectRootNode);
      }
    }
  }

  private static class PrivateProfileAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().switchToPrivateUserProfileView();
    }
  }
}

