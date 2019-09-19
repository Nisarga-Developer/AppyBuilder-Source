// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;


import com.google.appinventor.shared.rpc.help.HelpService;

/**
 * Implementation of the help service.
 *
 * <p>The help service also provides information.
 *
 */
public class HelpServiceImpl extends OdeRemoteServiceServlet implements HelpService {

  private static final long serialVersionUID = -73163124332949366L;

  @Override
  public boolean isProductionServer() {
    return Server.isProductionServer();
  }
}
