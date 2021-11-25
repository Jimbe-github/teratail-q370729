package com.teratail.q370729

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class SQLiteDatabaseAccesser(val context: Context): DatabaseAccesser {
  class OpenHelper(context: Context) : SQLiteOpenHelper(context, "Memo.db", null, 1) {
    private val SQL_CREATE_ENTRIES =
      "CREATE TABLE ${MemoEntry.TABLE_NAME} (" +
              "${BaseColumns._ID} INTEGER PRIMARY KEY," +
              "${MemoEntry.COLUMN_NAME_NAME} TEXT," +
              "${MemoEntry.COLUMN_NAME_AGE} INTEGER," +
              "${MemoEntry.COLUMN_NAME_TEXT} TEXT)"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${MemoEntry.TABLE_NAME}"

    override fun onCreate(db: SQLiteDatabase) {
      db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      db.execSQL(SQL_DELETE_ENTRIES)
      onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      onUpgrade(db, oldVersion, newVersion)
    }
  }

  val helper: OpenHelper

  init {
    helper = OpenHelper(context)
  }

  object MemoEntry : BaseColumns {
    const val TABLE_NAME = "Memo"
    const val COLUMN_NAME_NAME = "name"
    const val COLUMN_NAME_AGE = "age"
    const val COLUMN_NAME_TEXT = "text"
  }

  override fun readAll() : MutableList<Memo> {
    val memoList = mutableListOf<Memo>()

    val db = helper.readableDatabase
    val projection = arrayOf(BaseColumns._ID, MemoEntry.COLUMN_NAME_NAME, MemoEntry.COLUMN_NAME_AGE, MemoEntry.COLUMN_NAME_TEXT)
    db.query(MemoEntry.TABLE_NAME, projection, null, null, null, null, null).use { cursor ->
      with(cursor) {
        val idIndex = getColumnIndexOrThrow(BaseColumns._ID)
        val nameIndex = getColumnIndexOrThrow(MemoEntry.COLUMN_NAME_NAME)
        val ageIndex = getColumnIndexOrThrow(MemoEntry.COLUMN_NAME_AGE)
        val textIndex = getColumnIndexOrThrow(MemoEntry.COLUMN_NAME_TEXT)
        while(moveToNext()) {
          val memo = Memo();
          memo._id = getLong(idIndex)
          memo.name = getString(nameIndex)
          memo.age = getInt(ageIndex)
          memo.text = getString(textIndex)
          memoList.add(memo)
        }
      }
    }

    return memoList
  }
  override fun insert(target: Memo):Boolean {
    val db = helper.writableDatabase
    val values = ContentValues().apply {
      put(MemoEntry.COLUMN_NAME_NAME, target.name)
      put(MemoEntry.COLUMN_NAME_AGE, target.age)
      put(MemoEntry.COLUMN_NAME_TEXT, target.text)
    }
    target._id = db.insert(MemoEntry.TABLE_NAME, null, values)
    return true
  }
  override fun delete(target: Memo):Boolean {
    val db = helper.writableDatabase
    val selection = "${BaseColumns._ID} = ?"
    val selectionArgs = arrayOf(target._id.toString())
    val deletedRows = db.delete(MemoEntry.TABLE_NAME, selection, selectionArgs)
    return deletedRows >= 1
  }
  override fun deleteAll() {
    val db = helper.writableDatabase
    db.delete(MemoEntry.TABLE_NAME, null, null)
  }
  override fun destroy() {
    helper.close();
  }
}