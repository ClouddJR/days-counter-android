package com.arkadiusz.dayscounter.Model;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmObjectSchema.Function;
import io.realm.RealmSchema;

/**
 * Created by Arkadiusz on 31.05.2017.
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

      eventSchema.transform(new Function() {
        @Override
        public void apply(DynamicRealmObject obj) {
          obj.set("repeat", "0");
          obj.set("hasAlarm", false);
        }
      });

      oldVersion++;
    }
  }

}
