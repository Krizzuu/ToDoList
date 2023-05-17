package com.pam.todolist

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import kotlin.Int
import kotlin.Long
import kotlin.arrayOf

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "tasks.db"

        private const val TABLE_NAME = "tasks"

        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_CATEGORY = "category"
        private const val COL_DESCRIPTION = "description"
        private const val COL_ATTACHMENTS = "attachments"
        private const val COL_CREATED = "created"
        private const val COL_DEADLINE = "deadline"
        private const val COL_NOTIFY = "notify"
        private const val COL_COMPLETED = "completed"

        private const val TABLE = "CREATE TABLE $TABLE_NAME(" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_TITLE TEXT NOT NULL, " +
                "$COL_CATEGORY TEXT, " +
                "$COL_DESCRIPTION TEXT, " +
                "$COL_DEADLINE TEXT, " +
                "$COL_NOTIFY INTEGER DEFAULT 0, " +
                "$COL_ATTACHMENTS TEXT," +
                "$COL_COMPLETED INTEGER DEFAULT 0, " +
                "$COL_CREATED TEXT" +
                ");"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, old: Int, new: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun clearTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }

    fun addTask(task: Task) : Long {
        val db : SQLiteDatabase = writableDatabase
        val values = ContentValues()

        values.put(COL_TITLE, task.title)
        values.put(COL_DESCRIPTION, task.description)
        values.put(COL_CREATED, task.createTime)
        values.put(COL_DEADLINE, task.deadline)
        values.put(COL_COMPLETED, if (task.isCompleted) 1 else 0)
        values.put(COL_NOTIFY, if (task.isNotify) 1 else 0)
        values.put(COL_CATEGORY, task.category)

        val attachments = task.attachments
        val attachmentsString = Task.convertUriListToString(attachments)
        values.put(COL_ATTACHMENTS, attachmentsString)

        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun updateTask(task: Task) : Int{
        val db : SQLiteDatabase = writableDatabase;
        val values = ContentValues()

        values.put(COL_TITLE, task.title)
        values.put(COL_DESCRIPTION, task.description)
        values.put(COL_CREATED, task.createTime)
        values.put(COL_DEADLINE, task.deadline)
        values.put(COL_COMPLETED, if (task.isCompleted) 1 else 0)
        values.put(COL_NOTIFY, if (task.isNotify) 1 else 0)
        values.put(COL_CATEGORY, task.category)

        val attachments = task.attachments
        val attachmentsString = Task.convertUriListToString(attachments)
        values.put(COL_ATTACHMENTS, attachmentsString)

        val updated : Int = db.update(TABLE_NAME, values,
            "$COL_ID = ?" , arrayOf(task.id.toString())
        )
        db.close()
        return updated
    }

    fun getTask(id: Long) : Task? {
        val db : SQLiteDatabase = writableDatabase;
        var task : Task? = null

        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_ID = $id"
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            task = Task()
            var index : Int
            index = cursor.getColumnIndex(COL_ID)
            if (index != -1) {
                task.id = cursor.getLong(index)
            }
            index = cursor.getColumnIndex(COL_TITLE)
            if (index != -1) {
                task.title = cursor.getString(index)
            }
            index = cursor.getColumnIndex(COL_CATEGORY)
            if (index != -1) {
                task.category = cursor.getString(index)
            }
            index = cursor.getColumnIndex(COL_DESCRIPTION)
            if (index != -1) {
                task.description = cursor.getString(index)
            }
            index = cursor.getColumnIndex(COL_DEADLINE)
            if (index != -1) {
                task.deadline = cursor.getString(index)
            }
            index = cursor.getColumnIndex(COL_ID)
            if (index != -1) {
                task.isNotify = cursor.getInt(index) > 0
            }
            index = cursor.getColumnIndex(COL_ATTACHMENTS)
            if (index != -1) {
                val attachmentString = cursor.getString(index)
                val attachments = Task.convertStringToUriList(attachmentString)
                task.attachments = attachments
            }
            index = cursor.getColumnIndex(COL_CREATED)
            if (index != -1) {
                task.createTime = cursor.getString(index)
            }
            index = cursor.getColumnIndex(COL_COMPLETED)
            if (index != -1) {
                task.isCompleted = cursor.getInt(index) > 0
            }
        }

        cursor.close()
        db.close()

        return task
    }

    fun deleteTask(id: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getAllTasks(): List<Task>? {
        val tasks: MutableList<Task> = ArrayList()

        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val task = Task()
                var index : Int
                index = cursor.getColumnIndex(COL_ID)
                if (index != -1) {
                    task.id = cursor.getLong(index)
                }
                index = cursor.getColumnIndex(COL_TITLE)
                if (index != -1) {
                    task.title = cursor.getString(index)
                }
                index = cursor.getColumnIndex(COL_CATEGORY)
                if (index != -1) {
                    task.category = cursor.getString(index)
                }
                index = cursor.getColumnIndex(COL_DESCRIPTION)
                if (index != -1) {
                    task.description = cursor.getString(index)
                }
                index = cursor.getColumnIndex(COL_DEADLINE)
                if (index != -1) {
                    task.deadline = cursor.getString(index)
                }
                index = cursor.getColumnIndex(COL_ID)
                if (index != -1) {
                    task.isNotify = cursor.getInt(index) > 0
                }
                index = cursor.getColumnIndex(COL_ATTACHMENTS)
                if (index != -1) {
                    val attachmentString = cursor.getString(index)
                    task.setAttachmentsWithString(attachmentString)
                }
                index = cursor.getColumnIndex(COL_CREATED)
                if (index != -1) {
                    task.createTime = cursor.getString(index)
                }
                index = cursor.getColumnIndex(COL_COMPLETED)
                if (index != -1) {
                    task.isCompleted = cursor.getInt(index) > 0
                }

                tasks.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tasks
    }
}