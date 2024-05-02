package ie.setu.eventJournal.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import ie.setu.eventJournal.databinding.FragmentEventDetailBinding
import ie.setu.eventJournal.firebase.FirebaseImageManager
import ie.setu.eventJournal.ui.auth.LoggedInViewModel
import ie.setu.eventJournal.ui.report.ReportViewModel
import ie.setu.eventJournal.utils.showImagePicker
import timber.log.Timber

class EventDetailFragment : Fragment() {

    private lateinit var detailViewModel: EventDetailViewModel
    private val args by navArgs<EventDetailFragmentArgs>()
    private var _fragBinding: FragmentEventDetailBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val reportViewModel : ReportViewModel by activityViewModels()

    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentEventDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        detailViewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)
        detailViewModel.observableEvent.observe(viewLifecycleOwner, Observer { render() })

        // Click listener for chooseImage button
        fragBinding.editImageButton.setOnClickListener {
            doSelectImage()
        }

        // Register image picker callback
        registerImagePickerCallback()

        fragBinding.editEventButton.setOnClickListener {
            val updatedEvent = fragBinding.eventvm?.observableEvent!!.value!!.copy(
                image = selectedImageUri.toString()
            )
            detailViewModel.updateEvent(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                args.eventid, updatedEvent)
            findNavController().navigateUp()
        }

        fragBinding.deleteEventButton.setOnClickListener {
            reportViewModel.delete(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                detailViewModel.observableEvent.value?.uid!!)
            findNavController().navigateUp()
        }

        // Observe changes in the observableEvent
        detailViewModel.observableEvent.observe(viewLifecycleOwner, Observer { event ->
            event?.let {
                // Update UI with the latest event details
                fragBinding.editName.setText(event.name)
                fragBinding.editDescription.setText(event.description)
                fragBinding.editType.setText(event.type)
                fragBinding.editDate.setText(event.date)
                fragBinding.editTime.setText(event.time)
                fragBinding.editAmount.setText(event.amount.toString())

                // Check if there's a selected image URI
                selectedImageUri?.let { imageUri ->
                    // Load the selected image into ImageView using Picasso
                    Picasso.get().load(imageUri).into(fragBinding.imageView)
                }
            }
        })

        return root
    }

    private fun render() {
        val event = detailViewModel.observableEvent.value

        if (event != null) {
            fragBinding.editName.setText(event.name)
            fragBinding.editDescription.setText(event.description)
            fragBinding.editType.setText(event.type)
            fragBinding.editDate.setText(event.date)
            fragBinding.editTime.setText(event.time)
            fragBinding.editAmount.setText(event.amount.toString())
            Picasso.get().load(event.image).into(fragBinding.imageView)
        }

        fragBinding.eventvm = detailViewModel
        Timber.i("Retrofit fragBinding.eventvm == $fragBinding.eventvm")
    }
    override fun onResume() {
        super.onResume()
        detailViewModel.getEvent(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.eventid)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${result.data!!.data}")
                            val imageUri = result.data!!.data
                            if (imageUri != null) {
                                // Display the selected image
                                showSelectedImage(imageUri)
                            }
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> { }
                    else -> { }
                }
            }
    }

    private fun showSelectedImage(imageUri: Uri) {
        // Update selectedImageUri immediately
        selectedImageUri = imageUri

        // Load the selected event image into ImageView using Picasso
        Picasso.get().load(imageUri).into(fragBinding.imageView)

        // Upload the selected image to Firebase Storage
        FirebaseImageManager.uploadEventPictureToFirebase(
            loggedInViewModel.liveFirebaseUser.value!!.uid,
            bitmapFromUri(imageUri),
            true
        ).observe(viewLifecycleOwner, Observer { imageUrl ->
            // Store the download URL of the uploaded image
            selectedImageUri = Uri.parse(imageUrl)
        })
    }

    private fun bitmapFromUri(uri: Uri): Bitmap {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    fun doSelectImage() {
        showImagePicker(imageIntentLauncher, view)
    }
}
