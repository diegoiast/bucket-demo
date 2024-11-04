package com.example.bucketexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bucketexample.ui.theme.Test1Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import androidx.compose.material3.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }
    var isComputing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // New state variables for mode selection and IP address
    var selectedMode by remember { mutableStateOf("Local") }
    var ipAddress by remember { mutableStateOf("http://192.168.1.173:5000") }

    // Create processor based on selected mode
    val processor = when (selectedMode) {
        "Local" -> remember { AndroidValueProcessor() }
        else -> remember(ipAddress) { RemoteValueProcessor(ipAddress) }
    }

//    LaunchedEffect(processor) {
//        try {
//            processor.init()
//        } catch (e: Exception) {
//            println(e.message)
//        }
//    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Mode selection dropdown
        var expanded by remember { mutableStateOf(false) }
        val modes = listOf("Local", "Remote V1", "Remote V2")

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedMode,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                modes.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode) },
                        onClick = {
                            selectedMode = mode
                            expanded = false
                        }
                    )
                }
            }
        }

        // IP address input field (visible only for remote modes)
        if (selectedMode != "Local") {
            TextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Existing UI components
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            label = { Text("CSV of weights") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { text = randomize() }) {
                Text("New data")
            }
            Button(
                onClick = {
                    isComputing = true
                    progress = 0f
                    result = ""
                    scope.launch {
                        val textFlow: Flow<String> = text.asFlow()

                        if (processor is RemoteValueProcessor) {
                            when (selectedMode) {
                                "Remote V1" -> processor.setVersion(RemoteValueProcessor.V1)
                                "Remote V2" -> processor.setVersion(RemoteValueProcessor.V2)
                                // No need for "Local" case as it's not a RemoteValueProcessor
                            }
                        }

                        processor.init()
                        processStream(
                            stream = textFlow,
                            onProgress = { value, currentProgress ->
                                processor.processValue(value)
                                progress = currentProgress
                                isComputing = true
                            },
                            onEnd = {
                                isComputing = false
                                val count = processor.getItemsCount()
                                processor.flushRemainingValues()
                                val trips = processor.getAllProcessedTrips()
                                println("All Done in ${trips.size} trips")

                                snackbarHostState.showSnackbar(
                                    message = "Took ${trips.size} times to transport ${count} buckets",
                                    actionLabel = "OK"
                                )
                            }
                        )
                    }
                },
                enabled = !isComputing
            ) {
                Text("Compute")
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
        SnackbarHost(hostState = snackbarHostState)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun randomize(): String {
    val possibleValues = (20..60).map { it * 0.05 }.filter { it > 1.01 }
    val numberOfValues = (40..160).random()
    return (1..numberOfValues)
        .map { possibleValues.random() }
        .joinToString(separator = ", ") { "%.2f".format(it) }
}

fun String.asFlow(): Flow<String> = flow {
    split(",").forEach { value ->
        emit(value.trim())
//        delay(5) // Simulate delay between values in a stream
    }
}

suspend fun processStream(
    stream: Flow<String>,
    onProgress: suspend (String, Float) -> Unit,
    onEnd: suspend () -> Unit
) {
    var count = 0
    val totalItems = stream.count()

    stream.collect { value ->
        count++
        val progress = count.toFloat() / totalItems
        onProgress(value, progress)
    }

    onEnd()
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    Test1Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MainContent()
        }
    }
}
