package com.example.firstdemo.customview

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 拖动排序 + 侧滑删除 —— ItemTouchHelper 实战(最常用的"拖拽")。
 *
 * 你只写两件事:
 *   onMove   → 把数据换位 + notifyItemMoved(拖动排序)
 *   onSwiped → 把数据删掉 + notifyItemRemoved(侧滑删除)
 * 拖动的位移、松手归位动画、侧滑消失动画,ItemTouchHelper 全帮你做了。
 */
private class ReorderAdapter(
    val items: MutableList<String>,
) : RecyclerView.Adapter<ReorderAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val d = parent.resources.displayMetrics.density
        val tv = TextView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setPadding((20 * d).toInt(), (18 * d).toInt(), (20 * d).toInt(), (18 * d).toInt())
            textSize = 16f
            // 不透明背景:侧滑时不会露出下面的内容
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.parseColor("#222222"))
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = items[position]
    }

    override fun getItemCount() = items.size

    /** 拖动排序:把 from 位置的项移到 to(支持跨多格,不只相邻)。 */
    fun move(from: Int, to: Int) {
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }

    /** 侧滑删除。 */
    fun removeAt(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }
}

@Composable
fun ReorderListDemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("RecyclerView 拖动排序 + 侧滑删除", style = MaterialTheme.typography.titleMedium)
        Text(
            "长按某项拖动重排;左右滑动删除(ItemTouchHelper)。",
            style = MaterialTheme.typography.bodySmall,
        )

        AndroidView(
            factory = { ctx ->
                val data = MutableList(12) { "第 ${it + 1} 项 · 长按拖我 / 侧滑删我" }
                val adapter = ReorderAdapter(data)
                val rv = RecyclerView(ctx).apply {
                    layoutManager = LinearLayoutManager(ctx)
                    this.adapter = adapter
                    addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
                }
                val density = ctx.resources.displayMetrics.density

                val callback = object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,       // 允许上下拖(排序)
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,    // 允许左右滑(删除)
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder,
                    ): Boolean {
                        adapter.move(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        adapter.removeAt(viewHolder.bindingAdapterPosition)
                    }

                    // 拖起时抬高一点,给"被拿起来"的反馈
                    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                        super.onSelectedChanged(viewHolder, actionState)
                        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                            viewHolder?.itemView?.translationZ = 8f * density
                        }
                    }

                    // 松手复位高度
                    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                        super.clearView(recyclerView, viewHolder)
                        viewHolder.itemView.translationZ = 0f
                    }
                }
                ItemTouchHelper(callback).attachToRecyclerView(rv)
                rv
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}
