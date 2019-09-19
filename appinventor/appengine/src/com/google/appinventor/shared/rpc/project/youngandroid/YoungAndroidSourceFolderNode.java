// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.SourceFolderNode;

/**
 * Young Android source folder node in project tree.
 *
 */
public final class YoungAndroidSourceFolderNode extends SourceFolderNode {

  // For serialization
  private static final long serialVersionUID = -1036164488317207599L;

  /**
   * Serialization constructor.
   */
  public YoungAndroidSourceFolderNode() {
    super(null, null);
  }

  /**
   * Creates a new source folder node.
   */
  public YoungAndroidSourceFolderNode(String fileId) {
    super("Sources", fileId);
  }
}
