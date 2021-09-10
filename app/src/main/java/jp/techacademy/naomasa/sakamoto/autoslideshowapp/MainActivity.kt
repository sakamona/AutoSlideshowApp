package jp.techacademy.naomasa.sakamoto.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.content.ContentUris
import android.media.Image
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var imageUriList = mutableListOf<Uri>()
    private var index = 0
    private var mTimer: Timer? = null
    private var mTimerSec = 0.0
    private var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            val v: View = findViewById(android.R.id.content)
            Snackbar.make(v, "お使いの端末ではご利用出来ません。", Snackbar.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    val v: View = findViewById(android.R.id.content)
                    Snackbar.make(v, "許可がないので使用出来ません。", Snackbar.LENGTH_INDEFINITE)
                        .setAction("retry") {
                            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                        }.show()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                //indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUriList.add(imageUri)
                Log.d("ANDROID","added")
            } while (cursor.moveToNext())
        }
        cursor.close()

        go_button.setOnClickListener(this)
        back_button.setOnClickListener(this)
        auto_button.setOnClickListener(this)
        imageView.setImageURI(imageUriList[index])
    }

    override fun onClick(v: View) {
        if (v.id == R.id.go_button) {
            if (this.index == (this.imageUriList.size - 1)) {
                this.index = 0
            } else {
                this.index += 1
            }
            imageView.setImageURI(imageUriList[index])
        }

        if (v.id == R.id.back_button) {
            if (this.index == 0) {
                this.index = this.imageUriList.size - 1
            } else {
                this.index -= 1
            }
            imageView.setImageURI(imageUriList[index])
        }

        if (v.id == R.id.auto_button) {
            if (auto_button.text == "再生") {
                auto_button.text = "停止"
                go_button.isEnabled = false
                back_button.isEnabled = false

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if (index == (imageUriList.size - 1)) {
                                index = 0
                            } else {
                                index += 1
                            }
                            imageView.setImageURI(imageUriList[index])
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
            } else if (auto_button.text == "停止") {
                auto_button.text = "再生"
                go_button.isEnabled = true
                back_button.isEnabled = true

                mTimer!!.cancel()
                mTimer = null
            }
        }
    }


}