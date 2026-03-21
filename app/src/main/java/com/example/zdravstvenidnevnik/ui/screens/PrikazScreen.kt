package com.example.zdravstvenidnevnik.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zdravstvenidnevnik.R
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrikazScreen(
    viewModel: MeritevViewModel,
    meritevId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val meritev by viewModel.getById(meritevId)
        .collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.screen_measurement_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            meritev?.let { m ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${m.ime} ${m.priimek}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        HorizontalDivider()
                        val dateFormat = SimpleDateFormat(
                            "dd.MM.yyyy", Locale.getDefault()
                        )
                        PodatekRow(
                            context.getString(R.string.field_measurement_date),
                            dateFormat.format(Date(m.datum))
                        )
                        PodatekRow(
                            context.getString(R.string.field_heart_rate_short),
                            context.getString(R.string.value_bpm, m.srcniUtrip)
                        )
                        PodatekRow(
                            context.getString(R.string.field_spo2_short),
                            context.getString(R.string.value_percent, m.spO2)
                        )
                        PodatekRow(
                            context.getString(R.string.field_temperature_short),
                            context.getString(R.string.value_celsius, m.temperatura)
                        )
                    }
                }
            } ?: run {
                Text(context.getString(R.string.msg_measurement_not_found))
            }
        }
    }
}
@Composable
private fun PodatekRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
