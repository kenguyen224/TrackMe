package com.example.kenv.trackme.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kenv.trackme.databinding.FragmentHistoryWorkoutBinding
import com.example.kenv.trackme.domain.transform.toLatLng
import com.example.kenv.trackme.presentation.activity.WorkoutComponentContract
import com.example.kenv.trackme.presentation.arguments.WorkoutReviewArgument
import com.example.kenv.trackme.presentation.extensions.safeObserve
import com.example.kenv.trackme.presentation.extensions.show
import com.example.kenv.trackme.presentation.rcv.VerticalSpaceItemDecoration
import com.example.kenv.trackme.presentation.rcv.adapter.WorkoutAdapter
import com.example.kenv.trackme.presentation.viewmodel.ListWorkoutViewModel
import com.example.kenv.trackme.presentation.viewmodel.ListWorkoutViewModelFactory
import javax.inject.Inject

/**
 * Created by Kenv on 12/12/2020.
 */

class HistoryWorkoutFragment : BaseFragment() {

    private lateinit var viewBinding: FragmentHistoryWorkoutBinding
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var navController: NavController

    @Inject
    lateinit var listWorkoutViewModelFactory: ListWorkoutViewModelFactory
    private val listWorkoutViewModel by viewModels<ListWorkoutViewModel> { listWorkoutViewModelFactory }

    override fun getBindingView(inflater: LayoutInflater, container: ViewGroup?): View {
        viewBinding = FragmentHistoryWorkoutBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val componentContract = context as? WorkoutComponentContract
            ?: throw IllegalAccessException("$context must implement WorkoutComponentContract")
        componentContract.getWorkoutComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        workoutAdapter = WorkoutAdapter {
            navController.navigate(
                HistoryWorkoutFragmentDirections.actionStartWorkoutReviewActivity(
                    WorkoutReviewArgument(
                        it.trackingLocation.toLatLng(),
                        it.startTime,
                        it.finishTime,
                        it.distance,
                        it.avgSpeed
                    )
                )
            )
        }
        viewBinding.rcvWorkouts.adapter = workoutAdapter
        with(viewBinding.rcvWorkouts) {
            adapter = workoutAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(VerticalSpaceItemDecoration())
        }
        viewBinding.btnRecord.setOnClickListener {
            navController.navigate(HistoryWorkoutFragmentDirections.actionStartRecording())
        }
        bindViewModel()
    }

    override fun onResume() {
        super.onResume()
        listWorkoutViewModel.getListWorkout()
    }

    private fun bindViewModel() = with(listWorkoutViewModel) {
        listWorkout.safeObserve(this@HistoryWorkoutFragment) {
            workoutAdapter.setData(it)
            if (it.isEmpty()) {
                viewBinding.tvEmpty.show(true)
                viewBinding.rcvWorkouts.show(false)
            } else {
                viewBinding.tvEmpty.show(false)
                viewBinding.rcvWorkouts.show(true)
            }
        }
    }
}
