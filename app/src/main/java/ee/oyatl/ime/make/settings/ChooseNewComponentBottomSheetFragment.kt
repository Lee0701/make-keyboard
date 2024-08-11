package ee.oyatl.ime.make.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ee.oyatl.ime.make.databinding.BottomsheetChooseNewComponentBinding
import ee.oyatl.ime.make.databinding.ListitemBottomsheetChooseNewComponentBinding
import ee.oyatl.ime.make.preset.InputViewComponentType
import ee.oyatl.ime.make.service.Feature

class ChooseNewComponentBottomSheetFragment(
    private val onItemClicked: (InputViewComponentType) -> Unit,
): BottomSheetDialogFragment() {

    private var binding: BottomsheetChooseNewComponentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: BottomsheetChooseNewComponentBinding =
            BottomsheetChooseNewComponentBinding.inflate(inflater)
        val adapter = Adapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
        }
        val list = if(Feature.ComponentLanguageTabBar.enabled) InputViewComponentType.entries
        else InputViewComponentType.entries - InputViewComponentType.LanguageTabBar
        adapter.submitList(list)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    inner class Adapter: ListAdapter<InputViewComponentType, ViewHolder>(DiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding: ListitemBottomsheetChooseNewComponentBinding =
                ListitemBottomsheetChooseNewComponentBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(getItem(position))
        }
    }

    inner class ViewHolder(
        private val binding: ListitemBottomsheetChooseNewComponentBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun onBind(componentType: InputViewComponentType) {
            binding.icon.setImageResource(componentType.iconRes)
            binding.title.setText(componentType.titleRes)
            binding.root.setOnClickListener {
                onItemClicked(componentType)
                dismiss()
            }
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<InputViewComponentType>() {
        override fun areItemsTheSame(
            oldItem: InputViewComponentType,
            newItem: InputViewComponentType
        ): Boolean = oldItem === newItem

        override fun areContentsTheSame(
            oldItem: InputViewComponentType,
            newItem: InputViewComponentType
        ): Boolean = oldItem == newItem
    }

    companion object {
        const val TAG = "ChooseNewComponentBottomSheet"
    }
}