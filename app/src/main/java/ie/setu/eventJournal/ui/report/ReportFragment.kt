package ie.setu.eventJournal.ui.report

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.setu.eventJournal.R
import ie.setu.eventJournal.adapters.EventAdapter
import ie.setu.eventJournal.adapters.EventClickListener
import ie.setu.eventJournal.databinding.FragmentReportBinding
import ie.setu.eventJournal.models.EventModel
import ie.setu.eventJournal.ui.auth.LoggedInViewModel
import ie.setu.eventJournal.utils.*
import java.util.Locale

class ReportFragment : Fragment(), EventClickListener {

    private var _fragBinding: FragmentReportBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val reportViewModel: ReportViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var eventAdapter: EventAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentReportBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        loader = createLoader(requireActivity())

        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        fragBinding.fab.setOnClickListener {
            val action = ReportFragmentDirections.actionReportFragmentToEventFragment()
            findNavController().navigate(action)
        }
        showLoader(loader,"Downloading Events")


        // Initialize the adapter
        eventAdapter = EventAdapter(ArrayList(), this, reportViewModel.readOnly.value!!)

        // Set the adapter on the RecyclerView
        fragBinding.recyclerView.adapter = eventAdapter

        reportViewModel.observableEventsList.observe(viewLifecycleOwner, Observer {
                events ->
            events?.let {
                render(events as ArrayList<EventModel>)
                eventAdapter.notifyDataSetChanged()
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })

        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader,"Deleting Event")
                val adapter = fragBinding.recyclerView.adapter as EventAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                reportViewModel.delete(reportViewModel.liveFirebaseUser.value?.uid!!,
                    (viewHolder.itemView.tag as EventModel).uid)

                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEventClick(viewHolder.itemView.tag as EventModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportViewModel.eventChanged.observe(viewLifecycleOwner) { changedEvent ->
            // Find the position of the changed event in the list
            val position = reportViewModel.observableEventsList.value?.indexOfFirst { it.uid == changedEvent.uid }
            position?.let { pos ->
                // Notify the adapter that the item at the changed position needs to be updated
                fragBinding.recyclerView.adapter?.notifyItemChanged(pos)
            }
        }

        fragBinding.recyclerView.adapter = EventAdapter(ArrayList(), this, reportViewModel.readOnly.value!!)
    }


    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_report, menu)

                // Toggle Events
                val item = menu.findItem(R.id.toggleEvents) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val toggleEvents: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                toggleEvents.isChecked = false

                toggleEvents.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) reportViewModel.loadAll()
                    else reportViewModel.load()
                }

                // Search Events
                val searchManager =
                    requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        val filteredList = ArrayList<EventModel>()
                        reportViewModel.observableEventsList.value!!.forEach {
                            if (it.type.contains(query.orEmpty(), true)) {
                                filteredList.add(it)
                            }
                        }
                        eventAdapter.filterList(filteredList)
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })

                // Reference to get toggle to hide when search is active: https://stackoverflow.com/questions/7397391/event-for-handling-the-focus-of-the-edittext
                searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                    // Enable/Disable the toggle button when the search view is not focused// Disable the toggle button when the search view is focused
                    toggleEvents.post {
                        toggleEvents.isVisible = !hasFocus
                    }
                }

                // Reference to get events to reload when search bar was closed: https://stackoverflow.com/questions/52765209/menuitem-setonactionexpandlistener-with-kotlin
                searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                        return true
                    }

                    override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                        // Refresh the page when collapsed so events reload
                        reportViewModel.load()
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }



    private fun render(eventsList: ArrayList<EventModel>) {
        eventAdapter = EventAdapter(eventsList, this, reportViewModel.readOnly.value!!)
        fragBinding.recyclerView.adapter = eventAdapter
//        fragBinding.recyclerView.adapter = EventAdapter(eventsList, this, reportViewModel.readOnly.value!!)
//        fragBinding.recyclerView.adapter = EventAdapter(eventsList,this,reportViewModel.readOnly.value!!)
        if (eventsList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.eventsNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.eventsNotFound.visibility = View.GONE
        }
    }

    override fun onEventClick(event: EventModel) {
        val action = ReportFragmentDirections.actionReportFragmentToEventDetailFragment(event.uid)
        if(!reportViewModel.readOnly.value!!)
            findNavController().navigate(action)
    }

    override fun onFavouriteClick(event: EventModel) {
        reportViewModel.toggleFavorite(event)
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<EventModel> = ArrayList()

        for (item in reportViewModel.observableEventsList.value!!) {
            if (item.type.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(context, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            eventAdapter.filterList(filteredList)
            fragBinding.recyclerView.invalidate()
        }
    }

    private fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader,"Downloading Events")
            if(reportViewModel.readOnly.value!!)
                reportViewModel.loadAll()
            else
                reportViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Events")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                reportViewModel.liveFirebaseUser.value = firebaseUser
                reportViewModel.load()
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}