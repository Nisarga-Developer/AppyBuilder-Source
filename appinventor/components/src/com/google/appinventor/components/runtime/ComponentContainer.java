// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;

/**
 * Components that can contain other components need to implement this
 * interface.
 *
 */
public interface ComponentContainer {
  /**
   * Returns the activity context (which can be retrieved from the root
   * container - aka the form).
   *
   * @return  activity context
   */
  Activity $context();

  /**
   * Returns the form that ultimately contains this container.
   *
   * @return  form
   */
  Form $form();

  /**
   * Adds a component to a container.
   *
   * <p/>After this method is finished executing, the given component's view
   * must have LayoutParams, even if the component cannot be added to the
   * container until later.
   *
   * @param component  component associated with view
   */
  void $add(AndroidViewComponent component);

  void setChildWidth(AndroidViewComponent component, int width);

  void setChildHeight(AndroidViewComponent component, int height);

  int Width();

  int Height();

}
