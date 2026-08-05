package com.example.firstdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firstdemo.canvasstudy.BlendDemoScreen
import com.example.firstdemo.canvasstudy.FlipCardDemoScreen
import com.example.firstdemo.canvasstudy.InlineTextDemoScreen
import com.example.firstdemo.canvasstudy.PathDemoScreen
import com.example.firstdemo.customview.CustomViewDemoScreen
import com.example.firstdemo.customview.FlowLayoutDemoScreen
import com.example.firstdemo.customview.PanZoomDemoScreen
import com.example.firstdemo.customview.TouchEventDemoScreen
import com.example.firstdemo.mvvm.PostDetailScreen
import com.example.firstdemo.mvvm.PostListScreen
import com.example.firstdemo.mvvm.PostMvvmScreen
import com.example.firstdemo.retrofitstudy.RetrofitDemoScreen
import com.example.firstdemo.ui.theme.FirstDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoTabs(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DemoTabs(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val titles = listOf("基础 Demo", "MVVM Demo", "列表刷新", "帖子详情", "Canvas", "混合", "图文", "翻牌", "进度环", "流式布局", "触摸", "看图")

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }
        when (selectedTab) {
            0 -> RetrofitDemoScreen()
            1 -> PostMvvmScreen()
            2 -> PostListScreen()
            3 -> PostDetailScreen()
            4 -> PathDemoScreen()
            5 -> BlendDemoScreen()
            6 -> InlineTextDemoScreen()
            7 -> FlipCardDemoScreen()
            8 -> CustomViewDemoScreen()
            9 -> FlowLayoutDemoScreen()
            10 -> TouchEventDemoScreen()
            11 -> PanZoomDemoScreen()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FirstDemoTheme {
        Greeting("Android")
    }
}