package ee.oyatl.ime.make.view.candidates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.databinding.CandidateItemHorizontalBinding
import ee.oyatl.ime.make.databinding.CandidatesViewHorizontalBinding

class CandidatesViewManager(
    private val listener: Listener,
) {

    private var binding: CandidatesViewHorizontalBinding? = null
    private var adapter: Adapter? = null

    private var expanded = false

    fun initView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        val binding = CandidatesViewHorizontalBinding.inflate(inflater, null, false)
        val height = context.resources.getDimension(R.dimen.candidates_view_height).toInt()
        binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        val adapter = Adapter {
            listener.onCandidateClick(it)
            if(expanded) {
                expanded = false
                updateExpandedState(context)
            }
        }
        binding.recyclerView.adapter = adapter
        binding.expandWrapper.setOnClickListener {
            expanded = !expanded
            updateExpandedState(context)
        }
        this.adapter = adapter
        this.binding = binding
        return binding.root
    }

    private fun updateExpandedState(context: Context) {
        val binding = this.binding ?: return
        if(expanded) {
            listener.onExpand(binding.root)
            binding.expand.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
            binding.recyclerView.layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP)
        } else {
            listener.onCollapse(binding.root)
            binding.expand.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
            binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        val adapter = binding.recyclerView.adapter
        binding.recyclerView.adapter = null
        binding.recyclerView.adapter = adapter
    }

    fun getView(): View? {
        return this.binding?.root
    }

    fun showCandidates(candidates: List<String>) {
        val adapter = adapter ?: return
        adapter.submitList(candidates)
    }

    class Adapter(
        private val onClick: (Int) -> Unit,
    ): ListAdapter<String, ViewHolder>(ItemCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ViewHolder(CandidateItemHorizontalBinding.inflate(inflater, null, false))
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(getItem(position)) { onClick(position) }
        }
    }

    class ViewHolder(
        private val binding: CandidateItemHorizontalBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun onBind(text: String, onClick: () -> Unit) {
            binding.text.text = text
            binding.root.setOnClickListener { onClick() }
        }
    }

    class ItemCallback: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem === newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }

    interface Listener {
        fun onCandidateClick(position: Int)
        fun onExpand(candidatesView: View)
        fun onCollapse(candidatesView: View)
    }
}