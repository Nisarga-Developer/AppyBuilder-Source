// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.encryption;

/**
 * Pass-through encryptor.
 *
 */
class NopEncryptor implements Encryptor {
  /**
   * {@inheritDoc}
   *
   * <p>Returns the received array.
   */
  @Override
  public byte[] decrypt(byte[] encrypted) {
    return encrypted;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the received array.
   */
  @Override
  public byte[] encrypt(byte[] plain) {
    return plain;
  }
}
