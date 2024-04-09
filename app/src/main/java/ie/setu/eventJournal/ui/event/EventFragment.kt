package ie.setu.eventJournal.ui.event

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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

    var totalDonated = 0
    private var _fragBinding: FragmentEventBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!
    private lateinit var eventViewModel: EventViewModel
    private val reportViewModel: ReportViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentEventBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        eventViewModel.observableStatus.observe(viewLifecycleOwner, Observer {
                status -> status?.let { render(status) }
        })

        fragBinding.progressBar.max = 10000
        fragBinding.amountPicker.minValue = 1
        fragBinding.amountPicker.maxValue = 1000

        fragBinding.amountPicker.setOnValueChangedListener { _, _, newVal ->
            //Display the newly selected number to paymentAmount
            fragBinding.paymentAmount.setText("$newVal")
        }
        setButtonListener(fragBinding)

        return root;
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    //Uncomment this if you want to immediately return to Report
                    //findNavController().popBackStack()
                }
            }
            false -> Toast.makeText(context,getString(R.string.eventError),Toast.LENGTH_LONG).show()
        }
    }

    fun setButtonListener(layout: FragmentEventBinding) {
        layout.eventButton.setOnClickListener {
            val amount = if (layout.paymentAmount.text.isNotEmpty())
                layout.paymentAmount.text.toString().toInt() else layout.amountPicker.value
            if(totalDonated >= layout.progressBar.max)
                Toast.makeText(context,"Amount Exceeded!", Toast.LENGTH_LONG).show()
            else {
                val paymentmethod = if(layout.paymentMethod.checkedRadioButtonId == R.id.Direct) "Direct" else "Paypal"
                totalDonated += amount
                layout.totalSoFar.text = String.format(getString(R.string.totalSoFar),totalDonated)
                layout.progressBar.progress = totalDonated
                eventViewModel.addEvent(EventModel(paymentmethod = paymentmethod,amount = amount,
                    email = loggedInViewModel.liveFirebaseUser.value?.email!!))
            }
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
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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