package com.troy.constrainlayoutrepro

import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.coil.CoilImage
import com.thedeanda.lorem.Lorem
import com.thedeanda.lorem.LoremIpsum
import com.troy.constrainlayoutrepro.ui.theme.ConstrainLayoutReproTheme
import java.util.Date
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConstrainLayoutReproTheme {
                Screen()
            }
        }
    }
}

enum class Nav(val title: String) {
    HOME("ConstrainLayout Bug Demo"),
    CONSTRAINT_LAYOUT_1("ConstrainLayout with Barriers"),
    CONSTRAINT_LAYOUT_2("ConstrainLayout without Barriers"),
    ROWS_AND_COLUMNS("Rows and Columns")
}

@Composable
fun Screen() {
    var navState by remember { mutableStateOf(Nav.HOME) }
    val items = remember {
        (1..200).map { Item(it) }
    }
    when (navState) {
        Nav.HOME -> HomeScreen(navState.title) { nav ->
            navState = nav
        }
        else -> ListOfItems(items = items, nav = navState) {
            navState = Nav.HOME
        }
    }
}

@Composable
fun HomeScreen(title: String, onNavigate: (Nav) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(title)
        })
    }) {
        Column(
            Modifier
                .padding(32.dp)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is to demonstrate the scrolling problems we found when using ConstraintLayout within LazyColumn items")
            Spacer(modifier = Modifier.height(20.dp))
            TextButton(onClick = { onNavigate(Nav.CONSTRAINT_LAYOUT_1) }, Modifier.padding(20.dp)) {
                Text("ConstraintLayout with Barriers")
            }
            TextButton(onClick = { onNavigate(Nav.CONSTRAINT_LAYOUT_2) }, Modifier.padding(20.dp)) {
                Text("ConstraintLayout without Barriers")
            }
            TextButton(onClick = { onNavigate(Nav.ROWS_AND_COLUMNS) }, Modifier.padding(20.dp)) {
                Text("Rows and Columns")
            }
        }
    }
}

@Composable
fun ListOfItems(items: List<Item>, nav: Nav, onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(nav.title)
        }, navigationIcon = {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.Filled.ArrowBack, "Back")
            }
        })
    }) {
        LazyColumn(reverseLayout = true) {
            items(items.size, key = { items[it].id }) { index ->
                val item = items[index]
                when (nav) {
                    Nav.HOME -> Unit
                    Nav.CONSTRAINT_LAYOUT_1 -> ConstraintLayoutWithBarriers(item)
                    Nav.CONSTRAINT_LAYOUT_2 -> ConstraintLayoutWithoutBarriers(item)
                    Nav.ROWS_AND_COLUMNS -> RowsAndColumns(item)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConstraintLayoutWithBarriers(item: Item) {
    var showOptions by remember { mutableStateOf(false) }
    ConstraintLayout(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .combinedClickable(onLongClick = { showOptions = true }) {}
    ) {
        val (date, header, image, preview, reference, icon, text, warning, link) = createRefs()
        val startBarrier = createStartBarrier(image, preview, reference, text)
        val endBarrier = createEndBarrier(image, preview, reference, text)
        val topBarrier = createTopBarrier(image, preview, reference, text)
        val bottomBarrier = createBottomBarrier(image, preview, reference, text)
        val maxWidth = Dimension.percent(.7f)
        DateHeader(item, Modifier.constrainAs(date) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        })
        ProfileIcon(item, Modifier.constrainAs(icon) {
            start.linkTo(parent.start)
            bottom.linkTo(bottomBarrier)
        })
        UserHeader(item, Modifier.constrainAs(header) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(date.bottom)
        })
        ReferenceCard(item, Modifier.constrainAs(reference) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(header.bottom)
            width = maxWidth
        })
        ImageCard(item, Modifier.constrainAs(image) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(reference.bottom)
            width = maxWidth
        })
        PreviewCard(item, Modifier.constrainAs(preview) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(image.bottom)
            width = maxWidth
        })
        TextCard(item, Modifier.constrainAs(text) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(preview.bottom)
            width = maxWidth
        })
        WarningIcon(item, Modifier.constrainAs(warning) {
            if (item.isOwned) {
                end.linkTo(startBarrier)
            } else {
                start.linkTo(endBarrier)
            }
            linkTo(topBarrier, bottomBarrier)
        })
        LinkLine(item, Modifier.constrainAs(link) {
            top.linkTo(bottomBarrier)
            start.linkTo(startBarrier)
        })
        OptionsMenu(showOptions) {
            showOptions = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConstraintLayoutWithoutBarriers(item: Item) {
    var showOptions by remember { mutableStateOf(false) }
    ConstraintLayout(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .combinedClickable(onLongClick = { showOptions = true }) {}
    ) {
        val (date, header, image, preview, reference, icon, text, warning, link) = createRefs()
        val maxWidth = Dimension.percent(.7f)
        DateHeader(item, Modifier.constrainAs(date) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        })
        ProfileIcon(item, Modifier.constrainAs(icon) {
            start.linkTo(parent.start)
            bottom.linkTo(text.bottom)
        })
        UserHeader(item, Modifier.constrainAs(header) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(date.bottom)
        })
        ReferenceCard(item, Modifier.constrainAs(reference) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(header.bottom)
            width = maxWidth
        })
        ImageCard(item, Modifier.constrainAs(image) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(reference.bottom)
            width = maxWidth
        })
        PreviewCard(item, Modifier.constrainAs(preview) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(image.bottom)
            width = maxWidth
        })
        TextCard(item, Modifier.constrainAs(text) {
            if (item.isOwned) {
                end.linkTo(parent.end)
            } else {
                start.linkTo(icon.end)
            }
            top.linkTo(preview.bottom)
            width = maxWidth
        })
        WarningIcon(item, Modifier.constrainAs(warning) {
            if (item.isOwned) {
                end.linkTo(text.start)
            } else {
                start.linkTo(text.end)
            }
            linkTo(header.top, text.bottom)
        })
        LinkLine(item, Modifier.constrainAs(link) {
            top.linkTo(text.bottom)
            start.linkTo(text.start)
        })
        OptionsMenu(showOptions) {
            showOptions = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowsAndColumns(item: Item) {
    var showOptions by remember { mutableStateOf(false) }
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        DateHeader(item, Modifier.align(Alignment.CenterHorizontally))
        Row {
            if (item.isOwned) {
                WarningIcon(item,
                    Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically))
            }
            ProfileIcon(item, Modifier.align(Alignment.Bottom))
            Column(
                Modifier
                    .weight(4f)
                    .combinedClickable(onLongClick = { showOptions = true }) {}) {
                UserHeader(item)
                ReferenceCard(item)
                ImageCard(item)
                PreviewCard(item)
                TextCard(item)
                OptionsMenu(showOptions) {
                    showOptions = false
                }
            }
            if (!item.isOwned) {
                WarningIcon(item,
                    Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically))
            }
        }
    }

}

