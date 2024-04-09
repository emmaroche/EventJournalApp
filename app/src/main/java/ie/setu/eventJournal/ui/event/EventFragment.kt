package ie.setu.eventJournal.ui.event

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import ie.setu.eventJournal.R
import ie.setu.eventJournal.databinding.FragmentEventBinding
import ie.setu.eventJournal.models.EventModel
import ie.setu.eventJournal.ui.auth.LoggedInViewModel
import ie.setu.eventJournal.ui.report.ReportViewModel

class EventFragment : Fragment() {

    private var totalDonated = 0
    private var _fragBinding: FragmentEventBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var eventViewModel: EventViewModel
    private val reportViewModel: ReportViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _fragBinding = FragmentEventBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        setupMenu()
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        eventViewModel.observableStatus.observe(viewLifecycleOwner, Observer { status ->
            status?.let { render(status) }
        })

        fragBinding.progressBar.max = 10000

        fragBinding.eventButton.setOnClickListener {
            val amount = if (fragBinding.paymentAmount.text.isNotEmpty())
                fragBinding.paymentAmount.text.toString().toInt() else TODO()
            if (totalDonated >= fragBinding.progressBar.max)
                Toast.makeText(context, "Amount Exceeded!", Toast.LENGTH_LONG).show()
            else {
                totalDonated += amount
                fragBinding.totalSoFar.text = String.format(getString(R.string.totalSoFar), totalDonated)
                fragBinding.progressBar.progress = totalDonated
                eventViewModel.addEvent(loggedInViewModel.liveFirebaseUser,
                    EventModel(amount = amount,
                        email = loggedInViewModel.liveFirebaseUser.value?.email!!))

            }
        }

        return root
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    // Uncomment this if you want to immediately return to Report
                    //findNavController().popBackStack()
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
        _fragBinding = null
    }

    override fun onResume() {
        super.onResume()
        totalDonated = reportViewModel.observableEventsList.value!!.sumOf { it.amount }
        fragBinding.progressBar.progress = totalDonated
        fragBinding.totalSoFar.text = String.format(getString(R.string.totalSoFar),totalDonated)
    }
}