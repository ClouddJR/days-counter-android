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
  private String image;
  private String type;
  private String description;
  private int imageID;
  private int widgetID;
  private int color;
  private boolean isOnlyDays;
  private boolean hasAlarm;
  private int year;
  private int month;
  private int day;
  private int hour;
  private int minute;
  private String notificationText;
  private String repeat;


  public Event() {

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

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public boolean isOnlyDays() {
    return isOnlyDays;
  }

  public void setOnlyDays(boolean onlyDays) {
    isOnlyDays = onlyDays;
  }

  public boolean hasAlarm() {
    return hasAlarm;
  }

  public void setHasAlarm(boolean hasAlarm) {
    this.hasAlarm = hasAlarm;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public int getHour() {
    return hour;
  }

  public void setHour(int hour) {
    this.hour = hour;
  }

  public int getMinute() {
    return minute;
  }

  public void setMinute(int minute) {
    this.minute = minute;
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

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", date='" + date + '\'' +
        ", image='" + image + '\'' +
        ", type='" + type + '\'' +
        ", description='" + description + '\'' +
        ", imageID=" + imageID +
        ", widgetID=" + widgetID +
        ", color=" + color +
        ", isOnlyDays=" + isOnlyDays +
        ", hasAlarm=" + hasAlarm +
        ", year=" + year +
        ", month=" + month +
        ", day=" + day +
        ", hour=" + hour +
        ", minute=" + minute +
        ", notificationText='" + notificationText + '\'' +
        ", repeat='" + repeat + '\'' +
        '}';
  }
}
