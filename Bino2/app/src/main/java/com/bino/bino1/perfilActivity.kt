package com.bino.bino1

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bino.bino1.Utils.CircleTransform
import com.bino.bino1.Utils.SharePreferences
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.*
import java.lang.ref.WeakReference
import java.util.*

class perfilActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    //envio de imagem
    private lateinit var filePath: Uri
    private var urifinal: String = "nao"
    private lateinit var mphotoStorageReference: StorageReference
    private lateinit var mFireBaseStorage: FirebaseStorage

    var userBd = "nao"

    var pontos = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)


        val actionbar = this.supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setHomeButtonEnabled(true)

        metodosIniciais()

        pontos = SharePreferences.getPoints(this)

        queryInfosExtras()

    }

    fun metodosIniciais() {

        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference

        val btnHelpNumero: Button = findViewById(R.id.perfil_btnHelpNumero)
        btnHelpNumero.setOnClickListener {
            openPopUp(
                "Ajuda",
                "Você pode cadastrar um número de emergência para o qual o Bino envia sua localização e um pedido de ajuda em caso de emergência. É importante que este número seja um telefone com whatsapp.",
                false,
                "n",
                "m"
            )
        }

        val btnUpload: Button = findViewById(R.id.perfil_btnUpload)
        btnUpload.setOnClickListener {

            if (CheckPermissions()) {

                openPopUp2(
                    "Envio de imagem",
                    "Selecione o modo de envio da imagem:",
                    true,
                    "Tirar foto",
                    "foto do celular",
                    "fotoNovoProd"
                )

            }

        }


        val nEmergencia = intent.getStringExtra("nEmergencia")
        val nome = intent.getStringExtra("nome")
        val img = intent.getStringExtra("img")
        userBd = intent.getStringExtra("userBd")
        val whatsapp = intent.getStringExtra("whastapp")

        databaseReference = FirebaseDatabase.getInstance().reference


        val etNemergencia: EditText = findViewById(R.id.perfil_etNemergencia)
        val country2 = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter2 = PhoneNumberFormatter(WeakReference(etNemergencia), country2)
        etNemergencia.addTextChangedListener(phoneFormatter2)

        if (!nEmergencia.equals("nao")) {
            etNemergencia.setText(nEmergencia)
        }
        val etNome: EditText = findViewById(R.id.perfil_etNome)
        if (!nome.equals("nao")) {
            etNome.setText(nome)
        }
        val imageView: ImageView = findViewById(R.id.perfil_imageView)
        if (!img.equals("nao")) {

            Glide.with(this)
                .load(img)
                .thumbnail(0.7f)
                .skipMemoryCache(true)
                .transform(CircleTransform(this)) // applying the image transformer
                .into(imageView)

        } else {

            Glide.with(applicationContext)
                .load(R.drawable.perfil)
                .thumbnail(0.7f)
                .skipMemoryCache(true)
                .transform(CircleTransform(this)) // applying the image transformer
                .into(imageView)

        }

        val etNascimento: EditText = findViewById(R.id.perfil_etNascimento)
        dateWatcher(etNascimento)

        val etNwhatsapp: EditText = findViewById(R.id.perfil_etNwhatsapp)
        //textWatcher para formatar em máscara de telefone
        val country = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter = PhoneNumberFormatter(WeakReference(etNwhatsapp), country)
        etNwhatsapp.addTextChangedListener(phoneFormatter)

        if (!whatsapp.equals("nao")) {
            etNwhatsapp.setText(whatsapp)
        }

        val radioN = findViewById<RadioButton>(R.id.perfil_remedioRadioN)
        val radioS = findViewById<RadioButton>(R.id.perfil_remedioRadioS)

        radioN.setOnClickListener {
            radioN.isChecked = true
            radioS.isChecked = false
        }

        radioS.setOnClickListener {
            radioN.isChecked = false
            radioS.isChecked = true
        }


        val btnSalvar: Button = findViewById(R.id.perfil_btnSalvar)
        btnSalvar.setOnClickListener {

            if (!urifinal.equals("nao")) {
                databaseReference.child("usuarios").child(userBd).child("img").setValue(urifinal)
                if (img.equals("nao")){
                    //significa que é a primeira foto que ele upa
                    updateUserPoints(25)
                }

            }
            if (!etNemergencia.text.equals(nEmergencia) && !etNemergencia.text.isEmpty()) {
                var numero = etNemergencia.text.toString()
                numero = numero.replace("(", "")
                numero = numero.replace(")","").trim()
                databaseReference.child("usuarios").child(userBd).child("nEmergencia")
                    .setValue(numero)
                if (nEmergencia.equals("nao")){
                    updateUserPoints(20)
                }

            }
            if (!etNome.text.equals(nome) && !etNome.text.isEmpty()) {
                databaseReference.child("usuarios").child(userBd).child("nome")
                    .setValue(etNome.text.toString())
                if (nome.equals("nao")){
                    updateUserPoints(10)
                }

            }
            if (!etNwhatsapp.equals(whatsapp) && !etNwhatsapp.text.isEmpty()) {
                var numero = etNwhatsapp.text.toString()
                numero = numero.replace("(", "")
                numero = numero.replace(")","").trim()
                databaseReference.child("usuarios").child(userBd).child("whatsapp")
                    .setValue(numero)
                if (whatsapp.equals("nao")){
                    updateUserPoints(15)
                }
            }

            val etNascimento: EditText = findViewById(R.id.perfil_etNascimento)
            val etDoencas: EditText = findViewById(R.id.perfil_etDoencas)

            if (etNascimento.text.isEmpty()){
                databaseReference.child("usuarios").child(userBd).child("nascimento").setValue(etNascimento.text.toString())
            } else{
                databaseReference.child("usuarios").child(userBd).child("nascimento").setValue("nao")
            }

            if (etDoencas.text.isEmpty()){
                databaseReference.child("usuarios").child(userBd).child("doencas").setValue(etDoencas.text.toString())
            } else{
                databaseReference.child("usuarios").child(userBd).child("doencas").setValue("nao")
            }

            if (radioN.isChecked){
                databaseReference.child("usuarios").child(userBd).child("remedios").setValue("nao")
            } else {
                databaseReference.child("usuarios").child(userBd).child("remedios").setValue("sim")
            }

            finish()
        }

    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // This Activity allow only one action (Back To Menu);
        finish()
        return true
    }

   

      fun queryInfosExtras() {


        databaseReference = FirebaseDatabase.getInstance().reference

        ChamaDialog()
    val rootRef = databaseReference.child("usuarios").child(userBd)
    rootRef.addListenerForSingleValueEvent(
    object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {

            EncerraDialog()
        }

        override fun onDataChange(p0: DataSnapshot) {

            var values: String

            if (p0.child("nascimento").exists()){
                values = p0.child("nascimento").value.toString()
                val etNascimento: EditText = findViewById(R.id.perfil_etNascimento)
                etNascimento.setText(values)
            }

            if (p0.child("doencas").exists()){
                values = p0.child("doencas").value.toString()
                val etDoencas: EditText = findViewById(R.id.perfil_etDoencas)
                etDoencas.setText(values)
            }

            if (p0.child("remedios").exists()){
                val radioN = findViewById<RadioButton>(R.id.perfil_remedioRadioN)
                val radioS = findViewById<RadioButton>(R.id.perfil_remedioRadioS)
                values = p0.child("remedios").value.toString()
                if (values.equals("nao")){
                    radioN.isChecked = true
                    radioS.isChecked = false
                } else {
                    radioN.isChecked = false
                    radioS.isChecked = true
                }
            }

            EncerraDialog()

        }


        //EncerraDialog()

    })

}

    fun dateWatcher( editText:EditText) {

        var oldString : String = ""

        editText.addTextChangedListener(object : TextWatcher {
            var changed: Boolean = false

            override fun afterTextChanged(p0: Editable?) {

                changed = false



            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
                //changed=false
                editText.setSelection(p0.toString().length)
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                var str: String = p0.toString()

                if (str != null) {

                    if (oldString.equals(str)) {
                        //significs que o user esta apagando
                        //do Nothing

                    } else if (str.length == 2) {  //  xx
                        var element0: String = str.elementAt(0).toString()
                        var element1: String = str.elementAt(1).toString()
                        str = element0 + element1 + "/"
                        editText.setText(str)
                        oldString = element0 + element1
                        editText.setSelection(str.length)

                    } else if (str.length == 5) { //  xx/xx

                        var element0: String = str.elementAt(0).toString() //x
                        var element1: String = str.elementAt(1).toString() //-x
                        var element2: String = str.elementAt(2).toString() //--/
                        var element3: String = str.elementAt(3).toString() //--/x
                        var element4: String = str.elementAt(4).toString() //--/-x

                        str = element0 + element1 + element2 + element3 + element4 + "/"
                        editText.setText(str)
                        oldString = element0 + element1 + element2 + element3 + element4
                        editText.setSelection(str.length)

                    } else if (str.length > 10) { // este exemplo é para data no formato xx/xx/xx. Se você quer usar xx/xx/xxxx mudar para if (str.length >10). O resto do código permanece o mesmo.

                        str = str.substring(0, str.length - 1)
                        editText.setText(str)
                        editText.setSelection(str.length)

                    }


                }

            }
        })
    }

    //upload de foto
    fun openPopUp2 (titulo: String, texto:String, exibeBtnOpcoes:Boolean, btnSim: String, btnNao: String, call: String) {
        //exibeBtnOpcoes - se for não, vai exibir apenas o botão com OK, sem opção. Senão, exibe dois botões e pega os textos deles de btnSim e btnNao

        //EXIBIR POPUP
        // Initialize a new layout inflater instance
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.popup_model,null)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }

        // If API level 23 or higher then execute the code
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

        }

        // Get the widgets reference from custom view
        val buttonPopupN = view.findViewById<Button>(R.id.btnReclamar)
        val buttonPopupS = view.findViewById<Button>(R.id.BtnRecebimento)
        val buttonPopupOk = view.findViewById<Button>(R.id.popupBtnOk)
        val txtTitulo = view.findViewById<TextView>(R.id.popupTitulo)
        val txtTexto = view.findViewById<TextView>(R.id.popupTexto)
        val progressbar = view.findViewById<ProgressBar>(R.id.progressBar)
        val txBarra = view.findViewById<TextView>(R.id.popupMsg)

        txBarra.visibility = View.GONE
        progressbar.visibility = View.GONE


        if (exibeBtnOpcoes){
            //vai exibir os botões com textos e esconder o btn ok
            buttonPopupOk.visibility = View.GONE
            //exibe e ajusta os textos dos botões
            buttonPopupN.text = btnNao
            buttonPopupS.text = btnSim

            /*
            // Set a click listener for popup's button widget
            buttonPopupN.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

             */

            buttonPopupN.setOnClickListener {
                takePictureFromGallery()
                popupWindow.dismiss()
                //ChamaDialog()  se chamar aqui e o usuario cancelar, fica travada a tela pra sempre
            }

            buttonPopupS.setOnClickListener {
                takePictureFromCamera()
                popupWindow.dismiss()
                ChamaDialog() //ChamaDialog()  se chamar aqui e o usuario cancelar, fica travada a tela pra sempre
            }

        } else {

            //vai esconder os botões com textos e exibir o btn ok
            buttonPopupOk.visibility = View.VISIBLE
            //exibe e ajusta os textos dos botões
            buttonPopupN.visibility = View.GONE
            buttonPopupS.visibility = View.GONE


            buttonPopupOk.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

        }

        txtTitulo.text = titulo
        txtTexto.text = texto


        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            //Fecha a janela ao clicar fora também
            popupWindow.dismiss()
        }

        //lay_root é o layout parent que vou colocar a popup
        val lay_root: ConstraintLayout = findViewById(R.id.layPerfil)

        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(lay_root)
        popupWindow.showAtLocation(
            lay_root, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )


    }

    fun takePictureFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 100)
        }
    }

    fun takePictureFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Selecione a foto"), 101)

    }

    //retorno da imagem
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //retorno da camera
        //primeiro if resultado da foto tirada pela camera
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {

                val photo: Bitmap = data?.extras?.get("data") as Bitmap
                compressImage(photo)

            }

        } else {
            //resultado da foto pega na galeria
            if (resultCode == RESULT_OK
                && data != null && data.getData() != null
            ) {

                filePath = data.getData()!!
                var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                compressImage(bitmap)


            }
        }
    }

    private fun compressImage(image: Bitmap) {

        ChamaDialog()

        //agora sabemos as dimensões da imagem.
        //neste exemplo queremos que caiba em um banner de 100x400
        //é alterando o tamanho aqui que o tamanho total da imagem cresce ao final**************************************
//pode ser 100x100, depende do formato que você quer exibir
//400x100 fica com 2,5 kb, 800x200 fica com 5 kb
        val imageProvisoria: Bitmap = calculateInSizeSampleToFitImageView(image, 800, 200)

        //image provisoria pode ser colocada no imageview pois já é pequena suficiente.
        val imageviewBanne:ImageView = findViewById(R.id.perfil_imageView)
        //imageviewBanne.setImageBitmap(imageProvisoria)
        Glide.with(applicationContext)  //2
            .load(imageProvisoria) //3
            .centerCrop() //4
            .placeholder(R.drawable.perfil) //5
            .error(R.drawable.perfil) //6
            .fallback(R.drawable.perfil) //7
            .into(imageviewBanne)


//esta parte é do método antigo. Imagino que ele nao tenha função mais
        val baos = ByteArrayOutputStream()
        var optionsCompress = 20  //taxa de compressao. 100 significa nenhuma compressao
        try {
            //Code here
            while (baos.toByteArray().size / 1024 > 50) {  //Loop if compressed picture is greater than 50kb, than to compression
                baos.reset() //Reset baos is empty baos
                imageProvisoria.compress(
                    Bitmap.CompressFormat.JPEG,
                    optionsCompress,
                    baos
                ) //The compression options%, storing the compressed data to the baos
                optionsCompress -= 25 //Every time reduced by 10
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


        //aqui faz upload pro storage database
        val tempUri: Uri = getImageUri(this, imageProvisoria)
        filePath = tempUri
        uploadImage()
    }


    fun calculateInSizeSampleToFitImageView (image: Bitmap, imageViewWidth:Int, imageViewHeight:Int) : Bitmap{

        //ESTE BLOCO É PARA PEGAR AS DIMENSOES DA IMAGEM
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        //converte a imagem que o usuario escolheu para um Uri e depois para um File
        val file = bitmapToFile(image)
        val fpath = file.path
        BitmapFactory.decodeFile(fpath, options)
        //resultados pegos do método acima
        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        //FIM DAS DIMENSOES DA IMAGEM

        var adaptedHeight: Int =0
        var adaptedWidh: Int =0
        //vamos primeiro acerta a altura. Poderiamos fazer tudo ao mesmo tempo, mas como estamos trabalhando com possibilidade do height ser diferente do width poderia dar erro
        if (imageHeight > imageViewHeight){

            adaptedHeight = imageHeight / 2
            while (adaptedHeight > imageViewHeight){
                adaptedHeight = adaptedHeight/2
            }

        } else {
            adaptedHeight = imageViewHeight
        }

        if (imageWidth > imageViewWidth){

            adaptedWidh = imageWidth / 2
            while (adaptedWidh > imageViewHeight){
                adaptedWidh = adaptedWidh/2
            }
        } else {
            adaptedWidh = imageViewWidth
        }

        val newBitmap = Bitmap.createScaledBitmap(image, adaptedWidh, adaptedHeight, false)
        return newBitmap

    }

    // Method to save an bitmap to a file
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    //pega o uri
    fun  getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 35, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }


    //envio da foto
    //existe uma opção especial aqui para o caso de ser alvará
    fun uploadImage(){

        mFireBaseStorage = FirebaseStorage.getInstance()
        mphotoStorageReference = mFireBaseStorage.reference


        mphotoStorageReference =mFireBaseStorage.getReference().child(userBd).child("img").child("img")

        val bmp: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath)
        val baos: ByteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)

