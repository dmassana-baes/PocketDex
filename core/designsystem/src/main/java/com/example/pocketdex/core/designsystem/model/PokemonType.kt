package com.example.pocketdex.core.designsystem.model

import androidx.compose.ui.graphics.Color
import com.example.pocketdex.core.designsystem.R
import com.example.pocketdex.core.designsystem.theme.PocketDexColors

/** Visual styles only. The feature orders types by source slot before choosing its primary style. */
enum class PokemonType(val labelRes: Int, val accent: Color, val onAccent: Color) {
    Bug(R.string.pd_type_bug, Color(0xFF83C300), PocketDexColors.Gray500),
    Dark(R.string.pd_type_dark, Color(0xFF5B5466), PocketDexColors.White),
    Dragon(R.string.pd_type_dragon, Color(0xFF006FC9), PocketDexColors.White),
    Electric(R.string.pd_type_electric, Color(0xFFFBD100), PocketDexColors.Gray500),
    Fairy(R.string.pd_type_fairy, Color(0xFFFB89EB), PocketDexColors.Gray500),
    Fighting(R.string.pd_type_fighting, Color(0xFFE0306A), PocketDexColors.Black),
    Fire(R.string.pd_type_fire, Color(0xFFFF9741), PocketDexColors.Gray500),
    Flying(R.string.pd_type_flying, Color(0xFF89AAE3), PocketDexColors.Gray500),
    Ghost(R.string.pd_type_ghost, Color(0xFF4C6AB2), PocketDexColors.White),
    Grass(R.string.pd_type_grass, Color(0xFF38BF4B), PocketDexColors.Gray500),
    Ground(R.string.pd_type_ground, Color(0xFFE87236), PocketDexColors.Gray600),
    Ice(R.string.pd_type_ice, Color(0xFF4CD1C0), PocketDexColors.Gray500),
    Normal(R.string.pd_type_normal, Color(0xFF919AA2), PocketDexColors.Gray500),
    Poison(R.string.pd_type_poison, Color(0xFFB567CE), PocketDexColors.Gray600),
    Psychic(R.string.pd_type_psychic, Color(0xFFFF6675), PocketDexColors.Gray500),
    Rock(R.string.pd_type_rock, Color(0xFFC8B686), PocketDexColors.Gray500),
    Steel(R.string.pd_type_steel, Color(0xFF5A8EA2), PocketDexColors.Gray600),
    Water(R.string.pd_type_water, Color(0xFF3692DC), PocketDexColors.Gray600);

    val iconRes: Int
        get() = when (this) {
            Bug -> R.drawable.pd_type_bug
            Dark -> R.drawable.pd_type_dark
            Dragon -> R.drawable.pd_type_dragon
            Electric -> R.drawable.pd_type_electric
            Fairy -> R.drawable.pd_type_fairy
            Fighting -> R.drawable.pd_type_fighting
            Fire -> R.drawable.pd_type_fire
            Flying -> R.drawable.pd_type_flying
            Ghost -> R.drawable.pd_type_ghost
            Grass -> R.drawable.pd_type_grass
            Ground -> R.drawable.pd_type_ground
            Ice -> R.drawable.pd_type_ice
            Normal -> R.drawable.pd_type_normal
            Poison -> R.drawable.pd_type_poison
            Psychic -> R.drawable.pd_type_psychic
            Rock -> R.drawable.pd_type_rock
            Steel -> R.drawable.pd_type_steel
            Water -> R.drawable.pd_type_water
        }
}
