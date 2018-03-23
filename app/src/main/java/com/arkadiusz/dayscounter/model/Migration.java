package com.arkadiusz.dayscounter.model;

import android.util.Log;
import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Arkadiusz on 31.05.2017
 */

public class Migration implements RealmMigration {

  @Override
  public int hashCode() {
    return 37;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Migration);
  }

  @Override
  public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

    Log.d("migrationOldVersion", String.valueOf(oldVersion));
    Log.d("migrationNewVersion", String.valueOf(newVersion));

    if (oldVersion == 0) {
      RealmSchema schema = realm.getSchema();
      RealmObjectSchema eventSchema = schema.get("Event");
      eventSchema.addField("hasAlarm", boolean.class);
      eventSchema.addField("year", int.class);
      eventSchema.addField("month", int.class);
      eventSchema.addField("day", int.class);
      eventSchema.addField("hour", int.class);
      eventSchema.addField("minute", int.class);
      eventSchema.addField("notificationText", String.class);
      eventSchema.addField("repeat", String.class);

      eventSchema.transform(obj -> {
        obj.set("repeat", "0");
        obj.set("hasAlarm", false);
      });
    }

    if (oldVersion == 2) {
      RealmSchema schema = realm.getSchema();
      RealmObjectSchema eventSchema = schema.get("Event");

      eventSchema.removeField("isOnlyDays");
      eventSchema.removeField("color");

      eventSchema.renameField("year", "reminderYear");
      eventSchema.renameField("month", "reminderMonth");
      eventSchema.renameField("day", "reminderDay");
      eventSchema.renameField("hour", "reminderHour");
      eventSchema.renameField("minute", "reminderMinute");

      eventSchema.addField("formatYearsSelected", boolean.class);
      eventSchema.addField("formatMonthsSelected", boolean.class);
      eventSchema.addField("formatWeeksSelected", boolean.class);
      eventSchema.addField("formatDaysSelected", boolean.class);
      eventSchema.addField("isLineDividerSelected", boolean.class);
      eventSchema.addField("counterFontSize", int.class);
      eventSchema.addField("titleFontSize", int.class);
      eventSchema.addField("fontType", int.class);
      eventSchema.addField("fontColor", int.class);
      eventSchema.addField("pictureDim", int.class);

      eventSchema.transform(obj -> {
        obj.set("formatYearsSelected", false);
        obj.set("formatMonthsSelected", false);
        obj.set("formatWeeksSelected", false);
        obj.set("formatDaysSelected", true);
        obj.set("isLineDividerSelected", true);
        obj.set("counterFontSize", 30);
        obj.set("titleFontSize", 20);
        obj.set("fontType", "Roboto");
        obj.set("fontColor", -1);
        obj.set("pictureDim", 60);
      });
    }
  }

}
