package com.arkadiusz.dayscounter.model

import io.realm.DynamicRealm
import io.realm.RealmMigration

/**
 * Created by Arkadiusz on 31.05.2017
 */

class Migration : RealmMigration {

    override fun hashCode(): Int {
        return 37
    }

    override fun equals(other: Any?): Boolean {
        return other is Migration
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {

        if (oldVersion == 0L) {
            migrateFromFirstVersion(realm)
        }

        if (oldVersion == 2L) {
            migrateFromThirdVersion(realm)
        }
    }

    private fun migrateFromFirstVersion(realm: DynamicRealm) {
        val schema = realm.schema
        val eventSchema = schema.get("Event")
        eventSchema.addField("hasAlarm", Boolean::class.javaPrimitiveType)
        eventSchema.addField("year", Int::class.javaPrimitiveType)
        eventSchema.addField("month", Int::class.javaPrimitiveType)
        eventSchema.addField("day", Int::class.javaPrimitiveType)
        eventSchema.addField("hour", Int::class.javaPrimitiveType)
        eventSchema.addField("minute", Int::class.javaPrimitiveType)
        eventSchema.addField("notificationText", String::class.java)
        eventSchema.addField("repeat", String::class.java)

        eventSchema.transform { obj ->
            obj.set("repeat", "0")
            obj.set("hasAlarm", false)
        }
    }

    private fun migrateFromThirdVersion(realm: DynamicRealm) {
        val schema = realm.schema
        val eventSchema = schema.get("Event")

        eventSchema.removeField("isOnlyDays")
        eventSchema.removeField("color")

        eventSchema.renameField("year", "reminderYear")
        eventSchema.renameField("month", "reminderMonth")
        eventSchema.renameField("day", "reminderDay")
        eventSchema.renameField("hour", "reminderHour")
        eventSchema.renameField("minute", "reminderMinute")

        eventSchema.addField("formatYearsSelected", Boolean::class.javaPrimitiveType)
        eventSchema.addField("formatMonthsSelected", Boolean::class.javaPrimitiveType)
        eventSchema.addField("formatWeeksSelected", Boolean::class.javaPrimitiveType)
        eventSchema.addField("formatDaysSelected", Boolean::class.javaPrimitiveType)
        eventSchema.addField("isLineDividerSelected", Boolean::class.javaPrimitiveType)
        eventSchema.addField("hasTransparentWidget", Boolean::class.javaPrimitiveType)
        eventSchema.addField("counterFontSize", Int::class.javaPrimitiveType)
        eventSchema.addField("titleFontSize", Int::class.javaPrimitiveType)
        eventSchema.addField("fontType", Int::class.javaPrimitiveType)
        eventSchema.addField("fontColor", Int::class.javaPrimitiveType)
        eventSchema.addField("pictureDim", Int::class.javaPrimitiveType)

        eventSchema.transform { obj ->
            obj.set("formatYearsSelected", false)
            obj.set("formatMonthsSelected", false)
            obj.set("formatWeeksSelected", false)
            obj.set("formatDaysSelected", true)
            obj.set("isLineDividerSelected", true)
            obj.set("hasTransparentWidget", false)
            obj.set("counterFontSize", 30)
            obj.set("titleFontSize", 20)
            obj.set("fontType", "Roboto")
            obj.set("fontColor", -1)
            obj.set("pictureDim", 60)
        }
    }

}
