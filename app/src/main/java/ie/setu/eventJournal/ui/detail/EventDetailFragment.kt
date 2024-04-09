package ie.setu.eventJournal.ui.detail

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ie.setu.eventJournal.databinding.FragmentEventDetailBinding
import ie.setu.eventJournal.ui.auth.LoggedInViewModel
import ie.setu.eventJournal.ui.report.ReportViewModel
import timber.log.Timber

class EventDetailFragment : Fragment() {

    private lateinit var detailViewModel: EventDetailViewModel
    private val args by navArgs<EventDetailFragmentArgs>()
    private var _fragBinding: FragmentEventDetailBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val reportViewModel : ReportViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentEventDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        detailViewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)
        detailViewModel.observableEvent.observe(viewLifecycleOwner, Observer { render() })

        fragBinding.editEventButton.setOnClickListener {
            detailViewModel.updateEvent(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                args.eventid, fragBinding.eventvm?.observableEvent!!.value!!)
            findNavController().navigateUp()
        }

        fragBinding.deleteEventButton.setOnClickListener {
            reportViewModel.delete(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                detailViewModel.observableEvent.value?.uid!!)
            findNavController().navigateUp()
        }

        return root
    }

    private fun render() {
        fragBinding.editName.setText("Event Name")
        fragBinding.editDescription.setText("Description")
        fragBinding.editType.setText("Type")
        fragBinding.editDate.setText("Date")
        fragBinding.editTime.setText("Time")
        fragBinding.editAmount.setText("0")
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
}