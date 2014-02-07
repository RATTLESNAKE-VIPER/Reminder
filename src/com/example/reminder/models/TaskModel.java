package com.example.reminder.models;

import java.io.Serializable;

import com.example.reminder.database.TaskTableSchema;

import android.database.Cursor;

public class TaskModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	
	private Long taskID;
	private String task;
	private String description;
	private Boolean timerOn;
	private String time;
	private Boolean locationOn;
	private Integer latitudeE6;
	private Integer longitudeE6;
	private Boolean onArrive;
	private Boolean onLeave;
	private Float radius;
	private Integer priority;
	private String notes;
	private Boolean completed;
	private Integer snoozeOn;
	private Boolean reminderOn;

	public Long getTaskID() {
		return taskID;
	}
	public void setTaskID(Long taskID) {
		this.taskID = taskID;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getTimerOn() {
		return timerOn;
	}
	public void setTimerOn(Boolean timerOn) {
		this.timerOn = timerOn;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Boolean getLocationOn() {
		return locationOn;
	}
	public void setLocationOn(Boolean locationOn) {
		this.locationOn = locationOn;
	}
	public Integer getLatitudeE6() {
		return latitudeE6;
	}
	public void setLatitudeE6(Integer latitudeE6) {
		this.latitudeE6 = latitudeE6;
	}
	public Integer getLongitudeE6() {
		return longitudeE6;
	}
	public void setLongitudeE6(Integer longitudeE6) {
		this.longitudeE6 = longitudeE6;
	}
	public Boolean getOnArrive() {
		return onArrive;
	}
	public void setOnArrive(Boolean onArrive) {
		this.onArrive = onArrive;
	}
	public Boolean getOnLeave() {
		return onLeave;
	}
	public void setOnLeave(Boolean onLeave) {
		this.onLeave = onLeave;
	}
	public Float getRadius() {
		return radius;
	}
	public void setRadius(Float radius) {
		this.radius = radius;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Boolean getCompleted() {
		return completed;
	}
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	public Integer getSnoozeOn() {
		return snoozeOn;
	}
	public void setSnoozeOn(Integer snoozeOn) {
		this.snoozeOn = snoozeOn;
	}
	public Boolean getReminderOn() {
		return reminderOn;
	}
	public void setReminderOn(Boolean reminderOn) {
		this.reminderOn = reminderOn;
	}

	
	public static TaskModel populateModelFromCursor(Cursor c) {
		TaskModel taskModel = new TaskModel();
		taskModel.setTaskID(c.getLong(c.getColumnIndex(TaskTableSchema._ID)));
		taskModel.setTask(c.getString(c.getColumnIndex(TaskTableSchema.KEY_TASK)));
		taskModel.setDescription(c.getString(c.getColumnIndex(TaskTableSchema.KEY_DESCRIPTION)));
		taskModel.setTimerOn(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_TIMER_ON)) == 1 ? true : false);
		taskModel.setTime(c.getString(c.getColumnIndex(TaskTableSchema.KEY_TIME)));
		taskModel.setLocationOn(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LOCATION_ON)) == 1 ? true : false);
		taskModel.setLatitudeE6(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LATITUDE)));
		taskModel.setLongitudeE6(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LONGITUDE)));
		taskModel.setOnArrive(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_ON_ARRIVE)) == 1 ? true : false);
		taskModel.setOnLeave(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_ON_LEAVE)) == 1 ? true : false);
		taskModel.setRadius(c.getFloat(c.getColumnIndex(TaskTableSchema.KEY_RADIUS)));
		taskModel.setPriority(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_PRIORITY)));
		taskModel.setNotes(c.getString(c.getColumnIndex(TaskTableSchema.KEY_NOTES)));
		taskModel.setCompleted(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_TASK_COMPLETED)) == 1 ? true : false);
		taskModel.setSnoozeOn(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_SNOOZE_ON)));
		taskModel.setReminderOn(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_REMINDER_ON)) == 1 ? true : false);
		
		return taskModel;		
	}
	
}
