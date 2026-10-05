package com.example.pocketdex.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.R

/** UI assets only, with no destination, route or business state. */
enum class PocketDexIconAsset(val drawableRes: Int) {
    ArrowBack(R.drawable.pd_arrow_back),
    ChevronDown(R.drawable.pd_chevron_down),
    Close(R.drawable.pd_close),
    Comparator(R.drawable.pd_comparator),
    Filter(R.drawable.pd_filter),
    Heart(R.drawable.pd_heart),
    HeartFilled(R.drawable.pd_heart_filled),
    Home(R.drawable.pd_home),
    HomeFilled(R.drawable.pd_home_filled),
    Quiz(R.drawable.pd_quiz),
    QuizFilled(R.drawable.pd_quiz_filled),
    Refresh(R.drawable.pd_refresh),
    Search(R.drawable.pd_search),
}

/** Keep exact geometry; semantic tint follows the accessible light theme. */
@Composable
fun PocketDexIcon(
    asset: PocketDexIconAsset,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(painterResource(asset.drawableRes), contentDescription, modifier.size(24.dp), tint)
}
