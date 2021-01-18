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
            with(viewBinding.viewResult) {
                tvStartTime.text = item.startTime
                tvFinishTime.text = item.finishTime
                tvDistance.text = item.distance.formatMeter()
                tvAvgSpeed.text = item.avgSpeed.formatSpeedText()
                tvActiveTime.text = item.activeTime.formatTimeText()
            }
            with(viewBinding) {
                imgScreenShot.setImageURI(Uri.parse(item.screenShot))
                root.setOnClickListener {
                    onClickItem(item)
                }
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
}
