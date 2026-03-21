package com.example.zdravstvenidnevnik.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VnosScreen(
    viewModel: MeritevViewModel,
    editMeritevId: Int? = null,
    onMeritevSaved: (Int) -> Unit,
    onNavigateToSeznam: () -> Unit
) {
    val context = LocalContext.current
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
        context.getString(R.string.screen_edit_measurement)
    } else {
        context.getString(R.string.screen_new_measurement)
    }

    val snackbarSuccess = if (isEditMode) {
        context.getString(R.string.msg_measurement_updated)
    } else {
        context.getString(R.string.msg_measurement_saved)
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = naslov,
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = ime,
                onValueChange = {
                    ime = it
                    imeError = null
                },
                label = { Text(context.getString(R.string.field_first_name)) },
                isError = imeError != null,
                supportingText = {
                    if (imeError != null) {
                        Text(context.getString(imeError!!))
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
                label = { Text(context.getString(R.string.field_last_name)) },
                isError = priimekError != null,
                supportingText = {
                    if (priimekError != null) {
                        Text(context.getString(priimekError!!))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = df.format(Date(selectedDateMillis)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(context.getString(R.string.field_measurement_date)) },
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
                            Text(context.getString(R.string.btn_datepicker_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(context.getString(R.string.btn_datepicker_cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = srcniUtrip,
                onValueChange = {
                    srcniUtrip = it
                    srcniUtripError = null
                },
                label = { Text(context.getString(R.string.field_heart_rate)) },
                isError = srcniUtripError != null,
                supportingText = {
                    if (srcniUtripError != null) {
                        Text(context.getString(srcniUtripError!!))
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
                label = { Text(context.getString(R.string.field_spo2)) },
                isError = spO2Error != null,
                supportingText = {
                    if (spO2Error != null) {
                        Text(context.getString(spO2Error!!))
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
                label = { Text(context.getString(R.string.field_temperature)) },
                isError = temperaturaError != null,
                supportingText = {
                    if (temperaturaError != null) {
                        Text(context.getString(temperaturaError!!))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        context.getString(R.string.btn_update_measurement)
                    } else {
                        context.getString(R.string.btn_save_measurement)
                    }
                )
            }

            OutlinedButton(
                onClick = onNavigateToSeznam,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(context.getString(R.string.btn_measurements_list))
            }
        }
    }
}
