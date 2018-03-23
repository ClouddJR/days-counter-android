package com.arkadiusz.dayscounter.database;

import com.google.firebase.database.IgnoreExtraProperties;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@IgnoreExtraProperties
public class Event extends RealmObject {

  @PrimaryKey
  private int id;
  private String name;
  private String date;
  private String description;
  private String image;
  private int imageID;
  private String type;
  private String repeat;
  private int widgetID;
  private boolean hasAlarm;
  private int reminderYear;
  private int reminderMonth;
  private int reminderDay;
  private int reminderHour;
  private int reminderMinute;
  private String notificationText;
  private Boolean formatYearsSelected;
  private Boolean formatMonthsSelected;
  private Boolean formatWeeksSelected;
  private Boolean formatDaysSelected;
  private Boolean isLineDividerSelected;
  private int counterFontSize;
  private int titleFontSize;
  private String fontType;
  private int fontColor;
  private int pictureDim;

  public Event() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getImageID() {
    return imageID;
  }

  public void setImageID(int imageID) {
    this.imageID = imageID;
  }

  public int getWidgetID() {
    return widgetID;
  }

  public void setWidgetID(int widgetID) {
    this.widgetID = widgetID;
  }

  public boolean isHasAlarm() {
    return hasAlarm;
  }

  public void setHasAlarm(boolean hasAlarm) {
    this.hasAlarm = hasAlarm;
  }

  public int getReminderYear() {
    return reminderYear;
  }

  public void setReminderYear(int reminderYear) {
    this.reminderYear = reminderYear;
  }

  public int getReminderMonth() {
    return reminderMonth;
  }

  public void setReminderMonth(int reminderMonth) {
    this.reminderMonth = reminderMonth;
  }

  public int getReminderDay() {
    return reminderDay;
  }

  public void setReminderDay(int reminderDay) {
    this.reminderDay = reminderDay;
  }

  public int getReminderHour() {
    return reminderHour;
  }

  public void setReminderHour(int reminderHour) {
    this.reminderHour = reminderHour;
  }

  public int getReminderMinute() {
    return reminderMinute;
  }

  public void setReminderMinute(int reminderMinute) {
    this.reminderMinute = reminderMinute;
  }

  public String getNotificationText() {
    return notificationText;
  }

  public void setNotificationText(String notificationText) {
    this.notificationText = notificationText;
  }

  public String getRepeat() {
    return repeat;
  }

  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }

  public Boolean getFormatYearsSelected() {
    return formatYearsSelected;
  }

  public void setFormatYearsSelected(Boolean formatYearsSelected) {
    this.formatYearsSelected = formatYearsSelected;
  }

  public Boolean getFormatMonthsSelected() {
    return formatMonthsSelected;
  }

  public void setFormatMonthsSelected(Boolean formatMonthsSelected) {
    this.formatMonthsSelected = formatMonthsSelected;
  }

  public Boolean getFormatWeeksSelected() {
    return formatWeeksSelected;
  }

  public void setFormatWeeksSelected(Boolean formatWeeksSelected) {
    this.formatWeeksSelected = formatWeeksSelected;
  }

  public Boolean getFormatDaysSelected() {
    return formatDaysSelected;
  }

  public void setFormatDaysSelected(Boolean formatDaysSelected) {
    this.formatDaysSelected = formatDaysSelected;
  }

  public Boolean getLineDividerSelected() {
    return isLineDividerSelected;
  }

  public void setLineDividerSelected(Boolean lineDividerSelected) {
    isLineDividerSelected = lineDividerSelected;
  }

  public int getCounterFontSize() {
    return counterFontSize;
  }

  public void setCounterFontSize(int counterFontSize) {
    this.counterFontSize = counterFontSize;
  }

  public int getTitleFontSize() {
    return titleFontSize;
  }

  public void setTitleFontSize(int titleFontSize) {
    this.titleFontSize = titleFontSize;
  }

  public String getFontType() {
    return fontType;
  }

  public void setFontType(String fontType) {
    this.fontType = fontType;
  }

  public int getFontColor() {
    return fontColor;
  }

  public void setFontColor(int fontColor) {
    this.fontColor = fontColor;
  }

  public int getPictureDim() {
    return pictureDim;
  }

  public void setPictureDim(int pictureDim) {
    this.pictureDim = pictureDim;
  }


  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", date='" + date + '\'' +
        ", description='" + description + '\'' +
        ", image='" + image + '\'' +
        ", imageID=" + imageID +
        ", type='" + type + '\'' +
        ", repeat='" + repeat + '\'' +
        ", widgetID=" + widgetID +
        ", hasAlarm=" + hasAlarm +
        ", reminderYear=" + reminderYear +
        ", reminderMonth=" + reminderMonth +
        ", reminderDay=" + reminderDay +
        ", reminderHour=" + reminderHour +
        ", reminderMinute=" + reminderMinute +
        ", notificationText='" + notificationText + '\'' +
        ", formatYearsSelected=" + formatYearsSelected +
        ", formatMonthsSelected=" + formatMonthsSelected +
        ", formatWeeksSelected=" + formatWeeksSelected +
        ", formatDaysSelected=" + formatDaysSelected +
        ", isLineDividerSelected=" + isLineDividerSelected +
        ", counterFontSize=" + counterFontSize +
        ", titleFontSize=" + titleFontSize +
        ", fontType='" + fontType + '\'' +
        ", fontColor=" + fontColor +
        ", pictureDim=" + pictureDim +
        '}';
  }
}
