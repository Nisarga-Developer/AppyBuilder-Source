// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for screen orientation
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidScreenOrientationChoicePropertyEditor extends ChoicePropertyEditor {

  // Screen orientation choices
  private static final Choice[] screenOrientationChoices = new Choice[] {
    // To avoid confusion, we only show a subset of the available screen orientation values.
    new Choice(MESSAGES.unspecifiedScreenOrientation(), "unspecified"),
    new Choice(MESSAGES.portraitScreenOrientation(), "portrait"),
    new Choice(MESSAGES.landscapeScreenOrientation(), "landscape"),
    new Choice(MESSAGES.sensorScreenOrientation(), "sensor"),
    new Choice(MESSAGES.userScreenOrientation(), "user"),
  };

  public YoungAndroidScreenOrientationChoicePropertyEditor() {
    super(screenOrientationChoices);
  }
}
