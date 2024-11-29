package com.dullfan.nexuslink.ui.components.no_permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.R
import com.dullfan.nexuslink.ui.page.main.MainIntent
import com.dullfan.nexuslink.ui.page.main.MainViewModel
import com.dullfan.nexuslink.ui.theme.fontSize17Sp
import com.dullfan.nexuslink.ui.theme.padding10Dp
import com.dullfan.nexuslink.utils.PermissionUtil

@Composable
fun NoPermissionsComponent(modifier: Modifier = Modifier.fillMaxSize(),viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if(PermissionUtil.checkPermissions(context)){
            viewModel.onIntent(MainIntent.LoadContent)
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_permissions),
            contentDescription = null,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = padding10Dp),
            text = stringResource(R.string.permissions_error),
            textAlign = TextAlign.Center,
            fontSize = fontSize17Sp
        )

        Button(onClick = {
            val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            launcher.launch(intent)
        }) {
            Text(text = stringResource(R.string.go_to_set))
        }
    }
}

@Preview
@Composable
fun NavigationComponentsPreview() {
    NoPermissionsComponent()
}