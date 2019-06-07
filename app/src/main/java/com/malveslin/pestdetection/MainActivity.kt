package com.malveslin.pestdetection

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.util.LogPrinter
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.malvesin.pestdetection.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    private val GALLERY = 1
    private val TAKE_PHOTO_REQUEST = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Toast.makeText(this, "Bem vindo " + intent.getStringExtra("username"), Toast.LENGTH_SHORT).show()

        uploadImageButton.setOnClickListener {
            showPictureDialog()
        }

        setup()


    }

    private fun Context.toast(message:String){
        Toast.makeText(this@MainActivity,message,Toast.LENGTH_SHORT).show()
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun choosePhotoFromCamera() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), TAKE_PHOTO_REQUEST)
            return
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            TAKE_PHOTO_REQUEST -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                } else {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(packageManager)?.also {
                            startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                        }
                    }
                }
            }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Selecione uma imagem")
        val pictureDialogItems = arrayOf("Selecionar da galeria", "Tirar uma foto")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> choosePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data?.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                    Toast.makeText(this@MainActivity, "Imagem Salva!", Toast.LENGTH_SHORT).show()
                    result_textview.text = "Processando....."
                    imageView.setImageBitmap(bitmap)

                    val imgString = Base64.encodeToString(
                        getBytesFromBitmap(bitmap),
                        Base64.NO_WRAP
                    )


                    val requestBody = ModelRequestBody(PayloadRequest(ModelImage(imgString)))
                    Network("https://automl.googleapis.com/v1beta1/", true).getRetrofitClient()
                        .create(Endpoint::class.java).classifyImage(requestBody)
                        .enqueue(object : Callback<PayloadResult> {

                            override fun onResponse(call: Call<PayloadResult>?, response: Response<PayloadResult>?) {

                                if (response!!.isSuccessful) {
                                    Log.d("response", response?.body().toString())
                                    val result =
                                        response?.body()?.items?.first()?.classification?.let { it.score * 100 }
                                            .toString().toDoubleOrNull()
                                    if (result == null || result < 90) {
                                        result_textview.text = "Não encontramos anomalias relacionadas a imagem"
                                    } else {
                                        result_textview.text =
                                            "${response?.body()?.items?.first()?.displayName} Score: ${(response?.body()?.items?.first()?.classification?.let { it.score * 100 })}"
                                    }

                                } else {
                                    result_textview.text = "erro api"
                                }
                            }

                            override fun onFailure(call: Call<PayloadResult>, t: Throwable) {
                                print(t!!.message)
                            }
                        }
                        )
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == TAKE_PHOTO_REQUEST) {
            if (data != null) {
                try {
                    val bitmap = data.extras.get("data") as Bitmap

                    Toast.makeText(this@MainActivity, "Imagem Salva!", Toast.LENGTH_SHORT).show()
                    result_textview.text = "Processando....."
                    imageView.setImageBitmap(bitmap)

                    val imgString = Base64.encodeToString(
                        getBytesFromBitmap(bitmap),
                        Base64.NO_WRAP
                    )


                    val requestBody = ModelRequestBody(PayloadRequest(ModelImage(imgString)))
                    Network("https://automl.googleapis.com/v1beta1/", true).getRetrofitClient()
                        .create(Endpoint::class.java).classifyImage(requestBody)
                        .enqueue(object : Callback<PayloadResult> {

                            override fun onResponse(call: Call<PayloadResult>?, response: Response<PayloadResult>?) {
                                Toast.makeText(this@MainActivity, "" + response?.isSuccessful, Toast.LENGTH_SHORT).show()
                                if (response!!.isSuccessful) {
                                    Log.d("response", response?.body().toString())
                                    val result =
                                        response?.body()?.items?.first()?.classification?.let { it.score * 100 }
                                            .toString().toDoubleOrNull()
                                    if (result == null || result < 90) {
                                        result_textview.text = "Não encontramos anomalias relacionadas a imagem"
                                    } else {
                                        result_textview.text =
                                            "${response?.body()?.items?.first()?.displayName} Score: ${(response?.body()?.items?.first()?.classification?.let { it.score * 100 })}"
                                    }

                                } else {
                                    result_textview.text = "erro api"
                                }
                            }

                            override fun onFailure(call: Call<PayloadResult>, t: Throwable) {
                                print(t!!.message)
                            }
                        }
                        )
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null


        return true
    }

    private fun setup(){

        var actionBar = supportActionBar
        actionBar?.title = "Pest detection app"


        val drawerToggle:ActionBarDrawerToggle = object :ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.open,
            R.string.close
        ){
            override fun onDrawerClosed(view:View){
                super.onDrawerClosed(view)
                //toast("Drawer closed")
            }

            override fun onDrawerOpened(drawerView: View){
                super.onDrawerOpened(drawerView)
                //toast("Drawer opened")
            }
        }

        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        nav_view.getHeaderView(0).txtHomeUsuario.setText(intent.getStringExtra("username"))
        nav_view.getHeaderView(0).txtHomeEmail.setText(intent.getStringExtra("email"))


        nav_view.setNavigationItemSelectedListener{
            when (it.itemId){
                R.id.imageView -> toast("Image clicked")
                R.id.nav_home_analises -> toast("Minhas analises")
                R.id.nav_home_anomalias -> toast("Anomalias")
                R.id.nav_home_metricas -> toast("Metricas")
                R.id.nav_home_modelos -> toast("Modelos")
                R.id.nav_home_preferencias -> toast("Preferencias")
                R.id.nav_home_logout -> {
                    toast("Sair")
                    var intent: Intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }

            }
            // Close the drawer
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }
    }


    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

}
