package ie.setu.eventJournal.ui.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import ie.setu.eventJournal.R
import ie.setu.eventJournal.databinding.FragmentEventDetailBinding
import ie.setu.eventJournal.firebase.FirebaseImageManager
import ie.setu.eventJournal.ui.auth.LoggedInViewModel
import ie.setu.eventJournal.ui.report.ReportViewModel
import ie.setu.eventJournal.utils.hideSoftKeyboard
import ie.setu.eventJournal.utils.showImagePicker
import timber.log.Timber
import java.util.Calendar

class EventDetailFragment : Fragment() {

    private lateinit var detailViewModel: EventDetailViewModel
    private val args by navArgs<EventDetailFragmentArgs>()
    private var _fragBinding: FragmentEventDetailBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()

    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentEventDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        root.setOnTouchListener { _, _ ->
            hideSoftKeyboard()
            false
        }

        detailViewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)
        detailViewModel.observableEvent.observe(viewLifecycleOwner, Observer { render() })

        // Date picker
        fragBinding.editDate.setOnClickListener {
            hideSoftKeyboard()
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, pickedYear, pickedMonth, pickedDay ->
                val pickedDate = "${pickedDay}/${pickedMonth + 1}/${pickedYear}"
                fragBinding.editDate.setText(pickedDate)
            }, year, month, day).show()
        }

        fragBinding.editImageButton.setOnClickListener {
            hideSoftKeyboard()
            doSelectImage()
        }

        registerImagePickerCallback()

        // Drop down of event types
        val spinner = fragBinding.spinnerEventType
        val eventTypes = resources.getStringArray(R.array.eventTypesArray)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, eventTypes)
        spinner.setAdapter(adapter)
        spinner.setOnClickListener {
            hideSoftKeyboard()
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Toast.makeText(requireContext(), getString(R.string.event_type) + " " + eventTypes[position], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nothing
            }
        }

        // Time picker
        fragBinding.editTime.setOnClickListener {
            hideSoftKeyboard()
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, pickedHour, pickedMinute ->
                val pickedTime = String.format("%02d:%02d", pickedHour, pickedMinute)
                fragBinding.editTime.setText(pickedTime)
            }, hour, minute, true).show()
        }

        fragBinding.editEventButton.setOnClickListener {
            hideSoftKeyboard()
            val updatedEvent = fragBinding.eventvm?.observableEvent!!.value!!.copy(
                image = selectedImageUri.toString()
            )
            detailViewModel.updateEvent(
                loggedInViewModel.liveFirebaseUser.value?.uid!!,
                args.eventid, updatedEvent
            )
            findNavController().navigateUp()
        }

        fragBinding.deleteEventButton.setOnClickListener {
            hideSoftKeyboard()
            reportViewModel.delete(
                loggedInViewModel.liveFirebaseUser.value?.uid!!,
                detailViewModel.observableEvent.value?.uid!!
            )
            findNavController().navigateUp()
        }

        detailViewModel.observableEvent.observe(viewLifecycleOwner, Observer { event ->
            event?.let {
                // Update UI with the latest event details
                fragBinding.editName.setText(event.name)
                fragBinding.editDescription.setText(event.description)
                fragBinding.spinnerEventType.setText(event.type)
                fragBinding.editDate.setText(event.date)
                fragBinding.editTime.setText(event.time)
                fragBinding.editAmount.setText(event.amount.toString())

                // Update selected image with the current event's image URI
                selectedImageUri = Uri.parse(event.image)

                // Check if there's a selected image URI
                if (!selectedImageUri?.toString().isNullOrEmpty()) {
                    // Load the selected image into ImageView using Picasso
                    Picasso.get().load(selectedImageUri).into(fragBinding.imageView)
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
            fragBinding.spinnerEventType.setText(event.type)
            fragBinding.editDate.setText(event.date)
            fragBinding.editTime.setText(event.time)
            fragBinding.editAmount.setText(event.amount.toString())

            // Check if event's image is not null or empty
            if (!event.image.isNullOrEmpty()) {
                Picasso.get().load(event.image).into(fragBinding.imageView)
            }
        }

        fragBinding.eventvm = detailViewModel
        Timber.i("Retrofit fragBinding.eventvm == $fragBinding.eventvm")
    }

    override fun onResume() {
        super.onResume()
        detailViewModel.getEvent(
            loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.eventid
        )
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

                    AppCompatActivity.RESULT_CANCELED -> {}
                    else -> {}
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