@Composable
fun DateHeader(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(top = 2.dp)) {
        if (item.showDate) {
            Text(DateFormat.getLongDateFormat(LocalContext.current).format(Date()))
        }
    }
}

@Composable
fun ImageCard(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(top = 2.dp)) {
        if (item.showImage) {
            Card(backgroundColor = MaterialTheme.colors.background) {
                CoilImage(item.imageUrl, "Image", Modifier.fillMaxWidth(), contentScale = ContentScale.Crop)
            }
        }
    }
}

@Composable
fun LinkLine(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(top = 2.dp)) {
        if (item.showLink) {
            Text("Link to more")
        }
    }
}

@Composable
fun OptionsMenu(show: Boolean = false, onClose: () -> Unit) {
    DropdownMenu(show, onDismissRequest = { onClose() }) {
        DropdownMenuItem(onClick = { onClose() }) { Text("First Option") }
        DropdownMenuItem(onClick = { onClose() }) { Text("Second Option") }
        DropdownMenuItem(onClick = { onClose() }) { Text("Last Option") }
    }
}

@Composable
fun PreviewCard(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(top = 2.dp)) {
        if (item.showPreview) {
            Card(backgroundColor = MaterialTheme.colors.background) {
                Icon(
                    imageVector = Icons.Filled.Email, "Preview",
                    Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ProfileIcon(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp), Alignment.Center) {
        if (item.showIcon && !item.isOwned) {
            Icon(imageVector = Icons.Filled.Person, "Person", Modifier.size(35.dp))
        }
    }
}

@Composable
fun ReferenceCard(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(top = 2.dp)) {
        if (item.showReference) {
            Card(backgroundColor = MaterialTheme.colors.background) {
                Column {
                    CoilImage(data = item.referenceUrl, "Reference", Modifier.fillMaxWidth(), contentScale = ContentScale.Crop)
                    Text(text = item.title, Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.subtitle1)
                }
            }
        }
    }
}

@Composable
fun TextCard(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(top = 2.dp)) {
        if (item.showText) {
            Card(Modifier.align(if (item.isOwned) Alignment.TopEnd else Alignment.TopStart)) {
                Text(item.text, Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun UserHeader(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(top = 4.dp)) {
        if (item.showHeader) {
            Text(item.user, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun WarningIcon(item: Item, modifier: Modifier = Modifier) {
    Box(modifier.padding(4.dp)) {
        if (item.showWarning) {
            val iconModifier = if (item.isOwned) {
                Modifier.align(Alignment.CenterEnd)
            } else {
                Modifier.align(Alignment.CenterStart)
            }
            Icon(imageVector = Icons.Filled.Warning, "Warning", iconModifier, MaterialTheme.colors.error)
        }
    }
}

class Item(val id: Int) {
    val isOwned: Boolean = Random.nextBoolean()
    val showDate: Boolean = Random.nextBoolean()
    val showHeader: Boolean = Random.nextBoolean()
    val showImage: Boolean = Random.nextBoolean()
    val showPreview: Boolean = Random.nextBoolean()
    val showReference: Boolean = Random.nextBoolean()
    val showIcon: Boolean = Random.nextBoolean()
    val showText: Boolean = Random.nextBoolean()
    val showWarning: Boolean = Random.nextBoolean()
    val showLink: Boolean = Random.nextBoolean()

    val user: String by lazy { lorem.name }
    val text: String by lazy { lorem.getWords(2, 20) }
    val title: String by lazy { lorem.getTitle(2, 6) }
    val imageUrl: String = "https://picsum.photos/id/$id/200/300"
    val referenceUrl: String = "https://picsum.photos/id/$id/200/75"

    companion object {
        var lorem: Lorem = LoremIpsum.getInstance()
    }
}
