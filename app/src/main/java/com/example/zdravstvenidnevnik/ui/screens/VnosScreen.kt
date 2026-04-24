package com.example.zdravstvenidnevnik.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zdravstvenidnevnik.R
import com.example.zdravstvenidnevnik.data.Meritev
import com.example.zdravstvenidnevnik.viewmodel.MeritevViewModel
import com.google.firebase.auth.FirebaseAuth
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
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    VnosScreen(
        viewModel = viewModel,
        editMeritevId = editMeritevId,
        onMeritevSaved = onMeritevSaved,
        onNavigateToSeznam = onNavigateToSeznam,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateBack = onNavigateBack,
        currentUserDisplayName = FirebaseAuth.getInstance().currentUser?.displayName.orEmpty(),
        onMeritevEdited = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VnosScreen(
    viewModel: MeritevViewModel,
    editMeritevId: Int? = null,
    onMeritevSaved: (Int) -> Unit,
    onNavigateToSeznam: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    currentUserDisplayName: String,
    onMeritevEdited: () -> Unit = {}
) {
    MeasurementScreen(
        viewModel = viewModel,
        editMeritevId = editMeritevId,
        onMeritevSaved = onMeritevSaved,
        onNavigateToSeznam = onNavigateToSeznam,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateBack = onNavigateBack,
        currentUserDisplayName = currentUserDisplayName,
        onMeritevEdited = onMeritevEdited
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    viewModel: MeritevViewModel,
    editMeritevId: Int? = null,
    onMeritevSaved: (Int) -> Unit,
    onNavigateToSeznam: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    currentUserDisplayName: String,
    onMeritevEdited: () -> Unit = {}
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
    var lastPrefilledIme by rememberSaveable(editMeritevId) { mutableStateOf("") }
    var lastPrefilledPriimek by rememberSaveable(editMeritevId) { mutableStateOf("") }

    var imeError by rememberSaveable { mutableStateOf<Int?>(null) }
    var priimekError by rememberSaveable { mutableStateOf<Int?>(null) }
    var srcniUtripError by rememberSaveable { mutableStateOf<Int?>(null) }
    var spO2Error by rememberSaveable { mutableStateOf<Int?>(null) }
    var temperaturaError by rememberSaveable { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showCameraPreview by rememberSaveable { mutableStateOf(false) }
    var activeOcrField by rememberSaveable { mutableStateOf<OcrMeasurementField?>(null) }
    var pendingOcrField by rememberSaveable { mutableStateOf<OcrMeasurementField?>(null) }
    var isOcrProcessing by rememberSaveable { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
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
    val sensorReadFailedMessage = stringResource(R.string.msg_sensor_read_failed)
    val ocrReadFailedMessage = stringResource(R.string.msg_ocr_read_failed)
    val cameraPermissionDeniedMessage = stringResource(R.string.msg_camera_permission_denied)

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        val requestedField = pendingOcrField
        pendingOcrField = null

        if (granted && requestedField != null) {
            activeOcrField = requestedField
            showCameraPreview = true
        } else if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar(cameraPermissionDeniedMessage)
            }
        }
    }

    fun openCameraForField(field: OcrMeasurementField) {
        if (hasCameraPermission) {
            activeOcrField = field
            showCameraPreview = true
            return
        }

        pendingOcrField = field
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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

    LaunchedEffect(isEditMode, currentUserDisplayName) {
        if (isEditMode) return@LaunchedEffect

        val normalizedDisplayName = currentUserDisplayName.trim()
        if (normalizedDisplayName.isBlank()) return@LaunchedEffect

        val nameParts = normalizedDisplayName.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        if (nameParts.isEmpty()) return@LaunchedEffect

        val prefillIme = nameParts.first()
        val prefillPriimek = nameParts.drop(1).joinToString(" ")

        val canReplaceIme = ime.isBlank() || ime == lastPrefilledIme
        val canReplacePriimek = priimek.isBlank() || priimek == lastPrefilledPriimek

        if (canReplaceIme) {
            ime = prefillIme
        }
        if (canReplacePriimek) {
            priimek = prefillPriimek
        }

        lastPrefilledIme = prefillIme
        lastPrefilledPriimek = prefillPriimek
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(naslov) },
                navigationIcon = {
                    if (isEditMode) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            viewModel.readHeartRate { value ->
                                                if (value != null) {
                                                    srcniUtrip = value.toString()
                                                    srcniUtripError = null
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(sensorReadFailedMessage)
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MonitorHeart,
                                            contentDescription = stringResource(R.string.cd_fill_heart_rate_sensor)
                                        )
                                    }

                                    IconButton(
                                        onClick = { openCameraForField(OcrMeasurementField.HEART_RATE) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoCamera,
                                            contentDescription = stringResource(R.string.cd_scan_heart_rate_ocr)
                                        )
                                    }
                                }
                            },
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
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            viewModel.readSpO2 { value ->
                                                if (value != null) {
                                                    spO2 = value.toString()
                                                    spO2Error = null
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(sensorReadFailedMessage)
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.WaterDrop,
                                            contentDescription = stringResource(R.string.cd_fill_spo2_sensor)
                                        )
                                    }

                                    IconButton(
                                        onClick = { openCameraForField(OcrMeasurementField.SPO2) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoCamera,
                                            contentDescription = stringResource(R.string.cd_scan_spo2_ocr)
                                        )
                                    }
                                }
                            },
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
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            viewModel.readTemperature { value ->
                                                if (value != null) {
                                                    temperatura = value.toString()
                                                    temperaturaError = null
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(sensorReadFailedMessage)
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Thermostat,
                                            contentDescription = stringResource(R.string.cd_fill_temperature_sensor)
                                        )
                                    }

                                    IconButton(
                                        onClick = { openCameraForField(OcrMeasurementField.TEMPERATURE) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoCamera,
                                            contentDescription = stringResource(R.string.cd_scan_temperature_ocr)
                                        )
                                    }
                                }
                            },
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

                        fun clearVitalFields() {
                            srcniUtrip = ""
                            spO2 = ""
                            temperatura = ""
                            srcniUtripError = null
                            spO2Error = null
                            temperaturaError = null
                        }

                        if (isEditMode) {
                            viewModel.update(meritev) {
                                onMeritevEdited()
                            }
                        } else {
                            viewModel.insert(meritev) { id ->
                                scope.launch {
                                    clearVitalFields()
                                    onMeritevSaved(id)
                                    snackbarHostState.showSnackbar(snackbarSuccess)
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

            if (showCameraPreview && activeOcrField != null) {
                CameraPreview(
                    cameraController = cameraController,
                    isProcessing = isOcrProcessing,
                    onClose = {
                        if (isOcrProcessing) return@CameraPreview
                        showCameraPreview = false
                        activeOcrField = null
                    },
                    onCapture = {
                        val targetField = activeOcrField ?: return@CameraPreview
                        if (isOcrProcessing) return@CameraPreview

                        scope.launch {
                            isOcrProcessing = true
                            val bitmap = captureImageBitmap(
                                context = context,
                                cameraController = cameraController
                            )
                            val extractedValue = bitmap?.let { recognizeNumericValueFromBitmap(it) }

                            if (extractedValue != null) {
                                when (targetField) {
                                    OcrMeasurementField.HEART_RATE -> {
                                        srcniUtrip = extractedValue
                                        srcniUtripError = null
                                    }
                                    OcrMeasurementField.SPO2 -> {
                                        spO2 = extractedValue
                                        spO2Error = null
                                    }
                                    OcrMeasurementField.TEMPERATURE -> {
                                        temperatura = extractedValue
                                        temperaturaError = null
                                    }
                                }
                                showCameraPreview = false
                                activeOcrField = null
                            } else {
                                snackbarHostState.showSnackbar(ocrReadFailedMessage)
                            }

                            isOcrProcessing = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
