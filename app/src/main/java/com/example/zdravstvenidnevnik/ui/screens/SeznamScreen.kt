package com.example.zdravstvenidnevnik.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zdravstvenidnevnik.R
import com.example.zdravstvenidnevnik.data.Meritev
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeznamScreen(
    viewModel: MeritevViewModel,
    onOpenDetails: (Int) -> Unit,
    onEditMeasurement: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val meritve = viewModel.vseMeritve.collectAsStateWithLifecycle().value
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.screen_measurements_list)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.cd_back)
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        if (meritve.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.msg_empty_measurements),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(meritve, key = { it.id }) { meritev ->
                    MeritevListCard(
                        meritev = meritev,
                        formattedDate = dateFormat.format(Date(meritev.datum)),
                        onOpenDetails = { onOpenDetails(meritev.id) },
                        onDelete = { viewModel.delete(meritev) },
                        onEdit = { onEditMeasurement(meritev.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MeritevListCard(
    meritev: Meritev,
    formattedDate: String,
    onOpenDetails: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${meritev.ime} ${meritev.priimek}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = context.getString(R.string.value_date, formattedDate),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = context.getString(R.string.value_bpm, meritev.srcniUtrip),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = context.getString(R.string.cd_edit_measurement)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = context.getString(R.string.cd_delete_measurement)
                    )
                }
            }
        }
    }
}
