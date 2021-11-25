package com.teratail.q370729

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog

class Memo {
  var _id: Long = 0 //データベース用
  var name:String = ""
  var age:Int = 0
  var text:String = ""
}

class MainActivity : AppCompatActivity() {
  lateinit var dba: DatabaseAccesser

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val et_name : EditText = findViewById(R.id.et_name)
    val bt_add : Button = findViewById(R.id.bt_add)
    val bt_delete : Button = findViewById(R.id.bt_delete)
    val lv : ListView = findViewById(R.id.lv)

    dba = SQLiteDatabaseAccesser(this)

    val adapter = MemoAdapter(dba)
    lv.adapter = adapter

    //bt_addを押すとet_nameに入力した内容をDBに保存とListViewに表示する処理を記載する
    bt_add.setOnClickListener {
      if(et_name.text.toString().isEmpty()){
        AlertDialog.Builder(this)
          .setTitle("未入力")
          .setMessage("名前を入力して下さい")
          .setPositiveButton("OK",null)
          .show()//et_nameが未入力の場合、アラートダイアログで表示
      }else{
        val memo = Memo()
        memo.name = et_name.text.toString()
        if(adapter.add(memo)) {
          et_name.text.clear()//DBに書き込みしたらEditTextの文字をクリアする
          Toast.makeText(applicationContext, "登録しました", Toast.LENGTH_SHORT).show()//トーストで表示
        }
      }
    }

    //ListViewをタップするとアラートダイアログを表示して、削除するかしないか判断する
    //合わせてDBに登録されているデータも一緒に削除する
    lv.setOnItemClickListener { _, _, position, _ ->
      AlertDialog.Builder(this)
        .setTitle("削除しますか？？")
        .setPositiveButton("Yes", { _, _ ->
          //Yesを押下した時の処理
          //タップされたところのメモの削除
          if(adapter.remove(position)) {
            et_name.text.clear()
            //Toast.makeText(applicationContext,"削除しました",Toast.LENGTH_SHORT).show()
          }
        })
        .setNegativeButton("No", { _, _ ->
          Toast.makeText(applicationContext,"キャンセルしました",Toast.LENGTH_SHORT).show()
        })
        .show()
    }
    //これは予備コード
    //DB全削除用
    bt_delete.setOnClickListener {
      dba.deleteAll()
    }
  }

  override fun onDestroy() {
    dba.destroy()
    super.onDestroy()
  }
}

class MemoAdapter(val dba:DatabaseAccesser) : BaseAdapter() {
  private var memoList:MutableList<Memo>

  init {
    memoList = dba.readAll()
  }

  class ViewHolder(view:View) {
    var nameText:TextView
    var ageText:TextView
    var textText:TextView
    init {
      nameText = view.findViewById(R.id.nameText) as TextView
      ageText = view.findViewById(R.id.ageText) as TextView
      textText = view.findViewById(R.id.textText) as TextView
    }
  }
  override fun getCount(): Int {
    return memoList.size
  }
  override fun getItem(position: Int): Memo {
    return memoList[position]
  }
  override fun getItemId(position: Int): Long {
    return memoList[position]._id
  }
  override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
    var v = view ?:
    LayoutInflater.from(parent!!.context).inflate(R.layout.row_layout,null).also {
      it.tag = ViewHolder(it)
    }

    val vh = v.tag as ViewHolder
    vh.nameText.text = memoList[position].name
    vh.ageText.text = memoList[position].age.toString()
    vh.textText.text = memoList[position].text

    return v
  }

  fun add(memo:Memo): Boolean {
    if(!dba.insert(memo)) return false
    memoList.add(memo)
    notifyDataSetChanged()
    return true
  }
  fun remove(position:Int): Boolean {
    val memo = memoList[position]
    if(!dba.delete(memo)) return false
    memoList.remove(memo)
    notifyDataSetChanged()
    return true
  }
}

interface DatabaseAccesser {
  fun readAll() : MutableList<Memo>
  fun insert(target:Memo):Boolean
  fun delete(target:Memo):Boolean
  fun deleteAll()
  fun destroy()
}