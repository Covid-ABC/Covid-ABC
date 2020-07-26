package com.ufabc.covidabc.mainScreen.categories.event

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.ufabc.covidabc.App
import com.ufabc.covidabc.R
import com.ufabc.covidabc.model.event.CalendarEvent
import com.ufabc.covidabc.model.event.CalendarEventDAO
import com.ufabc.covidabc.model.FirestoreDatabaseOperationListener
import kotlin.collections.ArrayList

class EventFragment : Fragment() {

    private lateinit var createEventFAB : FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var eventRecyclerView : RecyclerView
    private val mAuth : FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews(view)
        setListeners()

        setEvents()
    }

    private fun setViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_event)
        eventRecyclerView = requireView().findViewById(R.id.recycler_view_calendar)

        createEventFAB = view.findViewById(R.id.fab_event_post)

    }

    private fun setListeners() {
        createEventFAB.setOnClickListener{
            startActivity(Intent(App.appContext, CreateEventActivity::class.java))
        }

        swipeRefreshLayout.setOnRefreshListener {
            CalendarEventDAO.refreshFAQ(object : FirestoreDatabaseOperationListener<Boolean> {
                override fun onCompleted(sucess: Boolean) {
                    if (sucess) {
                        populateEvents(CalendarEventDAO.getEventArray())
                    }

                    swipeRefreshLayout.isRefreshing = false
                }
            })
        }

        mAuth.addAuthStateListener {

                val user = mAuth.getCurrentUser();
                createEventFAB.visibility = if (user != null) View.VISIBLE else View.INVISIBLE

        }
    }

    private fun setEvents() {
        if (CalendarEventDAO.isAlreadyFetched()) {
            populateEvents(CalendarEventDAO.getEventArray())
        }
        else {
            val dialog = ProgressDialog(this.context).apply {
                this.setMessage("Aguarde...")
                this.show()
            }

            CalendarEventDAO.refreshFAQ(object : FirestoreDatabaseOperationListener<Boolean> {
                override fun onCompleted(sucess: Boolean) {
                    when (sucess) {
                        true -> populateEvents(CalendarEventDAO.getEventArray())
                        false -> Toast.makeText(App.appContext, R.string.get_events_failure, Toast.LENGTH_LONG).show()
                    }

                    dialog.cancel()
                }
            })
        }

    }

    private fun populateEvents(eventArray : ArrayList<CalendarEvent>) {
        eventRecyclerView.apply {
            val recyclerView = this
            recyclerView!!.layoutManager = LinearLayoutManager(App.appContext)
            recyclerView.adapter = EventAdapter(eventArray)
        }
    }
}