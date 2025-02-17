package com.broccolist.login.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.broccolist_icon
import broccolist.composeapp.generated.resources.cd_broccolist_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BroccolistHeader() {
//    Text(
//        text = stringResource(Res.string.login_title),
//        style = MaterialTheme.typography.titleLarge,
//        fontSize = 28.sp,
//        color = colorScheme.primary,
//    )
//    HorizontalDivider(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(24.dp),
//        thickness = 1.dp,
//        color = colorScheme.primary,
//    )
    Icon(
        painter = painterResource(Res.drawable.broccolist_icon),
        contentDescription = stringResource(Res.string.cd_broccolist_icon),
        tint = colorScheme.primary,
        modifier = Modifier.size(100.dp)
    )
}
