package com.example.kenv.trackme.presentation.rcv.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kenv.trackme.databinding.ViewRowWorkoutBinding
import com.example.kenv.trackme.domain.entity.WorkoutEntity
import com.example.kenv.trackme.presentation.utils.formatMeter
import com.example.kenv.trackme.presentation.utils.formatSpeedText
import com.example.kenv.trackme.presentation.utils.formatTimeText

/**
 * Created by Kenv on 22/12/2020.
 */

class WorkoutAdapter(private val onClickItem: (WorkoutEntity) -> Unit) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    class ViewHolder(private val viewBinding: ViewRowWorkoutBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bindView(item: WorkoutEntity, onClickItem: (WorkoutEntity) -> Unit) {
            viewBinding.viewResult.tvStartTime.text = item.startTime
            viewBinding.viewResult.tvFinishTime.text = item.finishTime
            viewBinding.viewResult.tvDistance.text = item.distance.formatMeter()
            viewBinding.viewResult.tvAvgSpeed.text = item.avgSpeed.formatSpeedText()
            viewBinding.viewResult.tvActiveTime.text = item.activeTime.formatTimeText()
            viewBinding.imgScreenShot.setImageURI(Uri.parse(item.screenShot))
            viewBinding.root.setOnClickListener {
                onClickItem(item)
            }
        }
    }

    private val data: MutableList<WorkoutEntity> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewRowWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(data[position], onClickItem)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(listWorkout: List<WorkoutEntity>) {
        data.clear()
        data.addAll(listWorkout)
        notifyDataSetChanged()
    }

    fun insert(workoutEntity: WorkoutEntity) {
        data.add(workoutEntity)
        notifyItemInserted(data.size - 1)
    }
}
