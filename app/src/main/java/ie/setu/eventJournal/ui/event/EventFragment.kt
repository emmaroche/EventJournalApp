package ie.setu.eventJournal.ui.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.squareup.picasso.Picasso
import ie.setu.eventJournal.R
import ie.setu.eventJournal.databinding.FragmentEventBinding
import ie.setu.eventJournal.firebase.FirebaseImageManager
import ie.setu.eventJournal.firebase.FirebaseImageManager.getEventImageUri
import ie.setu.eventJournal.models.EventModel
import ie.setu.eventJournal.ui.auth.LoggedInViewModel
import ie.setu.eventJournal.ui.map.MapsViewModel
import ie.setu.eventJournal.ui.report.ReportViewModel
import ie.setu.eventJournal.utils.hideSoftKeyboard
import ie.setu.eventJournal.utils.showImagePicker
import timber.log.Timber
import java.util.Calendar

class EventFragment : Fragment() {

    private var totalBudget = 0
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private var _fragBinding: FragmentEventBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var eventViewModel: EventViewModel
    private val reportViewModel: ReportViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _fragBinding = FragmentEventBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        root.setOnTouchListener { _, _ ->
            hideSoftKeyboard()
            false
        }

        setupMenu()
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        eventViewModel.observableStatus.observe(viewLifecycleOwner, Observer { status ->
            status?.let { render(status) }
        })

        fragBinding.progressBar.max = 10000

        fragBinding.chooseImage.setOnClickListener {
            doSelectImage()
        }

        registerImagePickerCallback()

        // Reference used to help with adding the date/calendar picker: https://stackoverflow.com/questions/51490354/datepicker-in-kotlin-showing-wrong-month
        fragBinding.editTextDate.setOnClickListener {
           hideSoftKeyboard()
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, pickedYear, pickedMonth, pickedDay ->
                val pickedDate = "${pickedDay}/${pickedMonth + 1}/${pickedYear}"
                fragBinding.editTextDate.setText(pickedDate)
            }, year, month, day).show()
        }

        // Reference used to help add drop down options for event types: https://www.geeksforgeeks.org/spinner-in-kotlin/
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

        // Reference used to help with adding the time picker: https://devendrac706.medium.com/timepickerdialog-android-studio-tutorial-time-picker-using-kotlin-in-android-studio-f9949732d62
        fragBinding.editTextTime.setOnClickListener {
            hideSoftKeyboard()
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, pickedHour, pickedMinute ->
                val pickedTime = String.format("%02d:%02d", pickedHour, pickedMinute)
                fragBinding.editTextTime.setText(pickedTime)
            }, hour, minute, true).show()
        }

        fragBinding.eventButton.setOnClickListener {
            hideSoftKeyboard()
            val amount = if (fragBinding.paymentAmount.text.isNotEmpty())
                fragBinding.paymentAmount.text.toString().toInt() else 0
            val eventName = fragBinding.editTextName.text.toString()
            val eventTime = fragBinding.editTextTime.text.toString()
            val eventDate = fragBinding.editTextDate.text.toString()
            val eventDescription = fragBinding.editTextDescription.text.toString()
            val eventType = fragBinding.spinnerEventType.text.toString()

            if (totalBudget >= fragBinding.progressBar.max)
                Toast.makeText(context, "Amount Exceeded!", Toast.LENGTH_LONG).show()
            else {
                totalBudget += amount
                fragBinding.totalSoFar.text = String.format(getString(R.string.totalSoFar), totalBudget)
                fragBinding.progressBar.progress = totalBudget

                // Pass the selected event image as a string
                val selectedImageUri = getEventImageUri().value.toString()

                eventViewModel.addEvent(
                    loggedInViewModel.liveFirebaseUser,
                    EventModel(
                        amount = amount,
                        email = loggedInViewModel.liveFirebaseUser.value?.email!!,
                        name = eventName,
                        description = eventDescription,
                        type = eventType,
                        date = eventDate,
                        time = eventTime,
                        image = selectedImageUri,
                        latitude = mapsViewModel.currentLocation.value!!.latitude,
                        longitude = mapsViewModel.currentLocation.value!!.longitude
                    )
                )
            }
        }

        return root
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    // Immediately return to Report Fragment
                    findNavController().navigate(R.id.reportFragment)
                }
            }
            false -> Toast.makeText(context, getString(R.string.eventError), Toast.LENGTH_LONG).show()
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_event, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return NavigationUI.onNavDestinationSelected(menuItem, requireView().findNavController())
            }
        }, viewLifecycleOwner, androidx.lifecycle.Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventViewModel.observableStatus.removeObservers(viewLifecycleOwner)
        _fragBinding = null
    }

    override fun onResume() {
        super.onResume()
        totalBudget = reportViewModel.observableEventsList.value!!.sumOf { it.amount }
        fragBinding.progressBar.progress = totalBudget
        fragBinding.totalSoFar.text = String.format(getString(R.string.totalSoFar), totalBudget)
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
        // Load the selected event image
        val picasso = Picasso.get()
        picasso.load(imageUri).into(fragBinding.locationImage)

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
