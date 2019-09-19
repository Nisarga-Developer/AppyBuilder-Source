// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for choosing an sensor port for a sensor on a Lego Mindstorms
 * NXT robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidLegoNxtSensorPortChoicePropertyEditor extends ChoicePropertyEditor {

  // Lego Mindstorms NXT sensor port choices
  private static final Choice[] sensorPorts = new Choice[] {
    new Choice("1", "1"),
    new Choice("2", "2"),
    new Choice("3", "3"),
    new Choice("4", "4")
  };

  public YoungAndroidLegoNxtSensorPortChoicePropertyEditor() {
    super(sensorPorts);
  }
}
