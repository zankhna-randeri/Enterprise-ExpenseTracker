package com.avengers.enterpriseexpensetracker.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.ConversationAdapter
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage
import com.avengers.enterpriseexpensetracker.modal.response.ApiResponse
import com.avengers.enterpriseexpensetracker.modal.response.ReceiptScanResponse
import com.avengers.enterpriseexpensetracker.receiver.ApiResponseReceiver
import com.avengers.enterpriseexpensetracker.service.EETrackerJobService
import com.avengers.enterpriseexpensetracker.ui.speech.SpeechRecognitionListener
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.Utility
import com.avengers.enterpriseexpensetracker.viewmodel.AddExpenseViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AddExpenseFragment : Fragment(), View.OnClickListener {
    private var btnVoice: AppCompatImageButton? = null
    private var btnUpload: Button? = null
    private var conversationView: RecyclerView? = null
    private var progress: LinearLayout? = null
    private var txtProgressMsg: TextView? = null
    private lateinit var emptyView: TextView

    private var addExpenseViewModel: AddExpenseViewModel? = null
    private var conversationAdapter: ConversationAdapter? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognitionListener: SpeechRecognitionListener? = null
    private var speechRecognizerIntent: Intent? = null
    private var receiptScanResponseReceiver: BroadcastReceiver? = null
    private var submitExpenseResponseReceiver: BroadcastReceiver? = null
    private var expenseReport: ExpenseReport? = null
    private val conversations = ArrayList<VoiceMessage>()

    private var cameraImagePhotoPath: String? = null
    private var isListening = false
    private var expenseType: String? = null

    //    private var receiptImageUri: Uri? = null
    private val PERMISSION_RECORD_AUDIO = 1
    private val PERMISSION_MULTIPLE_REQUEST = 2
    private val ACTION_CAMERA = 3
    private val ACTION_PHOTOS = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        addExpenseViewModel = ViewModelProvider(this).get(AddExpenseViewModel::class.java)
        return inflater.inflate(R.layout.fragment_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        conversationView = view.findViewById(R.id.conversationView)
        conversationView?.layoutManager = LinearLayoutManager(activity)
        btnUpload = view.findViewById(R.id.btnUpload)
        btnUpload?.setOnClickListener(this)
        btnVoice = view.findViewById(R.id.btnListen)
        btnVoice?.setOnClickListener(this)
        progress = view.findViewById(R.id.lyt_progress)
        txtProgressMsg = progress?.findViewById(R.id.txt_progress_msg)
        emptyView = view.findViewById(R.id.emptyView)

        initBroadcast()
        initSpeechRecognizer()
        setUpConversations()
        setUpObservers()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initBroadcast() {
        receiptScanResponseReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                updateVoiceBotData(response as ReceiptScanResponse)
            }

            override fun onFailure(context: Context?, message: String?) {
                context?.let { Utility.getInstance().showMsg(it, message) }
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                hideLoadingView()

                val response = intent?.getParcelableExtra<ReceiptScanResponse>(Constants.EXTRA_API_RESPONSE)
                response?.let {
                    Log.d("EETracker *******", "response $response")
                    val statusSuccess = response.isApiResponseSuccess() ?: false
                    if (statusSuccess) {
                        onSuccess(context, response)
                    } else {
                        onFailure(context, context?.getString(R.string.txt_api_failed))
                    }
                }
            }
        }

        submitExpenseResponseReceiver = object : ApiResponseReceiver() {
            override fun onSuccess(context: Context?, response: ApiResponse) {
                context?.let { Utility.getInstance().showMsg(it, response.getMessage()) }
                view?.findNavController()?.navigateUp()
            }

            override fun onFailure(context: Context?, message: String?) {
                context?.let { Utility.getInstance().showMsg(it, message) }
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                hideLoadingView()

                val response = intent?.getParcelableExtra<ApiResponse>(Constants.EXTRA_API_RESPONSE)
                response?.let {
                    Log.d("EETracker *******", "response $response")
                    if (response.isSuccess()) {
                        onSuccess(context, response)
                    } else {
                        onFailure(context, context?.getString(R.string.txt_api_failed))
                    }
                }
            }
        }
    }

    private fun setUpConversations() {
        val conversationObserver = Observer<ArrayList<VoiceMessage>>() {
            Log.d("EETracker *******", "Inside observer")
            conversations.clear()
            if (!it.isNullOrEmpty()) {
                hideEmptyView()
                conversations.addAll(it)
            } else {
                showEmptyView()
            }
            if (conversationAdapter == null) {
                conversationAdapter = ConversationAdapter(conversations)
                conversationView?.adapter = conversationAdapter
            } else {
                conversationAdapter?.notifyDataSetChanged()
            }
        }
        addExpenseViewModel?.getConversation()?.observe(viewLifecycleOwner, conversationObserver)
    }

    private fun setUpObservers() {
        addExpenseViewModel?.getUploadButtonVisibility()?.observe(viewLifecycleOwner, Observer {
            btnUpload?.visibility = if (it) View.VISIBLE else View.INVISIBLE
        })

        addExpenseViewModel?.getExpenseType()?.observe(viewLifecycleOwner, Observer {
            expenseType = it
        })

        addExpenseViewModel?.getExpenseReport()?.observe(viewLifecycleOwner, Observer {
            expenseReport = it
            // TODO : Show Progress dialog and disable mic and upload buttons
            if (expenseReport != null) {
                submitReport(expenseReport!!)
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    promptSpeechInput()
                } else {
                    context?.let {
                        Utility.getInstance().showMsg(it, getString(R.string.tts_permission_denied))
                    }
                }
            }
            PERMISSION_MULTIPLE_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    val i = 0
                    while (i < grantResults.size) {
                        if (permissions[i] == Manifest.permission.CAMERA &&
                            grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d("EETRacker *******", "Camera permission granted ")
                        } else if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d("EETRacker *******", "Storage write permission granted ")
                        } else if (permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d("EETRacker *******", "Read storage permission granted ")
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ACTION_CAMERA -> handleSelectImageResponse(isCamera = true, data = data)
                ACTION_PHOTOS -> handleSelectImageResponse(isCamera = false, data = data)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        receiptScanResponseReceiver?.let { receiver ->
            val intentFilter = IntentFilter(Constants.BROADCAST_RECEIPT_SCAN_RESPONSE)
            activity?.applicationContext?.let {
                LocalBroadcastManager.getInstance(it)
                        .registerReceiver(receiver, intentFilter)
            }
        }

        submitExpenseResponseReceiver?.let { receiver ->
            val intentFilter = IntentFilter(Constants.BROADCAST_SUBMIT_EXPENSE_REPORT_RESPONSE)
            activity?.applicationContext?.let {
                LocalBroadcastManager.getInstance(it)
                        .registerReceiver(receiver, intentFilter)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        receiptScanResponseReceiver?.let { receiver ->
            activity?.applicationContext?.let {
                LocalBroadcastManager.getInstance(it).unregisterReceiver(receiver)
            }
        }

        submitExpenseResponseReceiver?.let { receiver ->
            activity?.applicationContext?.let {
                LocalBroadcastManager.getInstance(it).unregisterReceiver(receiver)
            }
        }
    }

    private fun handleSelectImageResponse(isCamera: Boolean, data: Intent?) {
        try {
            val receiptPath = if (isCamera) {
                handleCameraResponse()
            } else {
                handlePhotosResponse(data)
            }
            // Do nothing if failed to get image path
            if (receiptPath.isNullOrBlank()) {
                activity?.applicationContext?.let {
                    Utility.getInstance().showMsg(it, getString(R.string.txt_failed_image_path))
                }
                return
            }
            // Send to webservice
            val intent = Intent(activity?.applicationContext, EETrackerJobService::class.java).apply {
                putExtra(Constants.EXTRA_RECEIPT_PATH, receiptPath)
                putExtra(Constants.EXTRA_EXPENSE_TYPE, expenseType)
                action = Constants.ACTION_RECEIPT_SCAN
            }
            Utility.getInstance().startExpenseTrackerService(context, intent)
            showUploadProgress()
        } catch (e: Exception) {
            Log.e("EETracker *******", "Exception in handleSelectImageResponse ${e.message}")
            e.printStackTrace()
            hideLoadingView()
        }
    }

    private fun handleCameraResponse(): String? {
        if (cameraImagePhotoPath.isNullOrBlank()) {
            return null
        }

        val file = File(cameraImagePhotoPath)
        var photo: Bitmap? = null
        try {
            photo = if (activity != null) MediaStore.Images.Media.getBitmap(activity!!.contentResolver,
                    Uri.fromFile(file)) else null
        } catch (e: IOException) {
            Log.e("EETracker *******", " handleCameraResponse: ${e.message}")
            e.printStackTrace()
        }
        /*if (photo != null) {
            cardImage.setImageBitmap(photo)
        }*/
        val receiptImageUri = getImageUri(activity?.applicationContext, photo)
        return getRealPathFromURI(receiptImageUri)
    }

    private fun handlePhotosResponse(data: Intent?): String? {
        val receiptImageUri = data?.data
        //cardImage.setImageURI(cardImageUri)
        Log.d("EETracker *******", "handlePhotoResponse cardImageUri: $receiptImageUri")
        return getPathFromPhotoURL(receiptImageUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer?.destroy()
        speechRecognitionListener?.shutDownTTs()
    }

    private fun initSpeechRecognizer() {
        speechRecognitionListener =
            SpeechRecognitionListener(
                    activity,
                    addExpenseViewModel)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice bot is now listening your command")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity?.packageName)
        }
        speechRecognizer?.setRecognitionListener(speechRecognitionListener)
    }

    private fun promptSpeechInput() {
        if (!isListening) {
            speechRecognizer?.startListening(speechRecognizerIntent)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity?.applicationContext?.let {
                ContextCompat.checkSelfPermission(it,
                        permission)
            } == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            btnVoice?.id -> {
                handleVoiceButtonClick()
            }
            btnUpload?.id -> {
                handleUploadClick()
            }
        }
    }

    private fun handleUploadClick() {
        if (hasRequiredPermissions()) {
            selectImage()
        }
    }

    private fun selectImage() {
        val options = arrayOfNulls<String>(3)
        options[0] = getString(R.string.txt_click_pic)
        options[1] = getString(R.string.txt_select_photos)
        options[2] = getString(R.string.txt_cancel)
        val builder = context?.let { AlertDialog.Builder(ContextThemeWrapper(it, R.style.AlertDialogTheme)) }
        builder?.setTitle(getString(R.string.txt_select_img_dialog_title))
        builder?.setItems(options) { dialog, item ->
            when {
                options[item].equals(getString(R.string.txt_click_pic)) -> {
                    openCamera()
                }
                options[item].equals(getString(R.string.txt_select_photos)) -> {
                    val pickPhoto = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, ACTION_PHOTOS)
                }
                options[item].equals(getString(R.string.txt_cancel)) -> {
                    dialog.dismiss()
                }
            }
        }

        builder?.show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity != null) {
            if (activity?.packageManager?.let { takePictureIntent.resolveActivity(it) } != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {
                    Log.e("EETracker *******", " openCamera: ${e.message}")
                    e.printStackTrace()
                }
                if (photoFile != null) {
                    val photoURI = activity?.applicationContext?.let {
                        FileProvider.getUriForFile(it,
                                "com.avengers.enterpriseexpensetracker.fileprovider",
                                photoFile)
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, ACTION_CAMERA)
                }
            }
        }
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
        } else {
            java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
        }
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = activity?.applicationContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        cameraImagePhotoPath = image.absolutePath
        Log.d("EETracker *******", cameraImagePhotoPath)
        return image
    }

    private fun getImageUri(inContext: Context?, inImage: Bitmap?): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext?.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun hasRequiredPermissions(): Boolean {
        val permissionToRequest = ArrayList<String>()
        if (!hasPermission(Manifest.permission.CAMERA)) {
            permissionToRequest.add(Manifest.permission.CAMERA)
        }

        if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionToRequest.isNotEmpty()) {
            requestPermissions(permissionToRequest.toArray(arrayOfNulls(permissionToRequest.size)),
                    PERMISSION_MULTIPLE_REQUEST)
            return false
        }

        return true
    }

    private fun handleVoiceButtonClick() {
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            activity?.let { it ->
                ActivityCompat.requestPermissions(it,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_RECORD_AUDIO)
            }
        } else {
            promptSpeechInput()
        }
    }

    private fun getRealPathFromURI(uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        var path: String? = null
        val cursor = activity?.contentResolver?.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            path = cursor.getString(idx)
            cursor.close()
        }

        return path
    }

    private fun getPathFromPhotoURL(uri: Uri?): String? {
        if (uri == null) {
            return null
        }

        var path: String? = null
        var cursor: Cursor? = null
        try {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            cursor = activity?.contentResolver?.query(uri, filePathColumn, null, null, null)
            path = if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0])
                cursor.getString(columnIndex)
            } else {
                uri.path
            }
        } catch (e: Exception) {
            Log.e("EETracker *******", " getPathFromPhotoURL: ${e.message}")
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        Log.d("EETracker *******", " getPathFromPhotoURL: $path")
        return path
    }

    private fun updateVoiceBotData(receiptScanResponse: ReceiptScanResponse) {
        speechRecognitionListener?.updateVoiceBotWithScanResponse(receiptScanResponse)
    }

    private fun submitReport(expenseReport: ExpenseReport) {
        try {
            expenseReport.setReportStatus(Constants.Companion.Status.Pending.name.toLowerCase())
            EETrackerPreferenceManager.getUserEmail(activity?.applicationContext)
                    ?.let { expenseReport.setEmailId(it) }

            showSubmitProgress()

            // Send to webservice
            val intent = Intent(activity?.applicationContext, EETrackerJobService::class.java).apply {
                putExtra(Constants.EXTRA_EXPENSE_REPORT, expenseReport)
                action = Constants.ACTION_SUBMIT_EXPENSE_REPORT
            }
            Utility.getInstance().startExpenseTrackerService(context, intent)
        } catch (e: Exception) {
            Log.e("EETracker *******", "Exception in submitReport ${e.message}")
            e.printStackTrace()
            hideLoadingView()
        }
    }

    private fun showUploadProgress() {
        progress?.visibility = View.VISIBLE
        txtProgressMsg?.text = getString(R.string.txt_upload_progress)
    }

    private fun showSubmitProgress() {
        progress?.visibility = View.VISIBLE
        txtProgressMsg?.text = getString(R.string.txt_submit_report_progress)
    }

    private fun hideLoadingView() {
        progress?.visibility = View.GONE
    }

    private fun showEmptyView() {
        emptyView.text = getString(R.string.txt_empty_chatbot)
        emptyView.visibility = View.VISIBLE
    }

    private fun hideEmptyView() {
        emptyView.visibility = View.GONE
    }
}
