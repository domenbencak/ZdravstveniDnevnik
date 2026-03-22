package com.example.zdravstvenidnevnik.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zdravstvenidnevnik.R
import com.example.zdravstvenidnevnik.data.Meritev
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VnosScreen(
    viewModel: MeritevViewModel,
    editMeritevId: Int? = null,
    onMeritevSaved: (Int) -> Unit,
    onNavigateToSeznam: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isEditMode = editMeritevId != null
    val meritevZaUrejanje by viewModel.getById(editMeritevId ?: -1)
        .collectAsStateWithLifecycle(initialValue = null)

    var ime by rememberSaveable { mutableStateOf("") }
    var priimek by rememberSaveable { mutableStateOf("") }
    var srcniUtrip by rememberSaveable { mutableStateOf("") }
    var spO2 by rememberSaveable { mutableStateOf("") }
    var temperatura by rememberSaveable { mutableStateOf("") }
    var selectedDateMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var fieldsInitialized by rememberSaveable(editMeritevId) { mutableStateOf(false) }

    var imeError by rememberSaveable { mutableStateOf<Int?>(null) }
    var priimekError by rememberSaveable { mutableStateOf<Int?>(null) }
    var srcniUtripError by rememberSaveable { mutableStateOf<Int?>(null) }
    var spO2Error by rememberSaveable { mutableStateOf<Int?>(null) }
    var temperaturaError by rememberSaveable { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    val naslov = if (isEditMode) {
        stringResource(R.string.screen_edit_measurement)
    } else {
        stringResource(R.string.screen_new_measurement)
    }

    val snackbarSuccess = if (isEditMode) {
        stringResource(R.string.msg_measurement_updated)
    } else {
        stringResource(R.string.msg_measurement_saved)
    }

    LaunchedEffect(isEditMode, meritevZaUrejanje?.id) {
        if (isEditMode && meritevZaUrejanje != null && !fieldsInitialized) {
            val m = meritevZaUrejanje ?: return@LaunchedEffect
            ime = m.ime
            priimek = m.priimek
            srcniUtrip = m.srcniUtrip.toString()
            spO2 = m.spO2.toString()
            temperatura = m.temperatura.toString()
            selectedDateMillis = m.datum
            fieldsInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(naslov) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.cd_open_settings)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonitorHeart,
                            contentDescription = null
                        )
                        Text(
                            text = naslov,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = stringResource(R.string.subtitle_measurement_form),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.section_personal_data),
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = ime,
                        onValueChange = {
                            ime = it
                            imeError = null
                        },
                        label = { Text(stringResource(R.string.field_first_name)) },
                        isError = imeError != null,
                        supportingText = {
                            if (imeError != null) {
                                Text(stringResource(imeError!!))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = priimek,
                        onValueChange = {
                            priimek = it
                            priimekError = null
                        },
                        label = { Text(stringResource(R.string.field_last_name)) },
                        isError = priimekError != null,
                        supportingText = {
                            if (priimekError != null) {
                                Text(stringResource(priimekError!!))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.section_measurement_date),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = df.format(Date(selectedDateMillis)),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.field_measurement_date)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showDatePicker = true }
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.section_measurement_values),
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = srcniUtrip,
                        onValueChange = {
                            srcniUtrip = it
                            srcniUtripError = null
                        },
                        label = { Text(stringResource(R.string.field_heart_rate)) },
                        isError = srcniUtripError != null,
                        supportingText = {
                            if (srcniUtripError != null) {
                                Text(stringResource(srcniUtripError!!))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = spO2,
                        onValueChange = {
                            spO2 = it
                            spO2Error = null
                        },
                        label = { Text(stringResource(R.string.field_spo2)) },
                        isError = spO2Error != null,
                        supportingText = {
                            if (spO2Error != null) {
                                Text(stringResource(spO2Error!!))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = temperatura,
                        onValueChange = {
                            temperatura = it
                            temperaturaError = null
                        },
                        label = { Text(stringResource(R.string.field_temperature)) },
                        isError = temperaturaError != null,
                        supportingText = {
                            if (temperaturaError != null) {
                                Text(stringResource(temperaturaError!!))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateMillis
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDateMillis =
                                datePickerState.selectedDateMillis ?: selectedDateMillis
                            showDatePicker = false
                        }) {
                            Text(stringResource(R.string.btn_datepicker_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.btn_datepicker_cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            HorizontalDivider()

            FilledTonalButton(
                onClick = onNavigateToSeznam,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.btn_measurements_list))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }

            Button(
                onClick = {
                    val parsedSrcniUtrip = srcniUtrip.toIntOrNull()
                    val parsedSpO2 = spO2.toIntOrNull()
                    val parsedTemperatura = temperatura.replace(',', '.').toDoubleOrNull()

                    imeError = if (ime.isBlank()) {
                        R.string.error_first_name_required
                    } else null
                    priimekError = if (priimek.isBlank()) {
                        R.string.error_last_name_required
                    } else null
                    srcniUtripError =
                        if (parsedSrcniUtrip == null || parsedSrcniUtrip !in 30..250) {
                            R.string.error_heart_rate_range
                        } else null
                    spO2Error = if (parsedSpO2 == null || parsedSpO2 !in 0..100) {
                        R.string.error_spo2_range
                    } else null
                    temperaturaError =
                        if (parsedTemperatura == null || parsedTemperatura !in 34.0..42.0) {
                            R.string.error_temperature_range
                        } else null

                    val imaNapako = imeError != null ||
                        priimekError != null ||
                        srcniUtripError != null ||
                        spO2Error != null ||
                        temperaturaError != null

                    if (imaNapako || parsedSrcniUtrip == null || parsedSpO2 == null || parsedTemperatura == null) {
                        return@Button
                    }

                    val idZaPosodobitev = if (isEditMode) {
                        meritevZaUrejanje?.id ?: editMeritevId ?: 0
                    } else 0

                    val meritev = Meritev(
                        id = idZaPosodobitev,
                        ime = ime.trim(),
                        priimek = priimek.trim(),
                        datum = selectedDateMillis,
                        srcniUtrip = parsedSrcniUtrip,
                        spO2 = parsedSpO2,
                        temperatura = parsedTemperatura
                    )

                    if (isEditMode) {
                        viewModel.update(meritev) {
                            scope.launch {
                                snackbarHostState.showSnackbar(snackbarSuccess)
                                onMeritevSaved(meritev.id)
                            }
                        }
                    } else {
                        viewModel.insert(meritev) { id ->
                            scope.launch {
                                snackbarHostState.showSnackbar(snackbarSuccess)
                                onMeritevSaved(id)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isEditMode) {
                        stringResource(R.string.btn_update_measurement)
                    } else {
                        stringResource(R.string.btn_save_measurement)
                    }
                )
            }
        }
    }
}
