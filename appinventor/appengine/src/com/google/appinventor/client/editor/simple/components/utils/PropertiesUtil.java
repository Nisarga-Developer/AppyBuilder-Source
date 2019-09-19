// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components.utils;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.editor.youngandroid.properties.*;
import com.google.appinventor.client.properties.BadPropertyEditorException;
import com.google.appinventor.client.properties.Property;
import com.google.appinventor.client.widgets.properties.*;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidFloatRangePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidMapTypePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidIntegerRangePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidHorizontalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidGeographicPointPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidGeoJSONPropertyEditor;

import java.util.Collections;
import java.util.List;

/**
 * Provides utility functions for dealing with the UI for component properties.
 *
 * These functions are called by descendants of MockComponent with its protected
 * field "properties" as an argument. This EditableProperties object is populated
 * by {@link YoungAndroidPalettePanel#configureComponent(MockComponent)}, which
 * calls {@link MockComponent#addProperty(String, String, String, PropertyEditor)}
 * for each of the MockComponent's properties. The addProperty method in turn calls
 * {@link EditableProperties#addProperty(String, String, String, PropertyEditor, int)},
 * adding the property to "properties".
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class PropertiesUtil {

  /**
   * Prevent instantiation.
   */
  private PropertiesUtil() {
  }

  /**
   * Populate properties of a MockComponent, given a list of property definitons
   * @param mockComponent
   * @param propertyDefintions
   */
  public static void populateProperties(MockComponent mockComponent, List<ComponentDatabaseInterface.PropertyDefinition> propertyDefintions,
                                        YaFormEditor editor) {

    String componentType = mockComponent.getType();
    // Configure properties
    for (ComponentDatabaseInterface.PropertyDefinition property : propertyDefintions) {
      mockComponent.addProperty(property.getName(), property.getDefaultValue(),
          ComponentsTranslation.getPropertyName(property.getCaption()),
              PropertiesUtil.createPropertyEditor(property.getEditorType(), property.getDefaultValue(), editor));
      /*OdeLog.log("Property Caption: " + property.getCaption() + ", "
          + TranslationComponentProperty.getName(property.getCaption()));*/
    }

  }

  // Use individual methods for each property since we can't write the generic
  // getExstingPropertyEditor due to type safety issues - see below.

  /**
   * Retrieves the property editor for Horizontal Alignment.
   *
   * @param  properties
   * @return the property editor for Horizontal Alignment
   * @throws BadPropertyEditorException
   */
  public static  YoungAndroidHorizontalAlignmentChoicePropertyEditor
  getHAlignmentEditor(EditableProperties properties)
      throws BadPropertyEditorException {

    PropertyEditor propEditor = null;

    // Get the property with the specified name from the properties
    // object. This works because an EditableProperties contains a
    // TreeMap<String, T extends Property> that maps a property's name to its instance.
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_HORIZONTAL_ALIGNMENT);

    if (prop != null) {
      if (prop instanceof EditableProperty) {
        // Cast prop to an EditableProperty and get its editor
        propEditor = ((EditableProperty) prop).getEditor();

        // Make sure we have the right editor
        if (propEditor != null) {
          if (! (propEditor instanceof YoungAndroidHorizontalAlignmentChoicePropertyEditor))
            throw new BadPropertyEditorException("Bad property editor");
        }
      }
    }

    // Cast propEditor to the appropriate type
    return (YoungAndroidHorizontalAlignmentChoicePropertyEditor)propEditor;
  }

  /**
   * Retrieves the property editor for Vertical Alignment.
   * Note that this is almost identical to {@link #getHAlignmentEditor(EditableProperties)}
   * in terms of implementation.
   *
   * @param  properties
   * @return the property editor for Vertical Alignment
   * @throws BadPropertyEditorException
   */
  public static  YoungAndroidVerticalAlignmentChoicePropertyEditor
  getVAlignmentEditor(EditableProperties properties)
      throws BadPropertyEditorException {

    PropertyEditor propEditor = null;
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_VERTICAL_ALIGNMENT);
    if (prop != null) {
      if (prop instanceof EditableProperty) {
        propEditor = ((EditableProperty) prop).getEditor();
        if (propEditor != null) {
          if (! (propEditor instanceof YoungAndroidVerticalAlignmentChoicePropertyEditor))
            throw new BadPropertyEditorException("Bad property editor");
        }
      }
    }
    return (YoungAndroidVerticalAlignmentChoicePropertyEditor)propEditor;
  }

  /*
   * Creates a new property editor.
   */
  public static PropertyEditor createPropertyEditor(String editorType, String defaultValue, YaFormEditor editor) {
    if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT)) {
      return new YoungAndroidHorizontalAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT)) {
      return new YoungAndroidVerticalAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_ASSET)) {
      return new YoungAndroidAssetSelectorPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BLUETOOTHCLIENT)) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor,
          // Pass the set of component types that will be shown in the property editor,
          // in this case, just "BluetoothClient".
          Collections.singleton("BluetoothClient"));
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)) {
      return new YoungAndroidBooleanPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BUTTON_SHAPE)) {
      return new YoungAndroidButtonShapeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_COLOR)) {
      return new YoungAndroidColorChoicePropertyEditor(defaultValue);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_COMPONENT)) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_FLOAT)) {
      return new FloatPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_INTEGER)) {
      return new IntegerPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_SENSOR_PORT)) {
      return new YoungAndroidLegoEv3SensorPortChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_COLOR_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3ColorSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_ULTRASONIC_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3UltrasonicSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_GYRO_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3GyroSensorModeChoicePropertyEditor();
//    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHOICES)) {
//      return new ChoicePropertyEditor(editorArgs);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_SENSOR_PORT)) {
      return new YoungAndroidLegoNxtSensorPortChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_GENERATED_COLOR)) {
      return new YoungAndroidColorChoicePropertyEditor(
          YoungAndroidColorChoicePropertyEditor.NXT_GENERATED_COLORS, defaultValue);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT)) {
      return new NonNegativeFloatPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER)) {
      return new NonNegativeIntegerPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ORIENTATION)) {
      return new YoungAndroidScreenOrientationChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ANIMATION)) {
      return new YoungAndroidScreenAnimationChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SENSOR_DIST_INTERVAL)) {
      return new YoungAndroidSensorDistIntervalChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SENSOR_TIME_INTERVAL)) {
      return new YoungAndroidSensorTimeIntervalChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_STRING)) {
      return new StringPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT)) {
      return new YoungAndroidAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA)) {
      return new TextAreaPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TOAST_LENGTH)) {
      return new YoungAndroidToastLengthChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE)) {
      return new YoungAndroidFontTypefaceChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY)) {
      return new YoungAndroidBooleanPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_RECEIVING)) {
      return new YoungAndroidTextReceivingPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SORT_OPTIONS)) {
        return new YoungAndroidSortOrderPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_GENDER_OPTIONS)) {
        return new YoungAndroidGenderPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MIN_API_LEVEL_OPTIONS)) {
        return new YoungAndroidMinApiLevelPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MAX_API_LEVEL_OPTIONS)) {
        return new YoungAndroidMaxApiLevelPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_PICKER_ITEM_OPTIONS)) {
        return new YoungAndroidListPickerItemChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_PICKER_STYLE_OPTIONS)) {
        return new YoungAndroidListPickerStylePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_ITEM_OPTIONS)) {
        return new YoungAndroidListViewItemChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_PROGRESSBAR_OPTIONS)) {
        return new YoungAndroidProgressBarPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_ACCELEROMETER_SENSITIVITY)) {
      return new YoungAndroidAccelerometerSensitivityChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_TO_SPEECH_COUNTRIES)) {
      return new CountryChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_TO_SPEECH_LANGUAGES)) {
      return new LanguageChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SIZING)) {
      return new YoungAndroidSizingChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCALING)) {
      return new ScalingChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_FIREBASE_URL)) {
      return new YoungAndroidDefaultURLPropertyEditor("DEFAULT");
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_GEOGRAPHIC_POINT)) {
      return new YoungAndroidGeographicPointPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_GEOJSON_TYPE)) {
      return new YoungAndroidGeoJSONPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LATITUDE)) {
      return new YoungAndroidFloatRangePropertyEditor(-90, 90);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LONGITUDE)) {
      return new YoungAndroidFloatRangePropertyEditor(-180, 180);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MAP_TYPE)) {
      return new YoungAndroidMapTypePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MAP_ZOOM)) {
      return new YoungAndroidIntegerRangePropertyEditor(1, 18);
    } else if (editorType.startsWith(PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":")) {
      String type = editorType.substring(PropertyTypeConstants.PROPERTY_TYPE_COMPONENT.length() + 2);
      type = type.substring(type.lastIndexOf('.') + 1);
      return new YoungAndroidComponentSelectorPropertyEditor(editor, Collections.singleton(type));
    } else {
      return new TextPropertyEditor();
    }
  }




  // TODO(hal):  The individual property methods above should be replaced by a single generic
  // method getExistingPropertyEditor that
  // retrieves the property editor for the named property and checks its type.  Here's how the
  // code should go.  While this compiles in Java, it sadly
  // does not compile in GWT, which has not yet implemented type-safe generic casting.
  // Sigh.  Well, maybe someday.

// /**
//  * Returns the existing property editor for the given name.
//  *
//  * @throws IllegalStateException  if no such property editor exists, or if
//  * the editor does not have the required class
//  * @param name  property name
//  * @param C the required class of class of the property editor
//  */

// public <T extends PropertyEditor>T getExistingPropertyEditor(String name, Class<T> C)
//     throws BadPropertyEditorException {
//   PropertyEditor propEditor= null;
//   Property prop = getProperty(name);
//   if (! (prop == null)) {
//     if (prop instanceof EditableProperty) {
//       propEditor = ((EditableProperty) prop).getEditor();
//       if (! (propEditor == null)) {
//         if (! (C.isAssignableFrom(propEditor.getClass()))) {
//           throw new BadPropertyEditorException("Bad property editor");
//         }
//       }
//     }
//   }
//   return C.cast(propEditor);
// }


}
