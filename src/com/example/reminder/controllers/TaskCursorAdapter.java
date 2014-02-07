package com.example.reminder.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.reminder.database.TaskTableSchema;


public class TaskCursorAdapter extends CursorAdapter {

	public TaskCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	static class ViewHolder {
		public TextView priorityView;
		public TextView taskNameTextView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder) view.getTag();		
		viewHolder.taskNameTextView.setText(cursor.getString(cursor.getColumnIndex(TaskTableSchema.KEY_TASK)));
		
		
		if (cursor.getInt(cursor.getColumnIndex(TaskTableSchema.KEY_TASK_COMPLETED)) == 1) {
			viewHolder.taskNameTextView.setPaintFlags(Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
			//viewHolder.taskNameTextView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.strike));
		} else {
			viewHolder.taskNameTextView.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
		}
		
		if (cursor.getInt(cursor.getColumnIndex(TaskTableSchema.KEY_PRIORITY)) == 1) {
			viewHolder.priorityView.setVisibility(TextView.VISIBLE);
		} else {
			viewHolder.priorityView.setVisibility(TextView.INVISIBLE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View convertView = inflater.inflate(R.layout.task_row_layout, parent, false);

		ViewHolder viewHolder = new ViewHolder();
		viewHolder.priorityView = (TextView) convertView.findViewById(R.id.priority);
		viewHolder.taskNameTextView = (TextView) convertView.findViewById(R.id.task_name);
		convertView.setTag(viewHolder);

		return convertView;
	}
}