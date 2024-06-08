package ee.oyatl.ime.make.module.candidates

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ee.oyatl.ime.make.databinding.CandidatesItemBinding

class BasicCandidatesAdapter(
    private val context: Context,
    private val onClick: (Candidate) -> Unit,
): ListAdapter<Candidate, BasicCandidatesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CandidatesItemBinding.inflate(LayoutInflater.from(context)))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item)
        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }

    class ViewHolder(
        val binding: CandidatesItemBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun onBind(candidate: Candidate) {
            this.binding.text.text = candidate.text
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<Candidate>() {
        override fun areItemsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem.text == newItem.text && oldItem.score == newItem.score
        }
    }

}