package ie.setu.eventJournal.firebase

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import ie.setu.eventJournal.utils.customTransformation
import timber.log.Timber
import java.io.ByteArrayOutputStream
import com.squareup.picasso.Target

object FirebaseImageManager {

    private var storage = FirebaseStorage.getInstance().reference
    private var profileImageUri = MutableLiveData<Uri>() // Separates profile picture URI
    private var eventImageUri = MutableLiveData<Uri>() // Separates event picture URI

    fun checkStorageForExistingProfilePic(userid: String) {
        val imageRef = storage.child("photos").child("${userid}.jpg")
        val defaultImageRef = storage.child("homer.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                profileImageUri.value = task.result!!
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            profileImageUri.value = Uri.EMPTY
        }
    }

    fun uploadProfilePicToFirebase(userid: String, bitmap: Bitmap, updating: Boolean) {
        val imageRef = storage.child("photos").child("${userid}.jpg")
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            if (updating) {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        profileImageUri.value = task.result!!
                        FirebaseDBManager.updateProfilePicture(userid, profileImageUri.value.toString())
                    }
                }
            }
        }.addOnFailureListener {
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    profileImageUri.value = task.result!!
                    FirebaseDBManager.updateProfilePicture(userid, profileImageUri.value.toString())
                }
            }
        }
    }

    fun uploadEventPictureToFirebase(userid: String, bitmap: Bitmap, updating: Boolean) {
        val imageRef = storage.child("event-photos").child("${userid}.jpg")
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            if (updating) {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        eventImageUri.value = task.result!!
                        FirebaseDBManager.updateLocationImage(userid, eventImageUri.value.toString())
                    }
                }
            }
        }.addOnFailureListener {
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    eventImageUri.value = task.result!!
                    FirebaseDBManager.updateLocationImage(userid, eventImageUri.value.toString())
                }
            }
        }
    }

    fun updateUserImage(userid: String, imageUri: Uri?, imageView: ImageView, updating: Boolean) {
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(
                    bitmap: Bitmap?,
                    from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadProfilePicToFirebase(userid, bitmap!!, updating)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(
                    e: java.lang.Exception?,
                    errorDrawable: Drawable?
                ) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun updateDefaultProfilePicture(userid: String, resource: Int, imageView: ImageView) {
        Picasso.get().load(resource)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(
                    bitmap: Bitmap?,
                    from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadProfilePicToFirebase(userid, bitmap!!, false)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(
                    e: java.lang.Exception?,
                    errorDrawable: Drawable?
                ) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun getProfileImageUri(): MutableLiveData<Uri> {
        return profileImageUri
    }

    fun getEventImageUri(): MutableLiveData<Uri> {
        return eventImageUri
    }
}
