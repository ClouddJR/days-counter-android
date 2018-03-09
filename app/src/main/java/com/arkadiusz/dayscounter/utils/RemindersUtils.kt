package com.arkadiusz.dayscounter.utils

/**
 * Created by arkadiusz on 04.03.18
 */

object RemindersUtils {

//    fun addNewReminder(context: Context, reminder: Event): Boolean {
//        val alarmManager = context.alarmManager
//        val bundle = prepareBundle(reminder)
//        val intent = prepareIntent(context, bundle)
//        val alarmIntent = PendingIntent.getBroadcast(context, reminder.key.hashCode(), intent, 0)
//
//        if (wasSingleEventInThePast(reminder)) {
//            return false
//        }
//
//        val calendar = generateCalendar()
//        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
//        return true
//    }
//
//    fun deleteReminder(context: Context, reminder: Event) {
//        val alarmManager = context.alarmManager
//        val intent = Intent(context, ReminderReceiver::class.java)
//        val alarmIntent = PendingIntent.getBroadcast(context, reminder.key.hashCode(), intent, 0)
//
//        alarmManager.cancel(alarmIntent)
//    }
//
//    private fun prepareBundle(reminder: Event): Bundle {
//        val bundle = Bundle()
//        bundle.putParcelable("reminderObject", reminder as Parcelable)
//        return bundle
//    }
//
//    private fun prepareIntent(context: Context, bundle: Bundle): Intent {
//        val intent = Intent(context, ReminderReceiver::class.java)
//        intent.putExtra("bundleData", bundle)
//        return intent
//    }
//
//    private fun wasSingleEventInThePast(reminder: Event): Boolean {
//        val reminderCalendar = generateCalendar(reminder)
//        val todayCalendar = generateTodayCalendar()
//
//        return (reminderCalendar.before(todayCalendar) && reminder.numberIntervals == 0)
//    }
//
//    private fun generateCalendar(reminder: Event): Calendar {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.YEAR, reminder.year)
//        calendar.set(Calendar.MONTH, reminder.month)
//        calendar.set(Calendar.DAY_OF_MONTH, reminder.day)
//        calendar.set(Calendar.HOUR_OF_DAY, reminder.hour)
//        calendar.set(Calendar.MINUTE, reminder.minute)
//        calendar.set(Calendar.SECOND, 59)
//        return calendar
//    }
}