package ee.oyatl.ime.make.settings

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ee.oyatl.ime.make.databinding.ListitemKeyboardLayoutComponentPreviewRowBinding
import ee.oyatl.ime.make.preset.InputEnginePreset
import ee.oyatl.ime.make.preset.PresetLoader

class KeyboardComponentsAdapter(
    val context: Context,
    val previewMode: Boolean = false,
): ListAdapter<InputEnginePreset, KeyboardComponentsAdapter.ViewHolder>(DiffCallback()) {
    val loader: PresetLoader = PresetLoader(context)

    var onItemLongPress: (ViewHolder) -> Unit = {}
    var onItemMenuPress: (ItemMenuType, ViewHolder) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListitemKeyboardLayoutComponentPreviewRowBinding
            .inflate(LayoutInflater.from(context), null, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ListitemKeyboardLayoutComponentPreviewRowBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        private val gestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val visible = binding.menuOverlay.visibility == View.VISIBLE
                binding.menuOverlay.visibility = if(visible) View.GONE else View.VISIBLE
                return true
            }
            override fun onLongPress(e: MotionEvent) = onItemLongPress(this@ViewHolder)
        })
        @SuppressLint("ClickableViewAccessibility")
        fun onBind(preset: InputEnginePreset) {
            val context = binding.root.context
            val engine = loader.mod(preset).inflate(
                context = context,
                rootListener = KeyboardLayoutSettingsActivity.emptyInputEngineListener,
                disableTouch = !previewMode
            )
            val view = engine.initView(context)
            engine.onReset()
            engine.onResetComponents()
            if(!previewMode) view?.setOnTouchListener { _, e -> gestureDetector.onTouchEvent(e) }
            binding.componentWrapper.removeAllViews()
            binding.componentWrapper.addView(view)

            binding.btnMoveUp.setOnClickListener {
                onItemMenuPress(ItemMenuType.MoveUp, this)
            }
            binding.btnMoveDown.setOnClickListener {
                onItemMenuPress(ItemMenuType.MoveDown, this)
            }
            binding.btnRemove.setOnClickListener {
                onItemMenuPress(ItemMenuType.Remove, this)
            }
            binding.btnEdit.setOnClickListener {
            }
            binding.btnEdit.visibility = View.GONE
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<InputEnginePreset>() {
        override fun areItemsTheSame(
            oldItem: InputEnginePreset,
            newItem: InputEnginePreset
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: InputEnginePreset,
            newItem: InputEnginePreset
        ): Boolean {
            return oldItem == newItem
        }
    }

    enum class ItemMenuType {
        Remove, Edit, MoveUp, MoveDown;
    }
}