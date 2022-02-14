package com.arkadiusz.dayscounter.data.local

import com.google.firebase.firestore.FirebaseFirestore
import io.realm.DynamicRealm
import io.realm.RealmMigration

class Migration : RealmMigration {

    val firebase = FirebaseFirestore.getInstance()

    override fun hashCode(): Int {
        return 37
    }

    override fun equals(other: Any?): Boolean {
        return other is Migration
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        when (oldVersion) {
            0L -> migrateFromFirstVersion(realm)
            2L -> migrateFromThirdVersion(realm)
            3L -> migrateFromFourthVersion(realm)
            4L -> migrateFromFifthVersion(realm)
        }
    }

    private fun migrateFromFirstVersion(realm: DynamicRealm) {
        val schema = realm.schema
        val eventSchema = schema.get("Event")!!
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
        val eventSchema = schema.get("Event")!!

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
        eventSchema.addField("lineDividerSelected", Boolean::class.javaPrimitiveType)
        eventSchema.addField("hasTransparentWidget", Boolean::class.javaPrimitiveType)
        eventSchema.addField("counterFontSize", Int::class.javaPrimitiveType)
        eventSchema.addField("titleFontSize", Int::class.javaPrimitiveType)
        eventSchema.addField("fontType", String::class.java)
        eventSchema.addField("fontColor", Int::class.javaPrimitiveType)
        eventSchema.addField("pictureDim", Int::class.javaPrimitiveType)
        eventSchema.addField("imageColor", Int::class.javaPrimitiveType)

        eventSchema.transform { obj ->
            obj.set("formatYearsSelected", false)
            obj.set("formatMonthsSelected", false)
            obj.set("formatWeeksSelected", false)
            obj.set("formatDaysSelected", true)
            obj.set("lineDividerSelected", true)
            obj.set("hasTransparentWidget", false)
            obj.set("counterFontSize", 30)
            obj.set("titleFontSize", 20)
            obj.set("fontType", "Josefin Sans")
            obj.set("fontColor", -1)
            obj.set("pictureDim", 4)
            obj.set("imageColor", 0)
            if (obj.get("imageID") as Long != 0L) {
                val newImageID = (obj.get("imageID") as Long + 393222).toInt()
                obj.set("imageID", newImageID)
            }
        }
    }

    private fun migrateFromFourthVersion(realm: DynamicRealm) {
        val schema = realm.schema
        val eventSchema = schema.get("Event")!!

        eventSchema
            .removePrimaryKey()
            .removeField("id")
            .addField("id", String::class.java)
            .addField("imageCloudPath", String::class.java)
            .transform {
                val newId = firebase.collection("getId").document().id
                it.set("id", newId)
                it.set("imageCloudPath", "")
            }
            .addPrimaryKey("id")
    }

    private fun migrateFromFifthVersion(realm: DynamicRealm) {
        realm.schema.get("Event")!!
            .removeField("hasAlarm")
    }
}