//get the uri from the bitmap
        val tempUri: Uri = getImageUri(this, bmp)
//transform the new compressed bmp in filepath uri
        filePath = tempUri

        //var file = Uri.fromFile(bitmap)
        var uploadTask = mphotoStorageReference.putFile(filePath)

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                    EncerraDialog()
                }
            }
            return@Continuation mphotoStorageReference.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                urifinal = downloadUri.toString()
                //se quiser salvar, é o urifinal que é o link
                //pra salvar no bd e carregar com glide.
                EncerraDialog()


            } else {
                // Handle failures
                Toast.makeText(this, "um erro ocorreu.", Toast.LENGTH_SHORT).show()
                EncerraDialog()
                // ...
            }
        }

    }




















    //permissoes
    private fun setupPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            //permissão concedida
        } else {
            RequestWriteStoragePermission()
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
        } else {
            RequestReadStoragePermission()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
        } else {
            RequestCameraPermission()
        }
    }

    private fun CheckPermissions() : Boolean {
        var permissao = 0  //é negado
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            //permissão concedida
            permissao=1
        } else {
            setupPermissions()
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            permissao=1
        } else {
            setupPermissions()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
            permissao=1
        } else {
            setupPermissions()
        }

        if (permissao==1){
            return true
        } else {
            return false
        }
    }

    //aqui sao tres métodos. Cada um para uma permissão
    fun RequestCameraPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para acessar a Camera. Vamos usar para você poder tirar fotos para enviar ao App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        1003)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para acessar a Camera. Vamos usar para você poder tirar fotos para enviar ao App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        1003)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        }
    }

    fun RequestReadStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para ler arquivos do seu celular. Vamos usar para você poder enviar as fotos para o App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1001)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para ler arquivos do seu celular. Vamos usar para você poder enviar as fotos para o App")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1002)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        }
    }

    fun RequestWriteStoragePermission (){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para salvar arquivos no seu celular")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1002)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Precisamos de sua permissão para salvar arquivos no seu celular")
                .setTitle("Permissões necessárias")
                .setCancelable(false)
                .setPositiveButton("Sim, autorizar", DialogInterface.OnClickListener { dialog, which ->

                    //mude a permissão aqui
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1002)

                })
            // Display a negative button on alert dialog
            builder.setNegativeButton("Não"){dialog,which ->
                Toast.makeText(applicationContext,"Você negou a permissão e não poderá acessar as funcionalidades.",Toast.LENGTH_SHORT).show()
            }
            val alert : AlertDialog = builder.create()
            alert.show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1002){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                //permissao garantida
            } else {
                //permissao negada
            }
        }
        if (requestCode == 1001){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                //permissao garantida
            } else {

            }
        }
        if (requestCode == 1003){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                //permissão garantida
            } else {
                //permissao negada
            }
        }
    }



    fun openPopUp (titulo: String, texto:String, exibeBtnOpcoes:Boolean, btnSim: String, btnNao: String) {
        //exibeBtnOpcoes - se for não, vai exibir apenas o botão com OK, sem opção. Senão, exibe dois botões e pega os textos deles de btnSim e btnNao

        //EXIBIR POPUP
        // Initialize a new layout inflater instance
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate a custom view using layout inflater
        val view = inflater.inflate(R.layout.popup_model,null)

        // Initialize a new instance of popup window
        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )



        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }


        // If API level 23 or higher then execute the code
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

        }


        // Get the widgets reference from custom view
        val buttonPopupN = view.findViewById<Button>(R.id.btnReclamar)
        val buttonPopupS = view.findViewById<Button>(R.id.BtnRecebimento)
        val buttonPopupOk = view.findViewById<Button>(R.id.popupBtnOk)
        val txtTitulo = view.findViewById<TextView>(R.id.popupTitulo)
        val txtTexto = view.findViewById<TextView>(R.id.popupTexto)
        val progressbar = view.findViewById<ProgressBar>(R.id.progressBar)
        val txBarra = view.findViewById<TextView>(R.id.popupMsg)

        txBarra.visibility = View.GONE
        progressbar.visibility = View.GONE

        if (exibeBtnOpcoes){
            //vai exibir os botões com textos e esconder o btn ok
            buttonPopupOk.visibility = View.GONE
            //exibe e ajusta os textos dos botões
            buttonPopupN.text = btnNao
            buttonPopupS.text = btnSim

            // Set a click listener for popup's button widget
            buttonPopupN.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

        } else {

            //vai esconder os botões com textos e exibir o btn ok
            buttonPopupOk.visibility = View.VISIBLE
            //exibe e ajusta os textos dos botões
            buttonPopupN.visibility = View.GONE
            buttonPopupS.visibility = View.GONE


            buttonPopupOk.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

        }

        txtTitulo.text = titulo
        txtTexto.text = texto


        // Set a dismiss listener for popup window
        popupWindow.setOnDismissListener {
            //Fecha a janela ao clicar fora também
            popupWindow.dismiss()
        }

        //lay_root é o layout parent que vou colocar a popup
        val lay_root: ConstraintLayout = findViewById(R.id.layPerfil)

        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(lay_root)
        popupWindow.showAtLocation(
            lay_root, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

    }

    fun updateUserPoints(pontosNovos: Int){


        pontos = pontos.toInt()+pontosNovos
        SharePreferences.setPoints(this, pontos)
        //databaseReference.child("usuarios").child(userBd).child("pontos").setValue(pontos.toString())
        Toast.makeText(this, "Parabens! você ganhou "+pontosNovos.toString(), Toast.LENGTH_SHORT).show()

    }

    fun ChamaDialog() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        ) //este serve para bloquear cliques que pdoeriam dar erros
        val layout = findViewById<RelativeLayout>(R.id.LayoutProgressBar)
        layout.visibility = View.VISIBLE
        val spinner = findViewById<ProgressBar>(R.id.progressBar1)
        spinner.visibility = View.VISIBLE
    }

    //este método torna invisivel um layout e encerra o dialogbar spinner.
    fun EncerraDialog() {
        val layout = findViewById<RelativeLayout>(R.id.LayoutProgressBar)
        val spinner = findViewById<ProgressBar>(R.id.progressBar1)
        layout.visibility = View.GONE
        spinner.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) //libera os clicks
    }


}